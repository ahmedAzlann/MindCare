package com.azlan.mindcare;

import static androidx.activity.EdgeToEdge.*;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.azlan.mindcare.fragments.JournalFragment;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.List;

public class JournalEntriesActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private MaterialButton btnAddJournal;
    private JournalAdapter journalAdapter;
    private List<JournalEntry> journalList;
    private DatabaseReference journalRef;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enable(this);
        setContentView(R.layout.activity_journal_entries);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.recyclerViewJournals);
        btnAddJournal = findViewById(R.id.btnAddJournal);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        journalList = new ArrayList<JournalEntry>();
        journalAdapter = new JournalAdapter(journalList, this);
        recyclerView.setAdapter(journalAdapter);

        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        journalRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid()).child("journal");

        fetchJournals();

        btnAddJournal.setOnClickListener(v -> {
            finish();
//            JournalFragment journalFragment = new JournalFragment();
//
//            // If you want to pass any data, use a Bundle
//            Bundle bundle = new Bundle();
//            bundle.putString("journalId", null); // New journal entry, so no ID yet
//            journalFragment.setArguments(bundle);
//
//            getSupportFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.container, journalFragment) // Ensure this ID is correct
//                    .addToBackStack(null) // Enables back navigation
//                    .commit();
        });
    }


    private void fetchJournals() {
        journalRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                journalList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    String id = data.getKey();
                    String content = data.child("content").getValue(String.class);

                    // Fix: Handle null timestamp
                    Long timestampObj = data.child("timestamp").getValue(Long.class);
                    long timestamp = (timestampObj != null) ? timestampObj : System.currentTimeMillis();

                    JournalEntry journal = new JournalEntry(id, content, timestamp);
                    journalList.add(journal);
                }
                journalAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }


}
