package com.azlan.mindcare.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.azlan.mindcare.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class JournalFragment extends Fragment {
    private EditText editContent;
    private Button btnSave;
    private DatabaseReference journalRef;
    private FirebaseAuth auth;
    private String journalId; // For editing an existing journal

    public JournalFragment() {
        // Required empty constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_journal, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editContent = view.findViewById(R.id.editContent);
        btnSave = view.findViewById(R.id.btnSave);

        auth = FirebaseAuth.getInstance();
        journalRef = FirebaseDatabase.getInstance().getReference("users").child(Objects.requireNonNull(auth.getUid())).child("journal");

        // Get journalId from arguments
        if (getArguments() != null) {
            journalId = getArguments().getString("journalId");
        }

        // If editing, load existing data
        if (journalId != null) {
            loadJournalEntry();
        }

        btnSave.setOnClickListener(v -> saveJournalEntry());
    }

    private void loadJournalEntry() {
        journalRef.child(journalId).get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                editContent.setText(snapshot.child("content").getValue(String.class));
            }
        }).addOnFailureListener(e ->
                Toast.makeText(getContext(), "Error loading entry", Toast.LENGTH_SHORT).show()
        );
    }





    private void saveJournalEntry() {
        String content = editContent.getText().toString().trim();

        if (content.isEmpty()) {
            Toast.makeText(getContext(), "Please fill field", Toast.LENGTH_SHORT).show();
            return;
        }

        if (journalId == null) { // New entry
            journalId = journalRef.push().getKey();
        }

        long timestamp = System.currentTimeMillis(); // Get current timestamp

        Map<String, Object> journalData = new HashMap<>();
        journalData.put("content", content);
        journalData.put("timestamp", timestamp); // Add timestamp field

        journalRef.child(journalId).setValue(journalData).addOnSuccessListener(aVoid -> {
            Toast.makeText(getContext(), "Journal saved", Toast.LENGTH_SHORT).show();

            // ✅ Clear input fields after submission
            editContent.setText("");
            requireActivity().getSupportFragmentManager().popBackStack(); // Navigate back
        }).addOnFailureListener(e ->
                Toast.makeText(getContext(), "Failed to save", Toast.LENGTH_SHORT).show()
        );
    }

}
