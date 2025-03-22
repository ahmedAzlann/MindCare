package com.azlan.mindcare;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private DialogflowManager dialogflowManager;
    private LinearLayout chatContainer;
    private EditText userInput;
    private ScrollView chatScrollView;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dialogflowManager = new DialogflowManager(this);
        chatContainer = findViewById(R.id.chatContainer);
        userInput = findViewById(R.id.userInput);
        ImageView sendButton = findViewById(R.id.sendButton);
        ImageView backButton = findViewById(R.id.backButton);
        chatScrollView = findViewById(R.id.chatScrollView);

        backButton.setOnClickListener(v -> finish());



        sendButton.setOnClickListener(v -> {
            String message = userInput.getText().toString();
            if (!message.isEmpty()) {
                addMessageToChat("You: " + message);
                userInput.setText("");

                // Send message to Dialogflow and get response
                new Thread(() -> {
                    String botResponse = dialogflowManager.sendMessage(message);
                    runOnUiThread(() -> {
                        Log.d("Botmessage", "reply: " + botResponse);

                        // Create TextView for bot response
                        TextView botMessageView = new TextView(MainActivity.this);
                        botMessageView.setText("Bot: " + botResponse);
                        botMessageView.setAutoLinkMask(Linkify.WEB_URLS);  // Enable clickable links
                        botMessageView.setMovementMethod(LinkMovementMethod.getInstance());  // Make links clickable
                        botMessageView.setPadding(10, 10, 10, 10);
                        botMessageView.setTextSize(16);

                        // Add to chat container
                        LinearLayout chatContainer = findViewById(R.id.chatContainer);
                        chatContainer.addView(botMessageView);
                    });

                }).start();
            }
        });
    }

    private void addMessageToChat(String message) {
        TextView textView = new TextView(this);
        textView.setText(message);
        textView.setPadding(16, 8, 16, 8);
        textView.setTextSize(16);
        textView.setTextColor(getResources().getColor(android.R.color.black));
        chatContainer.addView(textView);
        chatScrollView.post(() -> chatScrollView.fullScroll(ScrollView.FOCUS_DOWN));
    }

}

