package com.example.studdy;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class FacultyCodeRegistrationActivity extends AppCompatActivity {

    private ImageView backArrow, passwordToggle;
    private EditText usernameEditText, phoneEditText, passwordEditText;
    private RadioGroup roleRadioGroup;
    private AppCompatButton signUpButton, goToSignInButton;
    private boolean isPasswordVisible = false;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faculty_code_registration);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize views
        backArrow = findViewById(R.id.backButton);
        roleRadioGroup = findViewById(R.id.roleRadioGroup);
        usernameEditText = findViewById(R.id.usernameEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        passwordToggle = findViewById(R.id.passwordToggle);
        signUpButton = findViewById(R.id.signUpButton);

        // Back arrow click
        backArrow.setOnClickListener(v -> {
            finish(); // Go back to the previous activity (likely LoginActivity)
        });

        // Toggle password visibility
        passwordToggle.setOnClickListener(v -> {
            if (isPasswordVisible) {
                passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                passwordToggle.setImageResource(R.drawable.ic_eye); // Closed eye
                isPasswordVisible = false;
            } else {
                passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                passwordToggle.setImageResource(R.drawable.ic_eye); // Open eye (update if you have a different icon)
                isPasswordVisible = true;
            }
            // Move cursor to the end of the text
            passwordEditText.setSelection(passwordEditText.getText().length());
        });

        //phone number valid check
        phoneEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        if (phoneEditText.length() != 10) {
            phoneEditText.setError("Enter a valid 10-digit phone number");
            return;
        }



        // Sign up button click
        signUpButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String phone = phoneEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            // Basic validation
            if (username.isEmpty()) {
                usernameEditText.setError("Username is required");
                return;
            }

            if (phone.isEmpty()) {
                phoneEditText.setError("Phone number is required");
                return;
            }

            if (phone.length() < 10) {
                phoneEditText.setError("Phone number must be at least 10 digits");
                return;
            }

            if (password.isEmpty()) {
                passwordEditText.setError("Password is required");
                return;
            }

            // Check if username or phone number already exists in Firestore
            checkFacultyExists(username, phone, () -> {
                // If credentials are unique, proceed with registration
                String staffCode = generateStaffCode(); // Generate a random staff code
                saveFacultyToFirestore(username, phone, password, staffCode);
            });
        });
    }

    // Method to check if username or phone number already exists in Firestore
    private void checkFacultyExists(String username, String phone, Runnable onSuccess) {
        db.collection("faculty")
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot usernameSnapshot = task.getResult();
                        if (usernameSnapshot != null && !usernameSnapshot.isEmpty()) {
                            usernameEditText.setError("Username already exists");
                            return;
                        }

                        // Check phone number
                        db.collection("faculty")
                                .whereEqualTo("phone", phone)
                                .get()
                                .addOnCompleteListener(phoneTask -> {
                                    if (phoneTask.isSuccessful()) {
                                        QuerySnapshot phoneSnapshot = phoneTask.getResult();
                                        if (phoneSnapshot != null && !phoneSnapshot.isEmpty()) {
                                            phoneEditText.setError("Phone number already exists");
                                        } else {
                                            // If both username and phone are unique, proceed
                                            onSuccess.run();
                                        }
                                    } else {
                                        Toast.makeText(FacultyCodeRegistrationActivity.this,
                                                "Error checking phone number: " + phoneTask.getException().getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(FacultyCodeRegistrationActivity.this,
                                "Error checking username: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Method to save faculty data to Firestore
    private void saveFacultyToFirestore(String username, String phone, String password, String staffCode) {
        Map<String, Object> faculty = new HashMap<>();
        faculty.put("username", username);
        faculty.put("phone", phone);
        faculty.put("password", password); // Note: In a real app, password should be hashed
        faculty.put("staff_code", staffCode);

        db.collection("faculty")
                .add(faculty)
                .addOnSuccessListener(documentReference -> {
                    // Registration successful, send email (placeholder) and show popup
                    sendStaffCodeEmail(username, staffCode); // Placeholder for email sending
                    showSuccessPopup(staffCode);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(FacultyCodeRegistrationActivity.this,
                            "Registration failed: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    // Method to generate a random staff code
    private String generateStaffCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        for (int i = 0; i < 6; i++) {
            code.append(characters.charAt(random.nextInt(characters.length())));
        }
        return code.toString();
    }

    // Placeholder method for sending email
    private void sendStaffCodeEmail(String username, String staffCode) {
        // TODO: Implement actual email sending logic (e.g., using Firebase Functions, SendGrid, or JavaMail API)
        // For now, this is a placeholder
        System.out.println("Sending email to " + username + " with staff code: " + staffCode);
    }

    // Method to show success popup and navigate to LoginActivity
    private void showSuccessPopup(String staffCode) {
        new AlertDialog.Builder(this)
                .setTitle("Registration Successful")
                .setMessage("Generated staff code sent on your email")
                .setPositiveButton("OK", (dialog, which) -> {
                    // Navigate to LoginActivity
                    startActivity(new Intent(FacultyCodeRegistrationActivity.this, LoginActivity.class));
                    finish();
                })
                .setCancelable(false)
                .show();
    }
}