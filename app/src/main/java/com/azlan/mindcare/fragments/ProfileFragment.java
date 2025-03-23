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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.azlan.mindcare.AboutUsActivity;
import com.azlan.mindcare.JournalEntriesActivity;
import com.azlan.mindcare.LoginActivity;
import com.azlan.mindcare.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileFragment extends Fragment {

TextView username,useremail,yourJournalText,notificationpreference,chatbotpreference,emergencyContact,about;
    Button logout;
    FirebaseUser user;
    FirebaseAuth firebaseAuth;
    String currentUserId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.yourJournalText).setOnClickListener(View ->{
            startActivity(new Intent(getContext(), JournalEntriesActivity.class));
        });
    //    view.findViewById(R.id.notificationpreference).setOnClickListener(v -> {startActivity(new Intent(getContext(),NotificationSettingsActivity.class))});
     //   view.findViewById(R.id.chatbotpreference).setOnClickListener(v -> {startActivity(new Intent(getContext(),ChatbotSettingsActivity.class))});
        // view.findViewById(R.id.emergencyContact).setOnClickListener(v -> {startActivity(new Intent(getContext(),EmergencyContactsActivity.class))});
         view.findViewById(R.id.about).setOnClickListener(v -> {startActivity(new Intent(getContext(), AboutUsActivity.class));});
        logout = view.findViewById(R.id.btnLogout);
        user = FirebaseAuth.getInstance().getCurrentUser();
        firebaseAuth = FirebaseAuth.getInstance();
        username = view.findViewById(R.id.userName);
        useremail = view.findViewById(R.id.userEmail);
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
                        String email = snapshot.child("email").getValue(String.class);
                        if (Name != null && email != null) {
                            setuserdata(Name,email);
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

    private void setuserdata(String name,String email){
        username.setText(name);
        useremail.setText(email);

    }
}