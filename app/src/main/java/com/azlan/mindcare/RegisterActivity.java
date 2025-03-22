package com.azlan.mindcare;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {
    ImageView backButton;
    HashMap<String,Object> profiledata;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    private EditText fullNameInput, emailInput, phoneInput, passwordInput, confirmPasswordInput;
    private CheckBox termsCheckbox;
    private Button signupButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        EdgeToEdge.enable(this);
        // Initializing views

        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = firebaseDatabase.getReference("users");
        fullNameInput = findViewById(R.id.fullNameInput);
        emailInput = findViewById(R.id.emailInput);
        phoneInput = findViewById(R.id.phoneInput);
        passwordInput = findViewById(R.id.passwordInput);
        profiledata = new HashMap<>();
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        termsCheckbox = findViewById(R.id.termsCheckbox);
        signupButton = findViewById(R.id.signupButton);
        backButton = findViewById(R.id.back);
        backButton.setOnClickListener(v -> {
            finish();
        });

        // Handling Sign-Up Button Click
        signupButton.setOnClickListener(view -> validateAndRegister());
    }

    private void validateAndRegister() {
        String fullName = fullNameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        // Check Full Name
        if (fullName.isEmpty()) {
            showToast("Please enter your full name");
            return;
        }

        // Check Email
        if (email.isEmpty() || !isValidEmail(email)) {
            showToast("Please enter a valid email");
            return;
        }

        // Check Phone Number
        if (phone.isEmpty() || !isValidPhone(phone)) {
            showToast("Please enter a valid 10-digit phone number");
            return;
        }


        // Check Password
        if (password.isEmpty() || password.length() < 6) {
            showToast("Password must be at least 6 characters");
            return;
        }

        // Check Confirm Password
        if (!password.equals(confirmPassword)) {
            showToast("Passwords do not match");
            return;
        }

        // Check Terms & Conditions
        if (!termsCheckbox.isChecked()) {
            showToast("Please agree to the terms and conditions");
            return;
        }

        // All validations passed, display input values
        String message = "Full Name: " + fullName + "\n"
                + "Email: " + email + "\n"
                + "Phone: " + phone + "\n";


        fetchUserFromEmail(fullName,email,password,phone);

    }

    // Function to validate email
    private boolean isValidEmail(String email) {
        String emailPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
        return Pattern.matches(emailPattern, email);
    }

    // Function to validate phone number
    private boolean isValidPhone(String phone) {
        return phone.length() == 10 && phone.matches("\\d+");
    }

    // Function to show a toast message
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void fetchUserFromEmail(String name, String email, String password, String phone){

            firebaseAuth.fetchSignInMethodsForEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            List<String> users = task.getResult().getSignInMethods();
                            if (users != null) {
                                if (!users.isEmpty()) {
                                    Toast.makeText(RegisterActivity.this, "User Already exist", Toast.LENGTH_SHORT).show();
                                } else {
                                    registerNewUser(name, email, password, phone);
                                }
                            }
                        }
                    }).addOnFailureListener(e -> {
                        Log.d("firebase", "fetchUserFromEmail: "+e.getMessage());
                    });

    }

    private void registerNewUser(String name, String email, String password, String phone){
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String userId = Objects.requireNonNull(task.getResult().getUser()).getUid();
                        profiledata.put("name",name);
                        profiledata.put("email",email);
                        profiledata.put("password",password);
                        profiledata.put("phone",phone);
                        profiledata.put("userid",userId);
                        addUserToDB(profiledata);
                        //Insert to db
                    } else {
                        Toast.makeText(RegisterActivity.this, "Register failed", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("registernewuser", "onFailure: "+e.getMessage());
                    }
                });
    }

    private void addUserToDB(HashMap<String,Object> profiledata){
        databaseReference.child(Objects.requireNonNull(profiledata.get("userid")).toString()).child("profile").setValue(profiledata)
                .addOnCompleteListener(task -> {
                    Toast.makeText(RegisterActivity.this, "Registered Successfully!!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                }).addOnFailureListener(e -> {
                    Toast.makeText(RegisterActivity.this, "Database failed", Toast.LENGTH_SHORT).show();
                });
    }
}
