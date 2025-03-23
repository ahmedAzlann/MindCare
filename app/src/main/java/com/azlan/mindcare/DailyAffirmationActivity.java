package com.azlan.mindcare;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.airbnb.lottie.LottieAnimationView;

import java.util.Locale;
import java.util.Random;

public class DailyAffirmationActivity extends AppCompatActivity {


    private TextView affirmationText;
    private Button btnNextAffirmation;
    private ImageView btnSpeak,back;
    private TextToSpeech textToSpeech;

    private String[] affirmations = {
            "You are strong and capable!",
            "Believe in yourself and all that you are.",
            "Every day is a new beginning.",
            "You are loved and appreciated.",
            "Your potential is limitless.",
            "You are doing your best, and that is enough.",
            "I am in charge of how I feel today, and I choose happiness.",
            "I am worthy of love and success.",
            "I radiate confidence, self-respect, and inner harmony.",
            "My challenges help me grow stronger.",
            "I am proud of the person I am becoming.",
            "I have the power to create change.",
            "I am grateful for all that I have.",
            "I attract positivity and opportunities into my life.",
            "I am resilient, strong, and brave.",
            "I deserve happiness, love, and success.",
            "I am enough just as I am.",
            "My thoughts and feelings are valid.",
            "I am a magnet for success and happiness.",
            "I choose peace over worry and faith over fear.",
            "I am confident in my abilities and trust the journey of life.",
            "I am filled with creativity and new ideas.",
            "I let go of what I can’t control and focus on what I can.",
            "I deserve to be happy and fulfilled.",
            "My body is healthy, my mind is brilliant, and my soul is at peace.",
            "I am worthy of love, respect, and kindness.",
            "I trust myself to make the right decisions.",
            "I attract positive energy like a magnet.",
            "My potential is endless, and I am capable of achieving greatness.",
            "I choose to focus on the good things in my life.",
            "I am grateful for every experience that shapes me.",
            "Happiness flows through me effortlessly.",
            "I release all doubts and embrace my true potential.",
            "I am in alignment with my purpose and passion.",
            "Every challenge I face is an opportunity to grow.",
            "I trust the timing of my life and have faith in the process.",
            "My dreams are valid, and I have the power to make them real.",
            "I am surrounded by love, positivity, and abundance.",
            "I forgive myself for past mistakes and grow from them.",
            "I wake up each day feeling strong, confident, and empowered.",
            "I am free from worries and trust that everything will work out.",
            "My life is filled with joy, purpose, and abundance.",
            "I choose love, kindness, and compassion in all situations.",
            "Every cell in my body vibrates with positive energy.",
            "I am in control of my emotions and reactions.",
            "I am at peace with my past, present, and future.",
            "I am fearless in the pursuit of my dreams.",
            "I am continuously evolving into a better version of myself."
    };

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_affirmation);


        back = findViewById(R.id.back);
        affirmationText = findViewById(R.id.affirmationText);
        btnNextAffirmation = findViewById(R.id.btnNextAffirmation);
        btnSpeak = findViewById(R.id.btnSpeak);



        back.setOnClickListener(view ->{finish();});
        // Initialize Text-to-Speech
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.US);
                }
            }
        });

        // Display a random affirmation on launch
        showRandomAffirmation();

        // Change Affirmation on Button Click
        btnNextAffirmation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRandomAffirmation();
            }
        });

        // Text-to-Speech for Affirmation
        btnSpeak.setOnClickListener( View -> {
                String text = affirmationText.getText().toString();
                textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        });
    }

    // Function to Show Random Affirmation
    private void showRandomAffirmation() {
        Random random = new Random();
        int index = random.nextInt(affirmations.length);
        affirmationText.setText(affirmations[index]);
    }

    @Override
    protected void onDestroy() {
        // Shutdown Text-to-Speech when activity is destroyed
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}
