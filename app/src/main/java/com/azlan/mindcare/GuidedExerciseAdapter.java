package com.azlan.mindcare;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GuidedExerciseAdapter extends RecyclerView.Adapter<GuidedExerciseAdapter.ViewHolder> {
    private final List<GuidedExercise> exerciseList;
    private final Context context;

    public GuidedExerciseAdapter(Context context, List<GuidedExercise> exerciseList) {
        this.context = context;
        this.exerciseList = exerciseList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.guided_exercise_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GuidedExercise exercise = exerciseList.get(position);
        holder.exerciseTitle.setText(exercise.getTitle());
        holder.exerciseLink.setText("Watch on YouTube");

        holder.exerciseLink.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(exercise.getLink()));
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return exerciseList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView exerciseTitle, exerciseLink;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            exerciseTitle = itemView.findViewById(R.id.exerciseTitle);
            exerciseLink = itemView.findViewById(R.id.exerciseLink);
        }
    }
}
