package com.azlan.mindcare;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.tasks.Task;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class SleepTrackerActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_SIGN_IN = 1001;
    private TextView sleepStatus;
    private boolean isYesterday = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep_tracker);

        sleepStatus = findViewById(R.id.sleep_status);  // Add this TextView in your XML

        isYesterday = true;

        signInToGoogleFit();
    }

    private void signInToGoogleFit() {
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(Fitness.SCOPE_ACTIVITY_READ)
                .build();

        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, REQUEST_CODE_SIGN_IN);
        Log.d("GoogleFit", "Sign-In Intent started.");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SIGN_IN) {
            GoogleSignIn.getSignedInAccountFromIntent(data)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            GoogleSignInAccount signedInAccount = task.getResult();
                            Log.d("GoogleFit", "Sign-In successful.");
                            retrieveSleepData(signedInAccount);
                        } else {
                            Exception exception = task.getException();
                            Log.e("GoogleSignIn", "Sign-In failed: " + exception.getMessage());
                            Toast.makeText(SleepTrackerActivity.this, "Sign-In failed: " + exception.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    private void retrieveSleepData(GoogleSignInAccount signedInAccount) {
        if (signedInAccount == null) {
            Log.e("GoogleSignIn", "Signed-in account is null.");
            Toast.makeText(this, "Google Sign-In failed, please try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        FitnessOptions fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_SLEEP_SEGMENT, FitnessOptions.ACCESS_READ)
                .build();
        Log.d("GoogleFit", "Fitness options built for sleep segment data.");

        if (!GoogleSignIn.hasPermissions(signedInAccount, fitnessOptions)) {
            Log.d("GoogleFit", "Permissions not granted. Requesting permissions.");
            GoogleSignIn.requestPermissions(this, 1, signedInAccount, fitnessOptions);
        } else {
            Log.d("GoogleFit", "Permissions already granted. Retrieving sleep data.");
            querySleepData(signedInAccount, fitnessOptions);
        }
    }

    private void querySleepData(GoogleSignInAccount signedInAccount, FitnessOptions fitnessOptions) {
        Calendar calendar = Calendar.getInstance();
        long endTime = calendar.getTimeInMillis();

        if (isYesterday) {
            calendar.add(Calendar.DAY_OF_YEAR, -1);
        }

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startTime = calendar.getTimeInMillis();

        DataReadRequest readRequest = new DataReadRequest.Builder()
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .read(DataType.TYPE_SLEEP_SEGMENT)
                .build();
        Log.d("GoogleFit", "DataReadRequest created with time range: " + startTime + " to " + endTime);

        Task<DataReadResponse> response = Fitness.getHistoryClient(this, signedInAccount)
                .readData(readRequest);

        response.addOnSuccessListener(dataReadResponse -> {
            Log.d("GoogleFit", "Data retrieval successful.");
            long totalSleepDuration = 0;

            if (dataReadResponse.getDataSets().isEmpty()) {
                sleepStatus.setText("No sleep data found.");
//                scoreTextView.setText("Score: 0");
                Log.d("GoogleFit", "No sleep data found in response.");
                return;
            }

            for (DataSet dataSet : dataReadResponse.getDataSets()) {
                Log.d("GoogleFit", "DataSet: " + dataSet);
                for (DataPoint dataPoint : dataSet.getDataPoints()) {
                    long startTimePoint = dataPoint.getStartTime(TimeUnit.MILLISECONDS);
                    long endTimePoint = dataPoint.getEndTime(TimeUnit.MILLISECONDS);
                    long duration = endTimePoint - startTimePoint;
                    totalSleepDuration += duration;

                    Log.d("GoogleFit", "Sleep segment: start=" + startTimePoint + ", end=" + endTimePoint + ", duration=" + duration);
                }
            }

            if (totalSleepDuration > 0) {
                long totalSleepDurationInHours = totalSleepDuration / (1000 * 60 * 60);
                sleepStatus.setText("Total Sleep: " + totalSleepDurationInHours + " hours");
                int sleepScore = calculateSleepScore(totalSleepDurationInHours);
//                scoreTextView.setText("Score: " + sleepScore);
                Log.d("GoogleFit", "Total Sleep: " + totalSleepDurationInHours + " hours, Score: " + sleepScore);
            } else {
                sleepStatus.setText("No sleep data found.");
//                scoreTextView.setText("Score: 0");
                Log.d("GoogleFit", "retrieveSleepData: empty");
            }

        }).addOnFailureListener(e -> {
            Log.e("GoogleFit", "Error retrieving sleep data: " + e.getMessage());
            Toast.makeText(SleepTrackerActivity.this, "Failed to retrieve sleep data.", Toast.LENGTH_SHORT).show();
        });
    }

    private int calculateSleepScore(long hours) {
        if (hours < 4) {
            return 10;
        } else if (hours < 7) {
            return 20;
        } else {
            return 30;
        }
    }
}
