package com.example.studdy;

import static SMTP.Credentials.SMTP_PASSWORD;

import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Random;
import java.util.regex.Pattern;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class FacultyCodeRegistrationActivity extends AppCompatActivity {

    private ImageView backArrow, passwordToggle;
    private EditText usernameEditText, emailEditText, phoneEditText, passwordEditText;
    private RadioGroup roleRadioGroup;
    private AppCompatButton signUpButton, goToSignInButton;
    private boolean isPasswordVisible = false;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private Dialog loadingDialog; // Custom loading dialog

    // SMTP credentials for Gmail
    private static final String SMTP_EMAIL = "arshadali.app431@gmail.com";

    // Regular expressions for validation
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9]{3,20}$"); // Alphanumeric, 3-20 characters
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10}$"); // Exactly 10 digits
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{6,}$");
    // Password must contain: at least one lowercase, one uppercase, one digit, one special character, minimum 6 characters

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faculty_code_registration);

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        backArrow = findViewById(R.id.backButton); // Corrected ID
        roleRadioGroup = findViewById(R.id.roleRadioGroup);
        usernameEditText = findViewById(R.id.facultyUsernameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        passwordToggle = findViewById(R.id.passwordToggle);
        signUpButton = findViewById(R.id.signUpButton);

        // Initialize custom loading dialog
        loadingDialog = new Dialog(this);
        loadingDialog.setContentView(R.layout.loading_dialog);
        loadingDialog.setCancelable(false); // Prevent dismissing by clicking outside
        Objects.requireNonNull(loadingDialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent); // Transparent background

        // Get the ImageView for the loading animation
        ImageView loadingImage = loadingDialog.findViewById(R.id.loadingImage);
        if (loadingImage != null) {
            // Load and apply the rotation animation
            Animation rotation = AnimationUtils.loadAnimation(this, R.anim.rotate_loading);
            loadingImage.startAnimation(rotation);
        }

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

        // Sign up button click
        signUpButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim().toLowerCase(); // Normalize to lowercase
            String phone = phoneEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            // Username validation
            if (username.isEmpty()) {
                usernameEditText.setError("Username is required");
                return;
            }
            // Email validation
            if (email.isEmpty()) {
                emailEditText.setError("Email is required");
                return;
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailEditText.setError("Enter a valid email address");
                return;
            }
            if (!email.endsWith("@paruluniversity.ac.in")) {
                emailEditText.setError("Only @paruluniversity.ac.in emails are allowed");
                return;
            }

            // Phone number validation
            if (phone.isEmpty()) {
                phoneEditText.setError("Phone number is required");
                return;
            }
            if (!PHONE_PATTERN.matcher(phone).matches()) {
                phoneEditText.setError("Phone number must be exactly 10 digits");
                return;
            }

            // Password validation
            if (password.isEmpty()) {
                passwordEditText.setError("Password is required");
                return;
            }
            if (!PASSWORD_PATTERN.matcher(password).matches()) {
                passwordEditText.setError("Password must be at least 6 characters and contain at least one uppercase letter, one lowercase letter, one digit, and one special character (@$!%*?&)");
                return;
            }

            // Show loading dialog
            loadingDialog.show();

            // Create user in Firebase Authentication
            createUserInFirebaseAuth(email, password, username, phone);
        });
    }

    // Method to create a user in Firebase Authentication
    private void createUserInFirebaseAuth(String email, String password, String username, String phone) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // User created successfully in Firebase Authentication
                        String staffCode = generateStaffCode();
                        saveFacultyToFirestore(email, username, phone, staffCode);
                    } else {
                        // Hide loading dialog on failure
                        loadingDialog.dismiss();

                        // Handle errors
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            emailEditText.setError("Email already exists");
                        } else {
                            Toast.makeText(FacultyCodeRegistrationActivity.this,
                                    "Registration failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Method to save faculty data to Firestore
    private void saveFacultyToFirestore(String email, String username, String phone, String staffCode) {
        Map<String, Object> faculty = new HashMap<>();
        faculty.put("email", email);
        faculty.put("username", username);
        faculty.put("phone", phone); // Correctly map phone number
        faculty.put("staff_code", staffCode); // Correctly map staff code

        db.collection("faculty")
                .document(auth.getCurrentUser().getUid())
                .set(faculty)
                .addOnSuccessListener(documentReference -> {
                    // Registration successful, send email and show popup
                    new SendEmailTask(email, staffCode, () -> {
                        // Hide loading dialog after email is sent
                        loadingDialog.dismiss();
                        // Show popup only after email is sent
                        showSuccessPopup(staffCode);
                    }).execute();
                })
                .addOnFailureListener(e -> {
                    // Hide loading dialog on failure
                    loadingDialog.dismiss();
                    Toast.makeText(FacultyCodeRegistrationActivity.this,
                            "Failed to save faculty data: " + e.getMessage(),
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

    // AsyncTask to send email in the background
    private class SendEmailTask extends AsyncTask<Void, Void, Boolean> {
        private final String recipientEmail;
        private final String staffCode;
        private final Runnable onSuccess;
        private String errorMessage;

        public SendEmailTask(String recipientEmail, String staffCode, Runnable onSuccess) {
            this.recipientEmail = recipientEmail;
            this.staffCode = staffCode;
            this.onSuccess = onSuccess;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                // Set up mail server properties
                Properties props = new Properties();
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.host", "smtp.gmail.com");
                props.put("mail.smtp.port", "587");

                // Create a session with authentication
                Session session = Session.getInstance(props, new javax.mail.Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(SMTP_EMAIL, SMTP_PASSWORD);
                    }
                });

                // Create a new email message
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(SMTP_EMAIL, "Studdy Team"));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
                message.setSubject("Your Staff Code for Studdy");
                message.setText("Dear Faculty,\n\nYour staff code is: " + staffCode + "\n\nPlease use this code to complete your registration.\n\nBest regards,\nStuddy Team");

                // Send the email
                Transport.send(message);
                return true;
            } catch (Exception e) {
                errorMessage = e.getMessage();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(FacultyCodeRegistrationActivity.this,
                        "Email sent successfully", Toast.LENGTH_SHORT).show();
                onSuccess.run(); // Call the success callback
            } else {
                // Hide loading dialog on failure
                loadingDialog.dismiss();
                Toast.makeText(FacultyCodeRegistrationActivity.this,
                        "Failed to send email: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        }
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