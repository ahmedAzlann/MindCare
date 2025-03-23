package com.azlan.mindcare;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.azlan.mindcare.fragments.HomeFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;  // Import Collections for shuffling
import java.util.List;
import java.util.Locale;

public class RiddleGameActivity extends AppCompatActivity {

    private TextToSpeech textToSpeech;
    private TextView riddleTextView, timerTextView, scoreTextView, answerTextView;
    private EditText userAnswerEditText;
    private Button submitButton, speechButton, nextButton;
    private int score = 0, questionIndex = 0, questionsAnswered = 0;
    private long timeLeftInMillis = 30000;  // 30 seconds timer
    private List<String[]> riddles;
    private CountDownTimer countDownTimer;


    private static final String PREFS_NAME = "RiddleGamePrefs";
    private static final String KEY_TIME_LEFT = "timeLeft";
    private static final String KEY_SCORE = "score";
    private static final String KEY_QUESTION_INDEX = "questionIndex";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_riddle_game);

        // Initialize views
        riddleTextView = findViewById(R.id.riddleTextView);
        timerTextView = findViewById(R.id.timerTextView);
        scoreTextView = findViewById(R.id.scoreTextView);
        answerTextView = findViewById(R.id.resultTextView);
        userAnswerEditText = findViewById(R.id.userAnswerEditText);
        submitButton = findViewById(R.id.submitButton);
        speechButton = findViewById(R.id.speechButton);
        nextButton = findViewById(R.id.nextRiddleButton);

        // Load riddles from JSON
        loadRiddles();

        // Shuffle the riddles to randomize them
        Collections.shuffle(riddles);

        // Initialize Text-to-Speech
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.US);
            }
        });

        // Start the riddle game
        startRiddleGame();

        // Submit answer button
        submitButton.setOnClickListener(v -> checkAnswer());

        // Speech recognition button
        speechButton.setOnClickListener(v -> startSpeechRecognition());

        // Next riddle button
        nextButton.setOnClickListener(v -> {
            if (questionIndex < riddles.size() - 1 && questionsAnswered < 5) {
                questionIndex++;
                startRiddleGame();
            } else {
                endGame();
            }
        });
    }

    private void loadRiddles() {
        riddles = new ArrayList<>();

        try {
            // Read JSON file from assets
            InputStream is = getAssets().open("riddles_and_facts.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            String json = new String(buffer, "UTF-8");
            JSONObject jsonObject = new JSONObject(json);

            // Parse riddles
            JSONArray riddlesArray = jsonObject.getJSONArray("riddles");
            for (int i = 0; i < riddlesArray.length(); i++) {
                JSONObject riddleObject = riddlesArray.getJSONObject(i);
                String question = riddleObject.getString("question");
                String answer = riddleObject.getString("answer").toLowerCase();  // Use answer directly

                // Store the riddle with the question and answer
                riddles.add(new String[]{question, answer});
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading riddles", Toast.LENGTH_SHORT).show();
        }
    }

    private void startRiddleGame() {
        // Get the current riddle
        String[] currentRiddle = riddles.get(questionIndex);
        riddleTextView.setText(currentRiddle[0]);
        answerTextView.setText(""); // Clear the answer text view

        submitButton.setEnabled(true);
        userAnswerEditText.setEnabled(true);
        speechButton.setEnabled(true);

        // Clear the text editor (user answer field)
        userAnswerEditText.setText("");  // This line clears the text editor

        // Speak the riddle aloud
        textToSpeech.speak(currentRiddle[0], TextToSpeech.QUEUE_FLUSH, null, null);

        // Reset timer and start
        timeLeftInMillis = 30000;
        updateTimerText();
        startTimer();
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateTimerText();
            }

            @Override
            public void onFinish() {
                answerTextView.setText("Correct answer: " + riddles.get(questionIndex)[1]);
                nextButton.setVisibility(View.VISIBLE);
                Toast.makeText(RiddleGameActivity.this, "Time's up! Correct answer: " + riddles.get(questionIndex)[1], Toast.LENGTH_SHORT).show();
                questionsAnswered++;
            }
        }.start();
    }

    private void updateTimerText() {
        int seconds = (int) (timeLeftInMillis / 1000);
        timerTextView.setText(String.format("%02d", seconds));
    }

    private void checkAnswer() {
        // Stop the countdown timer when the answer is submitted
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        String userAnswer = userAnswerEditText.getText().toString().trim().toLowerCase();

        // Normalize the user's answer by removing 'a' or 'an' if it exists at the start
        String normalizedUserAnswer = normalizeAnswer(userAnswer);

        // Get the correct answer from the updated riddles structure (riddles.get(questionIndex)[1] returns the answer)
        String correctAnswer = riddles.get(questionIndex)[1]; // Correct answer is now a single string

        // Normalize the correct answer as well (remove 'a' or 'an' if it's there at the start)
        String normalizedCorrectAnswer = normalizeAnswer(correctAnswer.trim().toLowerCase());

        // Check if the normalized user answer matches the normalized correct answer using singular/plural matching
        boolean isCorrect = isSingularOrPluralMatch(normalizedUserAnswer, normalizedCorrectAnswer);

        // If the answer is correct
        if (isCorrect) {
            score += 10;
            scoreTextView.setText("Score: " + score);
            textToSpeech.speak("Correct answer!", TextToSpeech.QUEUE_FLUSH, null, null);
            Toast.makeText(RiddleGameActivity.this, "Correct answer!", Toast.LENGTH_SHORT).show();
        } else {
            textToSpeech.speak("Wrong answer! Correct answer was: " + correctAnswer, TextToSpeech.QUEUE_FLUSH, null, null);
            Toast.makeText(RiddleGameActivity.this, "Wrong answer! Correct answer was: " + correctAnswer, Toast.LENGTH_SHORT).show();
        }

        // Display the correct answer and show the next button
        answerTextView.setText("Correct answer: " + correctAnswer);
        nextButton.setVisibility(View.VISIBLE);

        // Disable submit button, answer field, and speech button after submission
        submitButton.setEnabled(false);
        userAnswerEditText.setEnabled(false);
        speechButton.setEnabled(false);

        questionsAnswered++;
    }

    private boolean isSingularOrPluralMatch(String userAnswer, String correctAnswer) {
        // Handle the singular/plural logic here
        if (correctAnswer.equals(userAnswer)) {
            return true;
        }

        // Handle pluralization (e.g., "egg" and "eggs")
        if (correctAnswer.endsWith("s") && userAnswer.equals(correctAnswer.substring(0, correctAnswer.length() - 1))) {
            return true; // e.g., "egg" and "eggs"
        }

        // Handle singular/plural logic for words like "a", "an", and other variations
        if (userAnswer.equals(correctAnswer + "s")) {
            return true; // e.g., "egg" and "eggs"
        }

        return false;
    }

    // Helper method to normalize the answer (remove 'a' or 'an' if it exists at the start)
    private String normalizeAnswer(String answer) {
        // Remove leading "a " or "an " (case insensitive)
        if (answer.startsWith("a ")) {
            return answer.substring(2).trim(); // Remove "a " (including the space)
        } else if (answer.startsWith("an ")) {
            return answer.substring(3).trim(); // Remove "an " (including the space)
        }
        return answer; // Return as is if no "a" or "an" at the start
    }

    private void startSpeechRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            // Stop the countdown timer if it's still running
            if (countDownTimer != null) {
                countDownTimer.cancel();
            }

            // Clear the timer display
            timerTextView.setText("00");

            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches != null && !matches.isEmpty()) {
                String spokenAnswer = matches.get(0).toLowerCase();
                String correctAnswer = riddles.get(questionIndex)[1].toLowerCase();

                // Check if the spoken answer is correct
                if (spokenAnswer.equals(correctAnswer)) {
                    score += 10;
                    scoreTextView.setText("Score: " + score);
                    textToSpeech.speak("Correct answer!", TextToSpeech.QUEUE_FLUSH, null, null);
                    Toast.makeText(this, "Correct answer!", Toast.LENGTH_SHORT).show();
                } else {
                    textToSpeech.speak("Wrong answer! The correct answer was: " + correctAnswer, TextToSpeech.QUEUE_FLUSH, null, null);
                    Toast.makeText(this, "Wrong answer! The correct answer was: " + correctAnswer, Toast.LENGTH_SHORT).show();
                }
            }

            nextButton.setVisibility(View.VISIBLE); // Show the next button after answering
            questionsAnswered++;
        }
    }

    private void endGame() {
        // Get the current user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            String userId = user.getUid();
            DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("score");

            // Retrieve the current score first
            databaseRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    int previousScore = 0;
                    if (task.getResult().exists()) {
                        previousScore = task.getResult().getValue(Integer.class);  // Get the existing score
                    }

                    int updatedScore = previousScore + score;  // Add new score

                    // Update Firebase with the new total score
                    databaseRef.setValue(updatedScore)
                            .addOnSuccessListener(aVoid ->
                                    Toast.makeText(RiddleGameActivity.this, "Score updated successfully!", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e ->
                                    Toast.makeText(RiddleGameActivity.this, "Failed to update score", Toast.LENGTH_SHORT).show());
                }
            });
        }

        // Show final score message
        Toast.makeText(this, "Game Over! Your final score is: " + score, Toast.LENGTH_LONG).show();

        // Navigate to HomeFragment (if applicable)
        Intent intent = new Intent(RiddleGameActivity.this, HomeFragment.class);
        startActivity(intent);
        finish();
    }


}
