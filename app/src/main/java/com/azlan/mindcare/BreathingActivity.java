package com.azlan.mindcare;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class BreathingActivity extends AppCompatActivity {

    private TextView instructionText;
    private ImageView breathingCircle;
    private Button startButton;
    private SeekBar durationSeekBar;
    private Spinner musicSpinner;
    private TextToSpeech textToSpeech;
    private MediaPlayer mediaPlayer;
    private int sessionDuration = 3; // Default: 3 min
    private boolean isBreathing = false;

    // Custom Song Options
    private String[] songOptions = {"Ocean Waves", "Rain Sounds", "Calm Piano", "Forest Ambience", "Meditation Flute"};
    private int[] songResources = {R.raw.ocean_waves, R.raw.rain_sounds, R.raw.calm_piano, R.raw.forest_ambience, R.raw.meditation_flute};
    private int selectedMusic = R.raw.ocean_waves; // Default Music

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_breathing);

        instructionText = findViewById(R.id.txt_instruction);
        breathingCircle = findViewById(R.id.img_breathing_circle);
        startButton = findViewById(R.id.btn_start);
        durationSeekBar = findViewById(R.id.seek_duration);
        musicSpinner = findViewById(R.id.spinner_music);

        // Initialize Text-to-Speech
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.US);
            }
        });

        // Populate Music Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, songOptions);
        musicSpinner.setAdapter(adapter);

        // Music Selection Listener
        musicSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedMusic = songResources[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Session Duration Selection
        durationSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                sessionDuration = (progress + 1) * 2; // 2, 4, 6 minutes
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Start Button Click
        startButton.setOnClickListener(v -> {
            if (!isBreathing) {
                startBreathingSession();
            } else {
                stopBreathingSession();
            }
        });
    }

    private void startBreathingSession() {
        isBreathing = true;
        mediaPlayer = MediaPlayer.create(this, selectedMusic);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        startButton.setText("Stop");
        guideBreathingCycle(0);
    }

    private void stopBreathingSession() {
        isBreathing = false;
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        startButton.setText("Start");
        instructionText.setText("Session Stopped");
    }

    private void guideBreathingCycle(int step) {
        if (!isBreathing || step >= sessionDuration * 3) {
            stopBreathingSession();
            return;
        }

        String[] steps = {"Inhale deeply...", "Hold your breath...", "Exhale slowly..."};
        int[] durations = {4000, 3000, 5000}; // 4s Inhale, 3s Hold, 5s Exhale

        instructionText.setText(steps[step % 3]);
        textToSpeech.speak(steps[step % 3], TextToSpeech.QUEUE_FLUSH, null, null);

        animateBreathing(step % 3);

        instructionText.postDelayed(() -> guideBreathingCycle(step + 1), durations[step % 3]);
    }

    private void animateBreathing(int phase) {
        float scale = (phase == 0) ? 1.5f : 1.0f;

        ObjectAnimator scaleAnimator = ObjectAnimator.ofPropertyValuesHolder(
                breathingCircle,
                PropertyValuesHolder.ofFloat(View.SCALE_X, scale),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, scale)
        );
        scaleAnimator.setDuration(2000);
        scaleAnimator.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (textToSpeech != null) {
            textToSpeech.shutdown();
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }
}
