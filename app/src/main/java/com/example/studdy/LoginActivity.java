package com.example.studdy;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.gms.common.SignInButton;

public class LoginActivity extends AppCompatActivity {

    private RadioGroup roleRadioGroup;
    private EditText usernameEditText, passwordEditText;
    private ImageView passwordToggle;
    private AppCompatButton signInButton;
    private SignInButton googleSignInButton;
    private TextView forgotPasswordTextView;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize views
        roleRadioGroup = findViewById(R.id.roleRadioGroup);
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        passwordToggle = findViewById(R.id.passwordToggle);
        signInButton = findViewById(R.id.signInButton);
        googleSignInButton = findViewById(R.id.googleSignInButton);
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);

        // Toggle password visibility
        passwordToggle.setOnClickListener(v -> {
            if (isPasswordVisible) {
                passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                passwordToggle.setImageResource(R.drawable.ic_eye); // Show closed eye (or your custom icon)
                isPasswordVisible = false;
            } else {
                passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                passwordToggle.setImageResource(R.drawable.ic_eye); // Update to an open eye icon if you have one
                isPasswordVisible = true;
            }
            // Move cursor to the end of the text
            passwordEditText.setSelection(passwordEditText.getText().length());
        });

        // Sign in button click
        signInButton.setOnClickListener(v -> {
            // Validate inputs
            if (roleRadioGroup.getCheckedRadioButtonId() == -1) {
                Toast.makeText(LoginActivity.this, "Please select a role", Toast.LENGTH_SHORT).show();
                return;
            }

            String role = ((RadioButton) findViewById(roleRadioGroup.getCheckedRadioButtonId())).getText().toString();
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (username.isEmpty()) {
                usernameEditText.setError("Username is required");
                return;
            }

            if (password.isEmpty()) {
                passwordEditText.setError("Password is required");
                return;
            }

            // TODO: Implement actual sign-in logic (e.g., API call, Firebase Auth, etc.)
            Toast.makeText(LoginActivity.this, "Sign in successful as " + role, Toast.LENGTH_SHORT).show();
            // Navigate to MainActivity or appropriate screen after successful login
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        });

        // Google sign-in button click
        googleSignInButton.setOnClickListener(v -> {
            // TODO: Implement Google Sign-In (requires Google Sign-In API setup)
            Toast.makeText(LoginActivity.this, "Google Sign-In clicked", Toast.LENGTH_SHORT).show();
        });

        // Forgot password click
        forgotPasswordTextView.setOnClickListener(v -> {
            // TODO: Implement forgot password logic (e.g., navigate to a ForgotPasswordActivity)
            Toast.makeText(LoginActivity.this, "Forgot Password clicked", Toast.LENGTH_SHORT).show();
        });
    }
}