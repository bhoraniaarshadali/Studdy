package com.example.studdy;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {

    private static final String TAG = "ResetPasswordActivity";
    private EditText newPasswordEditText, confirmPasswordEditText;
    private Button updatePasswordButton;
    private Dialog loadingDialog;
    private FirebaseAuth auth;
    private String email;
    private boolean isNewPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Initialize views
        newPasswordEditText = findViewById(R.id.newPasswordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        updatePasswordButton = findViewById(R.id.updatePasswordButton);

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

        ImageView passwordToggle1, toggleConfirmPassword2;
        passwordToggle1 = findViewById(R.id.passwordToggle1);
        toggleConfirmPassword2 = findViewById(R.id.toggleConfirmPassword2);

        // Toggle for new password field
        passwordToggle1.setOnClickListener(v -> {
            isNewPasswordVisible = !isNewPasswordVisible;
            if (isNewPasswordVisible) {
                newPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                passwordToggle1.setImageResource(R.drawable.ic_eye_open);
            } else {
                newPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                passwordToggle1.setImageResource(R.drawable.ic_eye_close);
            }
            newPasswordEditText.setSelection(newPasswordEditText.getText().length());
        });

// Toggle for confirm password field
        toggleConfirmPassword2.setOnClickListener(v -> {
            isConfirmPasswordVisible = !isConfirmPasswordVisible;
            if (isConfirmPasswordVisible) {
                confirmPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                toggleConfirmPassword2.setImageResource(R.drawable.ic_eye_open);
            } else {
                confirmPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                toggleConfirmPassword2.setImageResource(R.drawable.ic_eye_close);
            }
            confirmPasswordEditText.setSelection(confirmPasswordEditText.getText().length());
        });



        // Get email from Intent
        email = getIntent().getStringExtra("email");

        // Update password button click
        updatePasswordButton.setOnClickListener(v -> {
            String newPassword = newPasswordEditText.getText().toString().trim();
            String confirmPassword = confirmPasswordEditText.getText().toString().trim();

            if (TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmPassword)) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }
            if (newPassword.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            loadingDialog.show();

            if (auth.getCurrentUser() != null) {
                // Re-authenticate user with email & password
                String userEmail = auth.getCurrentUser().getEmail();
                String userPassword = getIntent().getStringExtra("password"); // Get old password from Intent

                if (userPassword == null || userPassword.isEmpty()) {
                    loadingDialog.dismiss();
                    Toast.makeText(ResetPasswordActivity.this, "Re-authentication failed: Old password required", Toast.LENGTH_SHORT).show();
                    return;
                }

                AuthCredential credential = EmailAuthProvider.getCredential(userEmail, userPassword);
                auth.getCurrentUser().reauthenticate(credential)
                        .addOnCompleteListener(reauthTask -> {
                            if (reauthTask.isSuccessful()) {
                                // After successful re-authentication, update password
                                auth.getCurrentUser().updatePassword(newPassword)
                                        .addOnCompleteListener(task -> {
                                            loadingDialog.dismiss();
                                            if (task.isSuccessful()) {
                                                Log.d(TAG, "Password updated successfully");
                                                Toast.makeText(ResetPasswordActivity.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(ResetPasswordActivity.this, LoginActivity.class));
                                                finish();
                                            } else {
                                                Log.e(TAG, "Password update failed: " + task.getException().getMessage());
                                                Toast.makeText(ResetPasswordActivity.this, "Failed to update password: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                loadingDialog.dismiss();
                                Log.e(TAG, "Re-authentication failed: " + reauthTask.getException().getMessage());
                                Toast.makeText(ResetPasswordActivity.this, "Re-authentication failed: " + reauthTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                loadingDialog.dismiss();
                Toast.makeText(ResetPasswordActivity.this, "User not logged in", Toast.LENGTH_SHORT).show();
            }
        });

    }
}