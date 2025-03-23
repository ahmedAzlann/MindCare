package com.azlan.mindcare;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.azlan.mindcare.fragments.JournalFragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class JournalAdapter extends RecyclerView.Adapter<JournalAdapter.ViewHolder> {
    private List<JournalEntry> journalList;
    private Context context;

    public JournalAdapter(List<JournalEntry> journalList, Context context) {
        this.journalList = journalList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_journal, parent, false);
        return new ViewHolder(view);
    }

    @Override

    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JournalEntry journal = journalList.get(position);
        holder.Entry.setText(journal.getContent());

        // Convert timestamp to readable date
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
        String formattedDate = sdf.format(new Date(journal.getTimestamp()));

        holder.date.setText(formattedDate);

        holder.itemView.setOnClickListener(v -> {
            JournalFragment journalFragment = new JournalFragment();
            Bundle bundle = new Bundle();
            bundle.putString("journalId", journal.getId());
            journalFragment.setArguments(bundle);

            ((AppCompatActivity) context).getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, journalFragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    @Override
    public int getItemCount() {
        return journalList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView Entry, date;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            Entry = itemView.findViewById(R.id.journalEntry);
            date = itemView.findViewById(R.id.journalDate);
        }
    }
}

