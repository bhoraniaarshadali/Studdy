package com.example.studdy;

import static SMTP.Credentials.SMTP_PASSWORD;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
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
import java.util.Properties;
import java.util.Random;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class FacultyCodeRegistrationActivity extends AppCompatActivity {

    // SMTP credentials for Gmail
    private static final String SMTP_EMAIL = "arshadali.app431@gmail.com"; // Your Gmail address
    private ImageView backArrow, passwordToggle;
    private EditText usernameEditText, emailEditText, phoneEditText, passwordEditText;
    private RadioGroup roleRadioGroup;
    private AppCompatButton signUpButton, goToSignInButton;
    private boolean isPasswordVisible = false;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faculty_code_registration);

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        backArrow = findViewById(R.id.backButton);
        roleRadioGroup = findViewById(R.id.roleRadioGroup);
        emailEditText = findViewById(R.id.emailEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        passwordToggle = findViewById(R.id.passwordToggle);
        signUpButton = findViewById(R.id.signUpButton);

        // Back arrow click
        backArrow.setOnClickListener(v -> {
            finish(); // go back to the previous (LoginActivity)
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
            String email = emailEditText.getText().toString().trim();
            String phone = phoneEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();


            // Basic validation
//            if (username.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
//                showErrorPopup("All fields are required!");
//                return;
//            }

            // Basic validation
            if (username.isEmpty()) {
                usernameEditText.setError("Username is required");
                return;
            }

            if (email.isEmpty()) {
                emailEditText.setError("Email is required");
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailEditText.setError("Enter a valid email address");
                return;
            }

            // Restrict email to @paruluniversity.ac.in domain
            if (!email.endsWith("@paruluniversity.ac.in")) {
                emailEditText.setError("Only @paruluniversity.ac.in emails are allowed");
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

            if (password.length() < 6) {
                passwordEditText.setError("Password must be at least 6 characters");
                return;
            }

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
        faculty.put("phone", phone);
        faculty.put("staff_code", staffCode);

        db.collection("faculty")
                .document(auth.getCurrentUser().getUid())
                .set(faculty)
                .addOnSuccessListener(documentReference -> {
                    // Registration successful, send email and show popup
                    new SendEmailTask(email, staffCode, () -> {
                        // Show popup only after email is sent
                        showSuccessPopup(staffCode);
                    }).execute();
                })
                .addOnFailureListener(e -> {
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
                onSuccess.run(); // Call the success callback to show the popup
            } else {
                Toast.makeText(FacultyCodeRegistrationActivity.this,
                        "Failed to send email: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        }
    }
}