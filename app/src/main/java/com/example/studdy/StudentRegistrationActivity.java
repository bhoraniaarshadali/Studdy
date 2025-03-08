package com.example.studdy;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public class StudentRegistrationActivity extends AppCompatActivity {

    private static final String TAG = "StudentRegistration"; // For logging
    // Precompiled regular expressions for validation
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$"); // Alphanumeric + underscore, 3-20 characters
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10}$"); // Exactly 10 digits
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{6,}$");

    private ImageView backArrow, passwordToggle;
    private EditText usernameEditText, emailEditText, phoneEditText, passwordEditText;
    private AppCompatButton signUpButton;
    private boolean isPasswordVisible = false;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private Dialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_registration);

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        backArrow = findViewById(R.id.backButton);
        usernameEditText = findViewById(R.id.studentUsernameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        passwordToggle = findViewById(R.id.passwordToggle);
        signUpButton = findViewById(R.id.signUpButton);

        // Initialize custom loading dialog
        loadingDialog = new Dialog(this);
        loadingDialog.setContentView(R.layout.loading_dialog);
        loadingDialog.setCancelable(false);
        Objects.requireNonNull(loadingDialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

        // Get the ImageView for the loading animation
        ImageView loadingImage = loadingDialog.findViewById(R.id.loadingImage);
        if (loadingImage != null) {
            Animation rotation = AnimationUtils.loadAnimation(this, R.anim.rotate_loading);
            loadingImage.startAnimation(rotation);
        } else {
            Log.w(TAG, "loadingImage is null in loading_dialog layout");
        }

        // Back arrow click
        backArrow.setOnClickListener(v -> {
            Intent back = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(back);
            finish();
        });

        // Toggle password visibility
        passwordToggle.setOnClickListener(v -> {
            if (isPasswordVisible) {
                passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                passwordToggle.setImageResource(R.drawable.ic_eye);
                isPasswordVisible = false;
            } else {
                passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                passwordToggle.setImageResource(R.drawable.ic_eye);
                isPasswordVisible = true;
            }
            passwordEditText.setSelection(passwordEditText.getText().length());
        });

        // Sign up button click
        signUpButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim().toLowerCase();
            String phone = phoneEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            Log.d(TAG, "Registration attempt - Username: " + username + ", Email: " + email + ", Phone: " + phone);

            // Validate inputs
            if (!validateInputs(username, email, phone, password)) {
                loadingDialog.dismiss();
                return;
            }

            // Show loading dialog
            loadingDialog.show();

            // Start the registration process
            registerUser(email, password, username, phone);
        });
    }

    private boolean validateInputs(String username, String email, String phone, String password) {
        // Username validation
        if (username.isEmpty()) {
            usernameEditText.setError("Username is required");
            return false;
        }
        // Email validation
        if (email.isEmpty()) {
            emailEditText.setError("Email is required");
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Enter a valid email address");
            return false;
        }
        if (!email.endsWith("@paruluniversity.ac.in")) {
            emailEditText.setError("Only @paruluniversity.ac.in emails are allowed");
            return false;
        }

        // Phone number validation
        if (phone.isEmpty()) {
            phoneEditText.setError("Phone number is required");
            return false;
        }
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            phoneEditText.setError("Phone number must be exactly 10 digits");
            return false;
        }

        // Password validation
        if (password.isEmpty()) {
            passwordEditText.setError("Password is required");
            return false;
        }
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            passwordEditText.setError("Password must be at least 6 characters and contain at least one uppercase letter, one lowercase letter, one digit, and one special character (@$!%*?&)");
            return false;
        }

        return true;
    }

    private void registerUser(String email, String password, String username, String phone) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Firebase Authentication successful for email: " + email);
                        // User created successfully in Firebase Authentication
                        saveToFirestore(email, username, phone);
                    } else {
                        // Hide loading dialog on failure
                        loadingDialog.dismiss();
                        Log.e(TAG, "Firebase Authentication failed: " + task.getException().getMessage());

                        // Handle errors
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            emailEditText.setError("Email already exists");
                        } else {
                            Toast.makeText(StudentRegistrationActivity.this,
                                    "Registration failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Registration failed: " + task.getException().getMessage());
                        }
                    }
                });
    }

    private void saveToFirestore(String email, String username, String phone) {
        Map<String, Object> student = new HashMap<>();
        student.put("email", email);
        student.put("username", username);
        student.put("phone", phone);

        db.collection("students")
                .document(auth.getCurrentUser().getUid())
                .set(student)
                .addOnSuccessListener(documentReference -> {
                    loadingDialog.dismiss();
                    Log.d(TAG, "Firestore save successful for UID: " + auth.getCurrentUser().getUid());
                    Toast.makeText(StudentRegistrationActivity.this,
                            "Registration successful", Toast.LENGTH_LONG).show();
                    Intent gotoLogin = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(gotoLogin);
                    finish();
                })
                .addOnFailureListener(e -> {
                    loadingDialog.dismiss();
                    Log.e(TAG, "Firestore save failed: " + e.getMessage());
                    Toast.makeText(StudentRegistrationActivity.this,
                            "Failed to save student data: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    // Optionally delete the user from Authentication if Firestore fails
                    if (auth.getCurrentUser() != null) {
                        auth.getCurrentUser().delete().addOnCompleteListener(deleteTask -> {
                            if (deleteTask.isSuccessful()) {
                                Log.d(TAG, "User deleted from Authentication due to Firestore failure");
                            }
                        });
                    }
                });
    }
}