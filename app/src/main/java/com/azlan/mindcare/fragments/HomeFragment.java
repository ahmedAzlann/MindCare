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

import com.azlan.mindcare.DailyAffirmationActivity;
import com.azlan.mindcare.GuidedExerciseActivity;
import com.azlan.mindcare.LoginActivity;
import com.azlan.mindcare.MoodCameraActivity;
import com.azlan.mindcare.MoodGraphActivity;
import com.azlan.mindcare.R;
import com.azlan.mindcare.SleepTrackerActivity;
import com.azlan.mindcare.VoiceRecorder;
import com.azlan.mindcare.FunModeActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeFragment extends Fragment {

    ImageView logout;
    FirebaseUser user;
    FirebaseAuth firebaseAuth;
    String currentUserId;
    TextView username;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        logout = view.findViewById(R.id.logout);
        user = FirebaseAuth.getInstance().getCurrentUser();
        firebaseAuth = FirebaseAuth.getInstance();
        username = view.findViewById(R.id.welcomeText);
        initializeProfile();

        logout.setOnClickListener(v -> new MaterialAlertDialogBuilder(v.getContext())
                .setTitle("MindCare")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    firebaseAuth.signOut();
                    openLoginActivity();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show());

        view.findViewById(R.id.guidedexercise).setOnClickListener(this::onExerciseClick);
        view.findViewById(R.id.affirmation).setOnClickListener(this::onAffirmationClick);
        view.findViewById(R.id.graph).setOnClickListener(this::onGraphClick);
        view.findViewById(R.id.camera).setOnClickListener(this::onCameraClick);
        view.findViewById(R.id.sleepTracker).setOnClickListener(this::onSleepTrackerClick);
        view.findViewById(R.id.voiceAssistant).setOnClickListener(this::onVoiceAssistantClick);
        view.findViewById(R.id.funMode).setOnClickListener(this::onFunModeClick);
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
