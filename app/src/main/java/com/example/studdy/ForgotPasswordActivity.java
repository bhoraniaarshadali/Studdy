package com.example.studdy;

import static SMTP.Credentials.SMTP_EMAIL;
import static SMTP.Credentials.SMTP_PASSWORD;

import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Properties;
import java.util.Random;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class ForgotPasswordActivity extends AppCompatActivity {

    private static final String TAG = "ForgotPasswordActivity";
    private ImageButton backButton;
    private TextView emailText;
    private AppCompatButton sendButton;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private Dialog loadingDialog;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        backButton = findViewById(R.id.backButton);
        emailText = findViewById(R.id.emailText);
        sendButton = findViewById(R.id.sendButton);

        // Initialize loading dialog
        loadingDialog = new Dialog(this);
        loadingDialog.setContentView(R.layout.loading_dialog);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // Apply rotation animation to loading image
        ImageView loadingImage = loadingDialog.findViewById(R.id.loadingImage);
        if (loadingImage != null) {
            Animation rotation = AnimationUtils.loadAnimation(this, R.anim.rotate_loading);
            loadingImage.startAnimation(rotation);
        } else {
            Log.w(TAG, "loadingImage is null in loading_dialog layout");
        }

        // Get email from Intent (passed from LoginActivity)
        email = getIntent().getStringExtra("email");
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "No email provided", Toast.LENGTH_SHORT).show();
            Intent back = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(back);
            finish();
            return;
        }

        // Display masked email
        emailText.setText(maskEmail(email));

        // Back button click
        backButton.setOnClickListener(v -> {
            Intent back = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(back);
            finish();
        });

        // Send OTP button click
        sendButton.setOnClickListener(v -> {
            loadingDialog.show();
            // Check if email exists in students collection
            db.collection("students").whereEqualTo("email", email).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (!querySnapshot.isEmpty()) {
                                // Email found, generate and send OTP
                                String otp = generateOTP();
                                new SendOtpEmailTask(email, otp).execute();
                                // Proceed to OTP verification activity
                                Intent intent = new Intent(ForgotPasswordActivity.this, OtpVerificationActivity.class);
                                intent.putExtra("email", email);
                                intent.putExtra("otp", otp);
                                startActivity(intent);
                                finish();
                            } else {
                                loadingDialog.dismiss();
                                Toast.makeText(ForgotPasswordActivity.this, "Wrong email. Email not found.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            loadingDialog.dismiss();
                            Log.e(TAG, "Firestore query failed: " + task.getException().getMessage());
                            Toast.makeText(ForgotPasswordActivity.this, "Error checking email.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    // Method to mask the email
    private String maskEmail(String email) {
        if (TextUtils.isEmpty(email)) return "";
        String[] parts = email.split("@");
        if (parts.length != 2) return email;
        String localPart = parts[0];
        String domain = parts[1];
        if (localPart.length() <= 4) {
            return localPart.charAt(0) + "****@" + domain;
        }
        String maskedLocalPart = localPart.substring(0, 2) + "******" + localPart.substring(localPart.length() - 2);
        return maskedLocalPart + "@" + domain;
    }

    // Method to generate a 4-digit OTP
    private String generateOTP() {
        Random random = new Random();
        return String.format("%04d", random.nextInt(10000));
    }

    // AsyncTask to send OTP via email using SMTP
    private class SendOtpEmailTask extends AsyncTask<Void, Void, Boolean> {
        private final String email;
        private final String otp;
        private String errorMessage;

        public SendOtpEmailTask(String email, String otp) {
            this.email = email;
            this.otp = otp;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                // Configure SMTP properties
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
                message.setFrom(new InternetAddress(SMTP_EMAIL));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
                message.setSubject("Your OTP for Password Reset");
                message.setText("Your OTP for resetting your password is: " + otp + "\n\nPlease use this OTP to verify your identity.");

                // Send the email
                Transport.send(message);
                Log.d(TAG, "OTP " + otp + " sent to " + email);
                return true;
            } catch (MessagingException e) {
                Log.e(TAG, "Failed to send OTP email: " + e.getMessage());
                errorMessage = e.getMessage();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (!success) {
                Toast.makeText(ForgotPasswordActivity.this, "Failed to send OTP: " + errorMessage, Toast.LENGTH_LONG).show();
            }
            // Note: We proceed to OtpVerificationActivity regardless because the OTP is already passed via Intent
            // If email sending fails, the user won't receive the OTP but can still try to verify
        }
    }
}