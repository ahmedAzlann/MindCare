package com.azlan.mindcare;

import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Locale;
import java.util.Random;

public class TicTacToeActivity extends AppCompatActivity {

    private Button[][] buttons = new Button[3][3];
    private boolean playerTurn = true;
    private int roundCount = 0;
    private TextView statusTextView;
    private static final String PLAYER = "X";
    private static final String AI = "O";
    private TextToSpeech textToSpeech;
    private Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tic_tac_toe);

        statusTextView = findViewById(R.id.statusTextView);
        GridLayout gridLayout = findViewById(R.id.gridLayout);

        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.US);
            }
        });

        // Initialize buttons dynamically
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                final int row = i;
                final int col = j;
                buttons[i][j] = new Button(this);
                buttons[i][j].setTextSize(32);
                buttons[i][j].setOnClickListener(v -> playerMove(row, col));

                // Set GridLayout parameters
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = 200;
                params.height = 200;
                params.setMargins(5, 5, 5, 5);
                buttons[i][j].setLayoutParams(params);
                buttons[i][j].setBackgroundColor(0xFFFFFFFF); // White background
                gridLayout.addView(buttons[i][j]);
            }
        }

        findViewById(R.id.resetButton).setOnClickListener(v -> resetGame());

        speak("It's your turn!");
    }

    private void playerMove(int row, int col) {
        if (!buttons[row][col].getText().toString().equals("")) {
            return;
        }

        buttons[row][col].setText(PLAYER);
        roundCount++;

        if (checkWin(PLAYER)) {
            statusTextView.setText("You Win! 🎉");
            speak("Hurray! You win!");
            disableBoard();
            updateScoreInFirebase(25);  // Add 25 points to score
        } else if (roundCount == 9) {
            statusTextView.setText("It's a Draw! 😐");
            speak("Oh no! It's a draw!");
        } else {
            playerTurn = false;
            statusTextView.setText("AI's Turn...");
            speak("It's my turn now!");
            new Handler().postDelayed(this::aiMove, 500);
        }
    }

    private void updateScoreInFirebase(int points) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            String userId = user.getUid();
            DatabaseReference scoreRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("score");

            // Retrieve the current score first
            scoreRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    int previousScore = 0;
                    if (task.getResult().exists()) {
                        previousScore = task.getResult().getValue(Integer.class);  // Get existing score
                    }

                    int updatedScore = previousScore + points;  // Add new points

                    // Update Firebase with the new total score
                    scoreRef.setValue(updatedScore)
                            .addOnSuccessListener(aVoid ->
                                    Toast.makeText(TicTacToeActivity.this, "Score updated: " + updatedScore, Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e ->
                                    Toast.makeText(TicTacToeActivity.this, "Failed to update score", Toast.LENGTH_SHORT).show());
                }
            });
        }
    }


    private void aiMove() {
        if (roundCount >= 9) return;

        int[] move = findBestMove();
        buttons[move[0]][move[1]].setText(AI);
        roundCount++;

        if (checkWin(AI)) {
            statusTextView.setText("AI Wins! 😢");
            speak("I win! Better luck next time.");
            disableBoard();
        } else {
            playerTurn = true;
            statusTextView.setText("Your Turn!");
            speak("It's your turn!");
        }
    }

    private int[] findBestMove() {
        // 1️⃣ Check for winning move
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (buttons[i][j].getText().toString().equals("")) {
                    buttons[i][j].setText(AI);
                    if (checkWin(AI)) {
                        return new int[]{i, j}; // AI wins!
                    }
                    buttons[i][j].setText("");
                }
            }
        }

        // 2️⃣ Block player's winning move
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (buttons[i][j].getText().toString().equals("")) {
                    buttons[i][j].setText(PLAYER);
                    if (checkWin(PLAYER)) {
                        buttons[i][j].setText(""); // Reset
                        return new int[]{i, j}; // Block player
                    }
                    buttons[i][j].setText("");
                }
            }
        }

        // 3️⃣ Take center if available
        if (buttons[1][1].getText().toString().equals("")) {
            return new int[]{1, 1};
        }

        // 4️⃣ Take a corner if available
        int[][] corners = {{0, 0}, {0, 2}, {2, 0}, {2, 2}};
        for (int[] corner : corners) {
            if (buttons[corner[0]][corner[1]].getText().toString().equals("")) {
                return corner;
            }
        }

        // 5️⃣ Take any empty side
        int[][] sides = {{0, 1}, {1, 0}, {1, 2}, {2, 1}};
        for (int[] side : sides) {
            if (buttons[side[0]][side[1]].getText().toString().equals("")) {
                return side;
            }
        }

        // 6️⃣ Last resort: Random move
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (buttons[i][j].getText().toString().equals("")) {
                    return new int[]{i, j};
                }
            }
        }
        return new int[]{-1, -1}; // Should never reach here
    }

    private boolean checkWin(String player) {
        for (int i = 0; i < 3; i++) {
            if (checkLine(player, buttons[i][0], buttons[i][1], buttons[i][2])) return true;
            if (checkLine(player, buttons[0][i], buttons[1][i], buttons[2][i])) return true;
        }
        return checkLine(player, buttons[0][0], buttons[1][1], buttons[2][2]) ||
                checkLine(player, buttons[0][2], buttons[1][1], buttons[2][0]);
    }

    private boolean checkLine(String player, Button b1, Button b2, Button b3) {
        return b1.getText().toString().equals(player) &&
                b2.getText().toString().equals(player) &&
                b3.getText().toString().equals(player);
    }

    private void disableBoard() {
        for (Button[] row : buttons) {
            for (Button btn : row) {
                btn.setEnabled(false);
            }
        }
    }

    private void resetGame() {
        roundCount = 0;
        playerTurn = true;
        statusTextView.setText("Your Turn!");
        speak("New game started! It's your turn!");

        for (Button[] row : buttons) {
            for (Button btn : row) {
                btn.setText("");
                btn.setEnabled(true);
            }
        }
    }

    private void speak(String text) {
        if (textToSpeech != null) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}