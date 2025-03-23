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
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class SOSActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static final int SMS_PERMISSION_REQUEST_CODE = 101;
    private static final String PREFS_NAME = "EmergencyContacts";
    private static final String EMERGENCY_CONTACT_KEY = "emergency_contact";
    private static final String HELPLINE_NUMBER = "911"; // Replace with actual helpline number
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

        // Load saved emergency contact
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        emergencyContact = prefs.getString(EMERGENCY_CONTACT_KEY, null);

        if (emergencyContact == null) {
            askForEmergencyContact();
        }

        // Panic Button: Triggers Call, SMS, and Location
        panicButton.setOnClickListener(v -> {
            makeEmergencyCall();
            requestSMSPermission(); // Request permission before sending SMS
            requestLocationPermission(); // Request permission before sharing location
        });

        callButton.setOnClickListener(v -> makeEmergencyCall());
        messageButton.setOnClickListener(v -> requestSMSPermission());
        locationButton.setOnClickListener(v -> requestLocationPermission());
    }

    // Ask user for emergency contact number
    private void askForEmergencyContact() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Emergency Contact");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_PHONE);
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            emergencyContact = input.getText().toString().trim();
            if (!emergencyContact.isEmpty()) {
                SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
                editor.putString(EMERGENCY_CONTACT_KEY, emergencyContact);
                editor.apply();
                Toast.makeText(SOSActivity.this, "Emergency contact saved!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(SOSActivity.this, "Invalid contact!", Toast.LENGTH_SHORT).show();
            }
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

    // Request SMS Permission
    private void requestSMSPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_REQUEST_CODE);
        } else {
            sendEmergencyMessage();
        }
    }

    // Send an emergency SMS
    private void sendEmergencyMessage() {
        if (emergencyContact == null || emergencyContact.trim().isEmpty()) {
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
            Log.e("SOSActivity", "SMS Error: " + e.getMessage());
        }
    }

    // Request Location Permission
    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            shareLocation();
        }
    }

    // Share Live Location
    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    private void shareLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null && emergencyContact != null) {
                        String message = "My Live Location: https://www.google.com/maps?q=" + location.getLatitude() + "," + location.getLongitude();
                        try {
                            SmsManager smsManager = SmsManager.getDefault();
                            smsManager.sendTextMessage(emergencyContact, null, message, null, null);
                            Toast.makeText(SOSActivity.this, "Location shared!", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Toast.makeText(SOSActivity.this, "Failed to send location!", Toast.LENGTH_SHORT).show();
                            Log.e("SOSActivity", "Location SMS Error: " + e.getMessage());
                        }
                    } else {
                        Toast.makeText(SOSActivity.this, "Failed to retrieve location or contact not set!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Handle permission request results
    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                shareLocation();
            } else {
                Toast.makeText(this, "Location permission required!", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendEmergencyMessage();
            } else {
                Toast.makeText(this, "SMS permission required!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
