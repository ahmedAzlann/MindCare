package com.azlan.mindcare;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class SOSActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final String PREFS_NAME = "EmergencyContacts";
    private static final String EMERGENCY_CONTACT_KEY = "emergency_contact";
    private static final String HELPLINE_NUMBER = "911"; // Update with actual helpline
    private FusedLocationProviderClient fusedLocationClient;
    private String emergencyContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        Button panicButton = findViewById(R.id.btn_panic);
        Button callButton = findViewById(R.id.btn_call);
        Button messageButton = findViewById(R.id.btn_message);
        Button locationButton = findViewById(R.id.btn_location);

        // Load saved emergency contact or ask for a new one
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        emergencyContact = prefs.getString(EMERGENCY_CONTACT_KEY, null);

        if (emergencyContact == null) {
            askForEmergencyContact();
        }

        // Panic Button: Trigger all actions
        panicButton.setOnClickListener(v -> {
            makeEmergencyCall();
            sendEmergencyMessage();
            shareLocation();
        });

        // Emergency Call
        callButton.setOnClickListener(v -> makeEmergencyCall());

        // Emergency Message
        messageButton.setOnClickListener(v -> sendEmergencyMessage());

        // Share Live Location
        locationButton.setOnClickListener(v -> shareLocation());
    }

    // Ask user for emergency contact number
    private void askForEmergencyContact() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Emergency Contact");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_PHONE);
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            emergencyContact = input.getText().toString();
            SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
            editor.putString(EMERGENCY_CONTACT_KEY, emergencyContact);
            editor.apply();
            Toast.makeText(SOSActivity.this, "Emergency contact saved!", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    // Make an emergency call
    private void makeEmergencyCall() {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + HELPLINE_NUMBER));
        startActivity(intent);
    }

    // Send an emergency SMS
    private void sendEmergencyMessage() {
        if (emergencyContact == null) {
            askForEmergencyContact();
            return;
        }
        String message = "URGENT: I am in distress. Please help!";
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(emergencyContact, null, message, null, null);
            Toast.makeText(this, "Emergency message sent!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to send message!", Toast.LENGTH_SHORT).show();
        }
    }

    // Share Live Location
    private void shareLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null && emergencyContact != null) {
                            String message = "My Live Location: https://www.google.com/maps?q=" + location.getLatitude() + "," + location.getLongitude();
                            SmsManager smsManager = SmsManager.getDefault();
                            smsManager.sendTextMessage(emergencyContact, null, message, null, null);
                            Toast.makeText(SOSActivity.this, "Location shared!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(SOSActivity.this, "Failed to retrieve location or contact not set!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Handle permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            shareLocation();
        } else {
            Toast.makeText(this, "Location permission required!", Toast.LENGTH_SHORT).show();
        }
    }
}
