package com.example.studdy;

import android.app.Dialog;
import android.content.Intent;
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

public class ForgotPasswordActivity extends AppCompatActivity {

    private static final String TAG = "ForgotPasswordActivity";
    private ImageButton backButton;
    private TextView emailText;
    private AppCompatButton sendButton;
    private FirebaseAuth auth;
    private Dialog loadingDialog;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();

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

        // Get email from Intent (passed from LoginActivity) - Email is retrieved here
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

        // Send reset email button click
        sendButton.setOnClickListener(v -> {
            // Show loading dialog
            loadingDialog.show();

            // Send password reset email
            auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        loadingDialog.dismiss();
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Password reset email sent to: " + email);
                            Toast.makeText(ForgotPasswordActivity.this,
                                    "Password reset email sent. Check your inbox!",
                                    Toast.LENGTH_LONG).show();
                            Intent back = new Intent(getApplicationContext(), LoginActivity.class);
                            startActivity(back);
                            finish();
                        } else {
                            Log.e(TAG, "Failed to send password reset email: " + task.getException().getMessage());
                            Toast.makeText(ForgotPasswordActivity.this,
                                    "Failed to send reset email: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    // Method to mask the email (e.g., 24******@paruluniversity.ac.in)
    private String maskEmail(String email) {
        if (TextUtils.isEmpty(email)) return "";
        String[] parts = email.split("@");
        if (parts.length != 2) return email;

        String localPart = parts[0];
        String domain = parts[1];

        // Mask the local part (e.g., "teststudent" -> "te******nt")
        if (localPart.length() <= 4) {
            return localPart.charAt(0) + "****@" + domain;
        }

        String maskedLocalPart = localPart.substring(0, 2) + "******" + localPart.substring(localPart.length() - 2);
        return maskedLocalPart + "@" + domain;
    }
}