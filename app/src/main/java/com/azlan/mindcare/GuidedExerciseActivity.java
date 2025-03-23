package com.azlan.mindcare;

import static java.security.AccessController.getContext;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.List;

public class GuidedExerciseActivity extends AppCompatActivity {
RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_guided_exercise);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(GuidedExerciseActivity.this));

        List<GuidedExercise> exercises = Arrays.asList(
                new GuidedExercise("Deep Breathing", "https://www.youtube.com/watch?v=odADwWzHR24"),
                new GuidedExercise("Progressive Muscle Relaxation", "https://www.youtube.com/watch?v=1nZEdqcGVzo"),
                new GuidedExercise("Mindfulness Meditation", "https://www.youtube.com/watch?v=inpok4MKVLM"),
                new GuidedExercise("Yoga for Stress Relief", "https://www.youtube.com/watch?v=v7AYKMP6rOE"),
                new GuidedExercise("5-Minute Breathing Exercise", "https://www.youtube.com/watch?v=b_LwA7lR9gE"),
                new GuidedExercise("Body Scan Meditation", "https://www.youtube.com/watch?v=6p_yaNFSYao"),
                new GuidedExercise("Box Breathing Technique", "https://www.youtube.com/watch?v=FJJazKtH_9I"),
                new GuidedExercise("Guided Visualization", "https://www.youtube.com/watch?v=so8QN9an3t8"),
                new GuidedExercise("10-Minute Meditation", "https://www.youtube.com/watch?v=O-6f5wQXSu8"),
                new GuidedExercise("Loving-Kindness Meditation", "https://www.youtube.com/watch?v=F3L8UjeVXE4")
        );

        GuidedExerciseAdapter adapter = new GuidedExerciseAdapter(GuidedExerciseActivity.this, exercises);
        recyclerView.setAdapter(adapter);

    }
}