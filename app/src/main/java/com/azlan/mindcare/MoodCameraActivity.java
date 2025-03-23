package com.azlan.mindcare;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.*;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MoodCameraActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.CAMERA
    };

    private TextView moodTextView, scoreTextView;
    private PreviewView previewView;
    private ExecutorService cameraExecutor;
    private FaceDetector faceDetector;
    private int userScore = 0; // User score

    // Firebase
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;

    // Mood smoothing variables
    private static final int SMOOTHING_WINDOW_SIZE = 5;
    private final Queue<String> moodQueue = new LinkedList<>();
    private String lastStableMood = "Detecting...";
    private long lastScoreUpdateTime = 0; // Last score update timestamp
    private long moodStartTime = 0; // Timestamp when a mood was first detected

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_camera);

        previewView = findViewById(R.id.previewView);
        moodTextView = findViewById(R.id.moodTextView);
        scoreTextView = findViewById(R.id.scoreTextView);
        cameraExecutor = Executors.newSingleThreadExecutor();

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        setupFaceDetector();
        requestPermissions();
        fetchUserScore(); // Fetch and display the score
    }

    private void fetchUserScore() {
        String userId = firebaseAuth.getCurrentUser().getUid();

        databaseReference.child(userId).child("score").get().addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {
                userScore = dataSnapshot.getValue(Integer.class) != null ? dataSnapshot.getValue(Integer.class) : 0;
                runOnUiThread(() -> scoreTextView.setText("Score: " + userScore)); // Update UI
            }
        }).addOnFailureListener(e -> Log.e("Firebase", "Failed to fetch user score", e));
    }


    private void requestPermissions() {
        if (!hasPermissions()) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            startCamera();
        }
    }

    private boolean hasPermissions() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void setupFaceDetector() {
        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
                .build();
        faceDetector = FaceDetection.getClient(options);
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                        .build();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(cameraExecutor, this::detectFace);

                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);

            } catch (Exception e) {
                Log.e("CameraX", "Camera initialization failed", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void detectFace(ImageProxy imageProxy) {
        InputImage image = InputImage.fromMediaImage(imageProxy.getImage(), imageProxy.getImageInfo().getRotationDegrees());

        faceDetector.process(image)
                .addOnSuccessListener(faces -> {
                    String detectedMood = "No Face Detected";

                    if (faces.isEmpty()) {
                        Log.d("MoodCamera", "No faces detected");
                    } else {
                        for (Face face : faces) {
                            float smilingProbability = face.getSmilingProbability() != null ? face.getSmilingProbability() : -1;
                            float leftEyeOpenProbability = face.getLeftEyeOpenProbability() != null ? face.getLeftEyeOpenProbability() : -1;
                            float rightEyeOpenProbability = face.getRightEyeOpenProbability() != null ? face.getRightEyeOpenProbability() : -1;

                            // Improved mood detection logic
                            if (leftEyeOpenProbability < 0.2 && rightEyeOpenProbability < 0.2) {
                                detectedMood = "😴 Sleepy";
                            } else if (smilingProbability < 0.2 && leftEyeOpenProbability > 0.9 && rightEyeOpenProbability > 0.9) {
                                detectedMood = "😡 Angry";
                            } else if (smilingProbability < 0.3 && leftEyeOpenProbability > 0.6 && rightEyeOpenProbability > 0.6) {
                                detectedMood = "😱 Surprised";
                            } else if (smilingProbability > 0.75) {
                                detectedMood = "😊 Happy";
                            } else if (smilingProbability > 0.4) {
                                detectedMood = "😐 Neutral";
                            } else {
                                detectedMood = "😢 Sad";
                            }
                        }
                    }

                    Log.d("MoodCamera", "Detected mood: " + detectedMood);
                    updateStableMood(detectedMood);
                    imageProxy.close();
                })
                .addOnFailureListener(e -> {
                    Log.e("MLKit", "Face detection failed", e);
                    imageProxy.close();
                });
    }

    private void updateStableMood(String newMood) {
        moodQueue.add(newMood);

        if (moodQueue.size() > SMOOTHING_WINDOW_SIZE) {
            moodQueue.poll();
        }

        String mostFrequentMood = getMostFrequentMood();
        long currentTime = System.currentTimeMillis();

        if (!mostFrequentMood.equals(lastStableMood)) {
            lastStableMood = mostFrequentMood;
            moodStartTime = currentTime;
            runOnUiThread(() -> moodTextView.setText("Mood: " + lastStableMood));
        } else if (currentTime - moodStartTime >= 3000 && currentTime - lastScoreUpdateTime > 24 * 60 * 60 * 1000) {
            updateScore(mostFrequentMood);
            lastScoreUpdateTime = currentTime;
        }
    }

    private void updateScore(String mood) {
        String userId = firebaseAuth.getCurrentUser().getUid();
        String todayDate = getCurrentDate(); // Get today's date

        databaseReference.child(userId).get().addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {
                userScore = dataSnapshot.child("score").getValue(Integer.class) != null ?
                        dataSnapshot.child("score").getValue(Integer.class) : 0;
                String lastMoodScoreDate = dataSnapshot.child("lastMoodScoreDate").getValue(String.class);

                // Check if the last update was today
                if (lastMoodScoreDate != null && lastMoodScoreDate.equals(todayDate)) {
                    Log.d("MoodCamera", "Points already given today.");
                    return; // Exit to prevent duplicate points
                }

                // Assign points based on mood
                int moodScore = mood.equals("😊 Happy") ? 30 :
                        mood.equals("😐 Neutral") ? 20 :
                                mood.equals("😱 Surprised") ? 10 : 0;

                userScore += moodScore;

                // Update UI
                runOnUiThread(() -> scoreTextView.setText("Score: " + userScore));

                // Update Firebase
                databaseReference.child(userId).child("score").setValue(userScore);
                databaseReference.child(userId).child("lastMoodScoreDate").setValue(todayDate);

                // Show a toast message for successful points update
                runOnUiThread(() ->
                        Toast.makeText(MoodCameraActivity.this, "🎉 Mood points added for today!", Toast.LENGTH_SHORT).show()
                );

            } else {
                // If user data doesn't exist, initialize it
                databaseReference.child(userId).child("score").setValue(0);
                databaseReference.child(userId).child("lastMoodScoreDate").setValue(todayDate);
            }
        }).addOnFailureListener(e -> Log.e("Firebase", "Failed to fetch user data", e));
    }
    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }



    private String getMostFrequentMood() {
        Map<String, Integer> frequencyMap = new HashMap<>();
        for (String mood : moodQueue) {
            frequencyMap.put(mood, frequencyMap.getOrDefault(mood, 0) + 1);
        }

        return frequencyMap.entrySet().stream().max(Map.Entry.comparingByValue()).get().getKey();
    }
}
