package com.example.studdy;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.gms.common.SignInButton;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private RadioGroup roleRadioGroup;
    private EditText usernameEditText, passwordEditText;
    private ImageView passwordToggle, facultyRegistration;
    private AppCompatButton signInButton;
    private SignInButton googleSignInButton;
    private TextView forgotPasswordTextView, signUpTextView;
    private boolean isPasswordVisible = false;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ImageView facultyRegistration1 = findViewById(R.id.facultyRegistration);
        facultyRegistration1.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, FacultyCodeRegistrationActivity.class));
        });


        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();

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
                passwordToggle.setImageResource(R.drawable.ic_eye);
                isPasswordVisible = false;
            } else {
                passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                passwordToggle.setImageResource(R.drawable.ic_eye);
                isPasswordVisible = true;
            }
            passwordEditText.setSelection(passwordEditText.getText().length());
        });

        // Sign in button click
        signInButton.setOnClickListener(v -> {
            if (roleRadioGroup.getCheckedRadioButtonId() == -1) {
                Toast.makeText(LoginActivity.this, "Please select a role", Toast.LENGTH_SHORT).show();
                return;
            }

            String role = ((RadioButton) findViewById(roleRadioGroup.getCheckedRadioButtonId())).getText().toString();
            String email = usernameEditText.getText().toString().trim(); // Now using email instead of username
            String password = passwordEditText.getText().toString().trim();

            if (email.isEmpty()) {
                usernameEditText.setError("Email is required");
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                usernameEditText.setError("Enter a valid email address");
                return;
            }

            if (password.isEmpty()) {
                passwordEditText.setError("Password is required");
                return;
            }

            // Sign in with Firebase Authentication
            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Sign-in successful
                            Toast.makeText(LoginActivity.this, "Sign in successful as " + role, Toast.LENGTH_SHORT).show();
                            // Navigate based on role
                            Intent intent;
                            if (role.equals("Faculty")) {
                                intent = new Intent(LoginActivity.this, MainActivity.class); // in future we will replace with faculty dashboard
                            } else {
                                intent = new Intent(LoginActivity.this, MainActivity.class);
                            }
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this,
                                    "Sign in failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
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