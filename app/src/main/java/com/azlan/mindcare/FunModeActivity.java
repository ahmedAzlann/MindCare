package com.azlan.mindcare;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

public class FunModeActivity extends AppCompatActivity {

    private Button riddleButton, funFactsButton, ticTacToeButton;
    private TextView funFactTextView;
    private ArrayList<String> funFacts;
    private TextToSpeech textToSpeech;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fun_mode);

        // Initialize buttons and TextView
        riddleButton = findViewById(R.id.riddleButton);
        funFactsButton = findViewById(R.id.funFactsButton);
        ticTacToeButton = findViewById(R.id.ticTacToeButton); // New Tic Tac Toe button
        funFactTextView = findViewById(R.id.funFactTextView);

        // Initialize Text-to-Speech
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.US);
            }
        });

        // Load fun facts from JSON
        loadFunFacts();

        // Start the Riddle Game Activity
        riddleButton.setOnClickListener(v -> {
            Intent riddleIntent = new Intent(FunModeActivity.this, RiddleGameActivity.class);
            startActivity(riddleIntent);
        });

        // Display and speak a random fun fact when button is clicked
        funFactsButton.setOnClickListener(v -> displayRandomFunFact());

        // Start Tic Tac Toe Game Activity
        ticTacToeButton.setOnClickListener(v -> {
            Intent ticTacToeIntent = new Intent(FunModeActivity.this, TicTacToeActivity.class);
            startActivity(ticTacToeIntent);
        });
    }

    // Load Fun Facts from the JSON file
    private void loadFunFacts() {
        funFacts = new ArrayList<>();

        try {
            // Read JSON file from assets
            InputStream is = getAssets().open("riddles_and_facts.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            String json = new String(buffer, "UTF-8");
            JSONObject jsonObject = new JSONObject(json);

            // Parse fun facts
            JSONArray funFactsArray = jsonObject.getJSONArray("fun_facts");
            for (int i = 0; i < funFactsArray.length(); i++) {
                String funFact = funFactsArray.getString(i);
                funFacts.add(funFact);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Display a random fun fact and speak it
    private void displayRandomFunFact() {
        if (!funFacts.isEmpty()) {
            Random random = new Random();
            int index = random.nextInt(funFacts.size());
            String randomFunFact = funFacts.get(index);

            // Display the fun fact
            funFactTextView.setText(randomFunFact);
            funFactTextView.setVisibility(View.VISIBLE);

            // Speak the fun fact aloud
            textToSpeech.speak(randomFunFact, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    @Override
    protected void onDestroy() {
        // Shutdown the Text-to-Speech engine
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}
