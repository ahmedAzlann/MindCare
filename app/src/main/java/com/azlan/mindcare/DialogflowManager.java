package com.azlan.mindcare;

import android.content.Context;
import android.util.Log;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.dialogflow.v2.*;
import java.io.InputStream;
import java.util.List;

public class DialogflowManager {
    private static final String TAG = "DialogflowManager";
    private SessionsClient sessionsClient;
    private SessionName session;


    public DialogflowManager(Context context) {
        try {
            Log.d("Dialogflowconstructor", "Initializing Dialogflow...");

            // Load JSON key from raw folder
            InputStream stream = context.getResources().openRawResource(R.raw.dialogflow_key);
            GoogleCredentials credentials = GoogleCredentials.fromStream(stream)
                    .createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));

            SessionsSettings sessionsSettings = SessionsSettings.newBuilder()
                    .setCredentialsProvider(() -> credentials)
                    .build();

            sessionsClient = SessionsClient.create(sessionsSettings);
            Log.d("Dialogflowconstructor", "SessionsClient initialized.");

            String projectId = "healthbot-lasm"; // Replace with your actual Dialogflow project ID
            String sessionId = "12345";  // Unique session ID
            session = SessionName.of(projectId, sessionId);
            Log.d("Dialogflowconstructor", "Session initialized: " + session.toString());

        } catch (Exception e) {
            Log.e("Dialogflowconstructor", "Error initializing Dialogflow", e);
        }
    }




    public String sendMessage(String userMessage) {
        try {
            if (session == null) {
                Log.e("Dialogflow", "Error: session is null. Dialogflow initialization failed.");
                return "Error: Chatbot session not initialized.";
            }

            // Create request
            TextInput.Builder textInput = TextInput.newBuilder()
                    .setText(userMessage)
                    .setLanguageCode("en");
            QueryInput queryInput = QueryInput.newBuilder().setText(textInput).build();

            DetectIntentRequest request = DetectIntentRequest.newBuilder()
                    .setSession(session.toString())
                    .setQueryInput(queryInput)
                    .build();

            // Send request and get response
            DetectIntentResponse response = sessionsClient.detectIntent(request);
            return response.getQueryResult().getFulfillmentText();
        } catch (Exception e) {
            Log.e("Dialogflow", "Error sending message to Dialogflow", e);
            return "Error connecting to chatbot.";
        }
    }


}

