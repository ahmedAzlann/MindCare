package com.azlan.mindcare;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.*;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.util.HashMap;
import java.util.LinkedList;
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

        setupFaceDetector();
        requestPermissions();
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
            moodStartTime = currentTime; // Reset start time for new mood
            runOnUiThread(() -> moodTextView.setText("Mood: " + lastStableMood));
        } else if (currentTime - moodStartTime >= 3000 && currentTime - lastScoreUpdateTime > 24 * 60 * 60 * 1000) {
            updateScore(mostFrequentMood);
            lastScoreUpdateTime = currentTime;
        }
    }
    private void updateScore(String mood) {
        int moodScore = 0;
        if (mood.equals("😊 Happy")) {
            moodScore = 30;
        } else if (mood.equals("😐 Neutral")) {
            moodScore = 20;
        } else if (mood.equals("😱 Surprised")) {
            moodScore = 10;
        }

        userScore += moodScore;
        runOnUiThread(() -> scoreTextView.setText("Score: " + userScore));
    }

    private String getMostFrequentMood() {
        Map<String, Integer> frequencyMap = new HashMap<>();
        for (String mood : moodQueue) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                frequencyMap.put(mood, frequencyMap.getOrDefault(mood, 0) + 1);
            }
        }



        String mostFrequent = lastStableMood;
        int maxCount = 0;
        for (Map.Entry<String, Integer> entry : frequencyMap.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                mostFrequent = entry.getKey();
            }
        }
        return mostFrequent;
    }
}
