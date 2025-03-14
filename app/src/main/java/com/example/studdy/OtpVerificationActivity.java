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

import java.util.Objects;
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
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        // Initialize views
        otpEditText = findViewById(R.id.otpEditText);
        verifyButton = findViewById(R.id.verifyButton);
        resendOtpButton = findViewById(R.id.resendOtpButton);
        resendTimerTextView = findViewById(R.id.resendTimerTextView);
        backButton = findViewById(R.id.backButton);

        // Initialize loading dialog
        loadingDialog = new Dialog(this);
        loadingDialog.setContentView(R.layout.loading_dialog);
        loadingDialog.setCancelable(false);
        Objects.requireNonNull(loadingDialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);


        otpEditText.setFilters(new InputFilter[]{
                new InputFilter.AllCaps(),
                new InputFilter.LengthFilter(4)
        });

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
        verifyButton.setOnClickListener(v -> {
            String enteredOtp = otpEditText.getText().toString().trim();
            if (enteredOtp.equals(generatedOtp)) {
                Intent intent = new Intent(OtpVerificationActivity.this, ResetPasswordActivity.class);
                intent.putExtra("email", email);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(OtpVerificationActivity.this, "Invalid OTP", Toast.LENGTH_SHORT).show();
            }
        });

        // Resend OTP button click
        resendOtpButton.setOnClickListener(v -> {
            loadingDialog.show();
            // Generate a new OTP
            generatedOtp = generateOTP();
            // Resend the OTP
            new SendOtpEmailTask(email, generatedOtp).execute();
            // Restart the countdown timer
            startResendTimer();
        });
    }

    // Method to start the 30-second countdown timer
    private void startResendTimer() {
        resendOtpButton.setEnabled(false);
        countDownTimer = new CountDownTimer(30000, 1000) { // 30 seconds, tick every 1 second
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel(); // Cancel the timer to avoid memory leaks
        }
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
            loadingDialog.dismiss();
            if (success) {
                Toast.makeText(OtpVerificationActivity.this, "OTP resent. Check your email.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(OtpVerificationActivity.this, "Failed to resend OTP: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        }
    }
}