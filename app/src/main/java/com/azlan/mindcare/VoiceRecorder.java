package com.azlan.mindcare;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class VoiceRecorder extends AppCompatActivity {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 1;
    private SpeechRecognizer speechRecognizer;
    private TextToSpeech textToSpeech;
    private TextView resultText, moodText;
    private Button recordButton;
    private boolean isListening = false;

    private final Map<String, String> moodResponses = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice);

        resultText = findViewById(R.id.resultText);
        moodText = findViewById(R.id.moodText);
        recordButton = findViewById(R.id.recordButton);

        // Request microphone permission
        requestMicrophonePermission();

        // Initialize Speech Recognizer
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        // Initialize Text-to-Speech
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.US);
            }
        });

        // Predefine mood-based responses
        setupMoodResponses();

        recordButton.setOnClickListener(v -> {
            if (isListening) {
                stopListening();
            } else {
                startListening();
            }
        });
    }

    // Request microphone permission at runtime
    private void requestMicrophonePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
        }
    }

    // Handle permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Microphone permission granted!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Microphone permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Start voice recognition
    private void startListening() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        }

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                Log.d("SpeechRecognizer", "Ready for speech...");
            }

            @Override
            public void onBeginningOfSpeech() {
                Log.d("SpeechRecognizer", "Speech started...");
            }

            @Override
            public void onError(int error) {
                Log.e("SpeechRecognizer", "Error Code: " + error);
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String detectedText = matches.get(0);
                    resultText.setText("Detected: " + detectedText);
                    analyzeMood(detectedText);
                }
            }

            @Override public void onEndOfSpeech() { Log.d("SpeechRecognizer", "Speech ended."); }
            @Override public void onEvent(int eventType, Bundle params) {}
            @Override public void onPartialResults(Bundle partialResults) {}
            @Override public void onBufferReceived(byte[] buffer) {}
            @Override public void onRmsChanged(float rmsdB) {}
        });

        speechRecognizer.startListening(intent);
        isListening = true;
        recordButton.setText("Stop Recording");
    }

    // Stop voice recognition
    private void stopListening() {
        if (speechRecognizer != null) {
            speechRecognizer.stopListening();
            isListening = false;
            recordButton.setText("Start Recording");
        }
    }

    // Analyze mood based on detected words and respond with TTS
    private void analyzeMood(String text) {
        String mood = "🤔 Neutral";
        String response = "I am not sure how you feel.";

        text = text.toLowerCase();

        for (Map.Entry<String, String> entry : moodResponses.entrySet()) {
            if (text.contains(entry.getKey())) {
                mood = entry.getKey();
                response = entry.getValue();
                break;
            }
        }

        moodText.setText("Detected Mood: " + mood);
        speakResponse(response);
    }

    // Setup mood-based responses
    private void setupMoodResponses() {
        moodResponses.put("sad", "😢 You seem sad. I’m here for you. Do you want to talk about it?");
        moodResponses.put("depressed", "😞 I’m sorry you feel this way. You're not alone. Maybe a deep breath can help.");
        moodResponses.put("angry", "😡 You sound frustrated. Let’s take a deep breath together.");
        moodResponses.put("happy", "😊 You sound happy! That’s wonderful!");
        moodResponses.put("excited", "😃 Wow, you seem really excited! Tell me more!");
        moodResponses.put("frustrated", "😠 I hear your frustration. Would you like some suggestions to feel better?");
        moodResponses.put("crying", "😭 I hear that you’re crying. I’m here for you. Let’s talk.");
        moodResponses.put("nervous", "😰 Feeling nervous? It’s okay. Take a deep breath with me.");
        moodResponses.put("stressed", "😔 Feeling stressed? Maybe some deep breathing could help.");
        moodResponses.put("tired", "😴 You sound tired. Maybe some rest would help?");
        moodResponses.put("bored", "😐 Feeling bored? How about we play a game?");
        moodResponses.put("lonely", "💙 You’re not alone. I’m always here to talk.");
    }

    // Speak response using Text-to-Speech
    private void speakResponse(String response) {
        textToSpeech.speak(response, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }
}
