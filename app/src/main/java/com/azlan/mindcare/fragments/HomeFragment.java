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
import com.azlan.mindcare.MainActivity;
import com.azlan.mindcare.MoodCameraActivity;
import com.azlan.mindcare.MoodGraphActivity;
import com.azlan.mindcare.R;
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
        initializeprofile();

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

    }

    private void openLoginActivity() {
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private void initializeprofile(){


        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            currentUserId = user.getUid();
            // Firebase data fetching logic inside onViewCreated
            FirebaseDatabase.getInstance().getReference("users").child(currentUserId).child("profile").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String Name = snapshot.child("name").getValue(String.class);
                        if (Name != null) {
                            setuserdata(Name);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.d("profileinitialization", "onCancelled: " + error.getMessage());
                }
            });
        }
    }

    private void setuserdata(String name){
        username.setText("Hi, "+name+" \uD83D\uDC4B");

    }



    public void onCameraClick(@Nullable View view) {
        startActivity(new Intent(getContext(), MoodCameraActivity.class));
    }


    public void onAffirmationClick(@Nullable View view) {
        startActivity(new Intent(getContext(), DailyAffirmationActivity.class));
    }

    public void onExerciseClick(@Nullable View view) {
        startActivity(new Intent(getContext(), GuidedExerciseActivity.class));
    }

    public void onGraphClick(@Nullable View view) {
        startActivity(new Intent(getContext(), MoodGraphActivity.class));
    }
}