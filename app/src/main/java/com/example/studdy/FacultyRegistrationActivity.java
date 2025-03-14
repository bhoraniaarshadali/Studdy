package com.example.studdy;

import static SMTP.Credentials.SMTP_EMAIL;
import static SMTP.Credentials.SMTP_PASSWORD;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class FacultyRegistrationActivity extends AppCompatActivity {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{6,}$");
    private ImageView backArrow, passwordToggle;
    private EditText usernameEditText, emailEditText, phoneEditText, passwordEditText;
    private RadioGroup roleRadioGroup;
    private AppCompatButton signUpButton;
    private boolean isPasswordVisible = false;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private Dialog loadingDialog;
    private ExecutorService emailExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faculty_code_registration);

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        emailExecutor = Executors.newSingleThreadExecutor();

        backArrow = findViewById(R.id.backButton);
        roleRadioGroup = findViewById(R.id.roleRadioGroup);
        usernameEditText = findViewById(R.id.facultyUsernameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        passwordToggle = findViewById(R.id.passwordToggle);
        signUpButton = findViewById(R.id.signUpButton);

        loadingDialog = new Dialog(this);
        loadingDialog.setContentView(R.layout.loading_dialog);
        loadingDialog.setCancelable(false);
        Objects.requireNonNull(loadingDialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);


        ImageView loadingImage = loadingDialog.findViewById(R.id.loadingImage);
        if (loadingImage != null) {
            Animation rotation = AnimationUtils.loadAnimation(this, R.anim.rotate_loading);
            loadingImage.startAnimation(rotation);
        }

        backArrow.setOnClickListener(v -> {
            finish();
        });

        passwordToggle.setOnClickListener(v -> {
            if (isPasswordVisible) {
                passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                passwordToggle.setImageResource(R.drawable.ic_eye); // Closed eye
                isPasswordVisible = false;
            } else {
                passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                passwordToggle.setImageResource(R.drawable.ic_eye); // Open eye
                isPasswordVisible = true;
            }
            passwordEditText.setSelection(passwordEditText.getText().length());
        });

        signUpButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim().toLowerCase(); // Normalize to lowercase
            String phone = phoneEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (!validateInputs(username, email, phone, password)) {
                return;
            }
            loadingDialog.show();
            registerUser(email, password, username, phone);
        });
    }

    // Separate method for input validation
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

    // Separate method for Firebase Authentication
    private void registerUser(String email, String password, String username, String phone) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // User created successfully in Firebase Authentication
                        String staffCode = generateStaffCode();
                        saveToFirestore(email, username, phone, staffCode);
                    } else {
                        // Hide loading dialog on failure
                        loadingDialog.dismiss();

                        // Handle errors
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            emailEditText.setError("Email already exists");
                        } else {
                            Toast.makeText(FacultyRegistrationActivity.this,
                                    "Registration failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    private void saveToFirestore(String email, String username, String phone, String staffCode) {
        Map<String, Object> faculty = new HashMap<>();
        faculty.put("email", email);
        faculty.put("username", username);
        faculty.put("phone", phone);
        faculty.put("staff_code", staffCode);

        db.collection("faculty")
                .document(auth.getCurrentUser().getUid())
                .set(faculty)
                .addOnSuccessListener(documentReference -> {
                    sendStaffCodeEmail(email, staffCode);
                })
                .addOnFailureListener(e -> {
                    loadingDialog.dismiss();
                    Toast.makeText(FacultyRegistrationActivity.this,
                            "Failed to save faculty data: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    // Separate method for generating staff code
    private String generateStaffCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        for (int i = 0; i < 6; i++) {
            code.append(characters.charAt(random.nextInt(characters.length())));
            //System.out.println(code);
        }
        return code.toString();
    }

    // Separate method for sending email using ExecutorService
    private void sendStaffCodeEmail(String recipientEmail, String staffCode) {
        emailExecutor.submit(() -> {
            try {
                // Set up mail server properties
                Properties props = new Properties();
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.host", "smtp.gmail.com");
                props.put("mail.smtp.port", "587");
                props.put("mail.smtp.connectiontimeout", "10000"); // 10 seconds timeout
                props.put("mail.smtp.timeout", "10000"); // 10 seconds timeout

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

                // Run on UI thread to update UI
                runOnUiThread(() -> {
                    loadingDialog.dismiss();
                    Toast.makeText(FacultyRegistrationActivity.this,
                            "Email sent successfully", Toast.LENGTH_LONG).show();
                    Intent gotoLogin = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(gotoLogin);
                    finish();
                });
            } catch (Exception e) {
                // Run on UI thread to update UI
                runOnUiThread(() -> {
                    loadingDialog.dismiss();
                    Toast.makeText(FacultyRegistrationActivity.this,
                            "Failed to send email: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    // Method to show success popup and navigate to LoginActivity
    private void showSuccessPopup(String staffCode) {
        new AlertDialog.Builder(this)
                .setTitle("Registration Successful")
                .setMessage("Generated staff code sent on your email")
                .setPositiveButton("OK", (dialog, which) -> {
                    // Navigate to LoginActivity
                    startActivity(new Intent(FacultyRegistrationActivity.this, LoginActivity.class));
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Shutdown the executor to prevent memory leaks
        emailExecutor.shutdown();
        try {
            if (!emailExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                emailExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            emailExecutor.shutdownNow();
        }
    }
}