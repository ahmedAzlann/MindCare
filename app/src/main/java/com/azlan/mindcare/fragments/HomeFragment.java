package com.azlan.mindcare.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.azlan.mindcare.DailyAffirmationActivity;
import com.azlan.mindcare.GuidedExerciseActivity;
import com.azlan.mindcare.LoginActivity;
import com.azlan.mindcare.MainActivity;
import com.azlan.mindcare.MoodCameraActivity;
import com.azlan.mindcare.MoodGraphActivity;
import com.azlan.mindcare.R;
import com.azlan.mindcare.SOSActivity;
import com.azlan.mindcare.SleepTrackerActivity;
import com.azlan.mindcare.VoiceRecorder;
import com.azlan.mindcare.FunModeActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HomeFragment extends Fragment {

    ImageView logout;
    FirebaseUser user;
    FirebaseAuth firebaseAuth;
    String currentUserId;
    TextView username, moodText;

    DatabaseReference databaseReference; // Firebase Database Reference

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        user = FirebaseAuth.getInstance().getCurrentUser();
        firebaseAuth = FirebaseAuth.getInstance();
        username = view.findViewById(R.id.welcomeText);
        moodText = view.findViewById(R.id.moodScore);

        initializeProfile();
        fetchUserScore(); // Fetch the score from Firebase


        view.findViewById(R.id.guidedexercise).setOnClickListener(this::onExerciseClick);
        view.findViewById(R.id.affirmation).setOnClickListener(this::onAffirmationClick);
//        view.findViewById(R.id.graph).setOnClickListener(this::onGraphClick);
        view.findViewById(R.id.camera).setOnClickListener(this::onCameraClick);
        view.findViewById(R.id.sleepTracker).setOnClickListener(this::onSleepTrackerClick);
        view.findViewById(R.id.voiceAssistant).setOnClickListener(this::onVoiceAssistantClick);
        view.findViewById(R.id.funMode).setOnClickListener(this::onFunModeClick);
        view.findViewById(R.id.bot).setOnClickListener(this::onBotClick);
        view.findViewById(R.id.sos).setOnClickListener(this::onSosClick);
    }

    private void onSosClick(View view) {
        startActivity(new Intent(requireContext(), SOSActivity.class));
    }


    private void onBotClick(View view) {
        startActivity(new Intent(requireContext(), MainActivity.class));
    }
    private void openLoginActivity() {
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private void initializeProfile() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            currentUserId = user.getUid();
            FirebaseDatabase.getInstance().getReference("Users").child(currentUserId).child("username")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                String name = snapshot.getValue(String.class);
                                username.setText("Hi, " + name + " 👋");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("Firebase", "Failed to fetch username", error.toException());
                        }
                    });
        }
    }

    private void fetchUserScore() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            currentUserId = user.getUid();
            databaseReference = FirebaseDatabase.getInstance().getReference("users").child(currentUserId);

            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        int score = snapshot.child("score").getValue(Integer.class) != null ? snapshot.child("score").getValue(Integer.class) : 0;
                        String lastActiveDate = snapshot.child("lastActiveDate").getValue(String.class);

                        String todayDate = getCurrentDate(); // Get today's date

                        if (lastActiveDate == null || !lastActiveDate.equals(todayDate)) {
                            // If the user is opening the app for the first time today, add 10 points
                            int newScore = score + 10;
                            databaseReference.child("score").setValue(newScore);
                            databaseReference.child("lastActiveDate").setValue(todayDate);
                            moodText.setText("Your Current Mood Score: " + newScore + " ⭐");

                            // Show a toast message
                            Toast.makeText(requireContext(), "🎉 Hurray! Daily points added!", Toast.LENGTH_SHORT).show();
                        } else {
                            moodText.setText("Your Current Mood Score: " + score + " ⭐");
                        }
                    } else {
                        moodText.setText("Your Current Mood Score: 0 ⭐");
                        databaseReference.child("score").setValue(0);
                        databaseReference.child("lastActiveDate").setValue(getCurrentDate());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("Firebase", "Failed to fetch score", error.toException());
                }
            });
        }
    }

    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }


//    private void updateMoodText(int score) {
//
//
//    }

    private void onExerciseClick(View view) {
        startActivity(new Intent(requireContext(), GuidedExerciseActivity.class));
    }

    private void onAffirmationClick(View view) {
        startActivity(new Intent(requireContext(), DailyAffirmationActivity.class));
    }

    private void onGraphClick(View view) {
        startActivity(new Intent(requireContext(), MoodGraphActivity.class));
    }

    private void onCameraClick(View view) {
        startActivity(new Intent(requireContext(), MoodCameraActivity.class));
    }

    private void onSleepTrackerClick(View view) {
        startActivity(new Intent(requireContext(), SleepTrackerActivity.class));
    }

    private void onVoiceAssistantClick(View view) {
        startActivity(new Intent(requireContext(), VoiceRecorder.class));
    }

    private void onFunModeClick(View view) {
        startActivity(new Intent(requireContext(), FunModeActivity.class));
    }
    

}
