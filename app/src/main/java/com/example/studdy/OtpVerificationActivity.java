package com.example.studdy;

import static SMTP.Credentials.SMTP_EMAIL;
import static SMTP.Credentials.SMTP_PASSWORD;

import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.security.SecureRandom;
import java.util.Properties;
import java.util.Random;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class OtpVerificationActivity extends AppCompatActivity {

    private static final String TAG = "OtpVerificationActivity";
    private EditText otpEditText;
    private Button verifyButton, resendOtpButton;
    private TextView resendTimerTextView;
    private ImageButton backButton;
    private Dialog loadingDialog;
    private String email, generatedOtp;
    private FirebaseAuth auth;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Initialize views
        otpEditText = findViewById(R.id.otpEditText);
        otpEditText.setFilters(new InputFilter[]{
                new InputFilter.AllCaps(),
                new InputFilter.LengthFilter(4)
        });
        verifyButton = findViewById(R.id.verifyButton);
        resendOtpButton = findViewById(R.id.resendOtpButton);
        resendTimerTextView = findViewById(R.id.resendTimerTextView);
        backButton = findViewById(R.id.backButton);

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
        }

        // Get email and OTP from Intent
        email = getIntent().getStringExtra("email");
        generatedOtp = getIntent().getStringExtra("otp");

        // Enable verify button only when OTP is 4 digits
        otpEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                verifyButton.setEnabled(s.length() == 4);
            }
        });

        // Start the countdown timer
        startResendTimer();

        // Back button click
        backButton.setOnClickListener(v -> {
            Intent back = new Intent(OtpVerificationActivity.this, ForgotPasswordActivity.class);
            back.putExtra("email", email);
            startActivity(back);
            finish();
        });

        // Verify button click
//        verifyButton.setOnClickListener(v -> {
//            String enteredOtp = otpEditText.getText().toString().trim();
//            if (enteredOtp.equals(generatedOtp)) {
//                loadingDialog.show();
//                // Generate a random 8-character temporary password
//                String tempPassword = generateTempPassword();
//                // Update existing user's password
//                auth.signInWithEmailAndPassword(email, "default") // Use a placeholder password to sign in
//                        .addOnCompleteListener(signInTask -> {
//                            if (signInTask.isSuccessful()) {
//                                auth.getCurrentUser().updatePassword(tempPassword)
//                                        .addOnCompleteListener(updateTask -> {
//                                            if (updateTask.isSuccessful()) {
//                                                auth.signOut();
//                                                sendTempPasswordEmail(email, tempPassword);
//                                                proceedToLogin();
//                                            } else {
//                                                loadingDialog.dismiss();
//                                                Toast.makeText(OtpVerificationActivity.this, "Failed to update password", Toast.LENGTH_SHORT).show();
//                                            }
//                                        });
//                            } else {
//                                loadingDialog.dismiss();
//                                Toast.makeText(OtpVerificationActivity.this, "Failed to sign in", Toast.LENGTH_SHORT).show();
//                            }
//                        });
//            } else {
//                Toast.makeText(OtpVerificationActivity.this, "Invalid OTP", Toast.LENGTH_SHORT).show();
//            }
//        });

        // Resend OTP button click

        // Verify button click
        verifyButton.setOnClickListener(v -> {
            String enteredOtp = otpEditText.getText().toString().trim();
            if (enteredOtp.equals(generatedOtp)) {
                loadingDialog.show();
                // Send password reset email instead of manual password update
                auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            loadingDialog.dismiss();
                            if (task.isSuccessful()) {
                                Toast.makeText(OtpVerificationActivity.this,
                                        "Password reset email sent. Check your inbox.",
                                        Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(OtpVerificationActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(OtpVerificationActivity.this,
                                        "Failed to send reset email: " + task.getException().getMessage(),
                                        Toast.LENGTH_LONG).show();
                            }
                        });
            } else {
                Toast.makeText(OtpVerificationActivity.this, "Invalid OTP", Toast.LENGTH_SHORT).show();
            }
        });

        resendOtpButton.setOnClickListener(v -> {
            loadingDialog.show();
            generatedOtp = generateOTP();
            new SendOtpEmailTask(email, generatedOtp).execute();
            startResendTimer();
        });
    }

    // Method to start the 30-second countdown timer
    private void startResendTimer() {
        resendOtpButton.setEnabled(false);
        countDownTimer = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                resendTimerTextView.setText("Resend OTP in " + (millisUntilFinished / 1000) + "s");
            }

            @Override
            public void onFinish() {
                resendTimerTextView.setText("Resend OTP now");
                resendOtpButton.setEnabled(true);
            }
        }.start();
    }

    // Method to generate a 4-digit OTP
    private String generateOTP() {
        Random random = new Random();
        return String.format("%04d", random.nextInt(10000));
    }

    // Method to generate a random 8-character temporary password
    private String generateTempPassword() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            password.append(characters.charAt(random.nextInt(characters.length())));
        }
        return password.toString();
    }

    // Method to proceed to LoginActivity
    private void proceedToLogin() {
        loadingDialog.dismiss();
        Toast.makeText(OtpVerificationActivity.this, "Temporary password sent. Please log in.", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(OtpVerificationActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    // AsyncTask to send OTP via email
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
                Properties props = new Properties();
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.host", "smtp.gmail.com");
                props.put("mail.smtp.port", "587");

                Session session = Session.getInstance(props, new javax.mail.Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(SMTP_EMAIL, SMTP_PASSWORD);
                    }
                });

                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(SMTP_EMAIL));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
                message.setSubject("Your OTP");
                message.setText("Your OTP is: " + otp);

                Transport.send(message);
                Log.d(TAG, "OTP sent to " + email);
                return true;
            } catch (MessagingException e) {
                Log.e(TAG, "Failed to send OTP email: " + e.getMessage());
                errorMessage = e.getMessage();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            loadingDialog.dismiss();
            if (success) {
                Toast.makeText(OtpVerificationActivity.this, "OTP resent. Check your email.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(OtpVerificationActivity.this, "Failed to resend OTP", Toast.LENGTH_LONG).show();
            }
        }
    }
}