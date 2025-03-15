package com.example.studdy;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;
import java.util.regex.Pattern;

import faculty.FacultyDashboardActivity;
import faculty.FacultyRegistrationActivity;
import student.StudentDashboardActivity;
import student.StudentRegistrationActivity;

public class LoginActivity extends AppCompatActivity {

    private static final Pattern STAFF_CODE_PATTERN = Pattern.compile("^[A-Z0-9]{6,}$");
    private RadioGroup roleRadioGroup;
    private EditText usernameEditText, passwordEditText;
    private ImageView passwordToggle, facultyRegistration;
    private AppCompatButton signInButton;
    private TextView forgotPasswordTextView;
    private boolean isPasswordVisible = false;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private Dialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        roleRadioGroup = findViewById(R.id.roleRadioGroup);
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        passwordToggle = findViewById(R.id.passwordToggle);
        facultyRegistration = findViewById(R.id.facultyRegistration);
        signInButton = findViewById(R.id.signInButton);
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);

        // Initialize loading dialog
        loadingDialog = new Dialog(this);
        loadingDialog.setContentView(R.layout.loading_dialog);
        loadingDialog.setCancelable(false);
        Objects.requireNonNull(loadingDialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

        // Apply rotation animation to loading image
        ImageView loadingImage = loadingDialog.findViewById(R.id.loadingImage);
        if (loadingImage != null) {
            Animation rotation = AnimationUtils.loadAnimation(this, R.anim.rotate_loading);
            loadingImage.startAnimation(rotation);
        }

        // Update hint based on role selection
        roleRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.facultyRadioButton) {
                usernameEditText.setHint("Enter Staff Code");
                usernameEditText.setInputType(InputType.TYPE_CLASS_TEXT);
                usernameEditText.setFilters(new InputFilter[]{
                        new InputFilter.AllCaps(),
                        new InputFilter.LengthFilter(6)
                });
            } else {
                usernameEditText.setHint("Enter University Email");
                usernameEditText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                usernameEditText.setFilters(new InputFilter[]{});
            }
        });

        // Faculty registration click
        facultyRegistration.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Select Role")
                    .setItems(new String[]{"Faculty", "Student"}, (dialog, which) -> {
                        if (which == 0) {
                            startActivity(new Intent(LoginActivity.this, FacultyRegistrationActivity.class));
                        } else {
                            startActivity(new Intent(LoginActivity.this, StudentRegistrationActivity.class));
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // Toggle password visibility
        passwordToggle.setOnClickListener(v -> {
            if (isPasswordVisible) {
                passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                passwordToggle.setImageResource(R.drawable.ic_eye_close);
                isPasswordVisible = false;
            } else {
                passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                passwordToggle.setImageResource(R.drawable.ic_eye_open);
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
            String input = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (!validateInputs(role, input, password)) {
                return;
            }
            loadingDialog.show();
            authenticateUser(role, input, password);
        });

        // Forgot Password click
        forgotPasswordTextView.setOnClickListener(v -> {
            int selectedRoleId = roleRadioGroup.getCheckedRadioButtonId();

            if (selectedRoleId == -1) {
                Toast.makeText(LoginActivity.this, "Please select a role", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedRoleId == R.id.facultyRadioButton) {
                Toast.makeText(LoginActivity.this, "Contact Admin", Toast.LENGTH_SHORT).show();
            } else {
                String email = usernameEditText.getText().toString().trim();
                if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    usernameEditText.setError("Enter a valid email address");
                    return;
                }
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                intent.putExtra("email", email);
                startActivity(intent);
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            loadingDialog.dismiss();
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Toast.makeText(this, "Google sign in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    loadingDialog.dismiss();
                    if (task.isSuccessful()) {
                        String role = ((RadioButton) findViewById(roleRadioGroup.getCheckedRadioButtonId())).getText().toString();
                        validateUserRoleFromFirestore(role, account.getEmail());
                    } else {
                        Toast.makeText(this, "Google sign in failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean validateInputs(String role, String input, String password) {
        if (role.equals("Faculty")) {
            if (input.isEmpty()) {
                usernameEditText.setError("Staff code is required");
                return false;
            }
            if (!STAFF_CODE_PATTERN.matcher(input).matches()) {
                usernameEditText.setError("Staff code must be uppercase and max 6 characters");
                return false;
            }
        } else {
            if (input.isEmpty()) {
                usernameEditText.setError("Email is required");
                return false;
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(input).matches()) {
                usernameEditText.setError("Enter a valid email address");
                return false;
            }
            if (!input.endsWith("@paruluniversity.ac.in")) {
                usernameEditText.setError("Only @paruluniversity.ac.in emails are allowed");
                return false;
            }
        }
        if (password.isEmpty()) {
            passwordEditText.setError("Password is required");
            return false;
        }
        if (password.length() < 6) {
            passwordEditText.setError("Password must be at least 6 characters");
            return false;
        }
        return true;
    }

    private void authenticateUser(String role, String input, String password) {
        if (role.equals("Faculty")) {
            db.collection("faculty").whereEqualTo("staff_code", input).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            String facultyEmail = querySnapshot.getDocuments().get(0).getString("email");
                            signInWithEmail(facultyEmail, password, role);
                        } else {
                            loadingDialog.dismiss();
                            usernameEditText.setError("Invalid staff code");
                        }
                    });
        } else {
            signInWithEmail(input, password, role);
        }
    }

    private void signInWithEmail(String email, String password, String role) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        validateUserRoleFromFirestore(role, email);
                    } else {
                        loadingDialog.dismiss();
                        Toast.makeText(LoginActivity.this, "Sign in failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void validateUserRoleFromFirestore(String role, String email) {
        String collection = role.equals("Faculty") ? "faculty" : "students";
        db.collection(collection).whereEqualTo("email", email).get()
                .addOnCompleteListener(task -> {
                    loadingDialog.dismiss();
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        Toast.makeText(LoginActivity.this, "Sign in successful as " + role, Toast.LENGTH_SHORT).show();
                        navigateBasedOnRole(role);
                    } else {
                        auth.signOut();
                        Toast.makeText(LoginActivity.this, "User not found in " + role + " records", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigateBasedOnRole(String role) {
        Intent intent;
        if (role.equals("Faculty")) {
            intent = new Intent(this, FacultyDashboardActivity.class);
        } else {
            intent = new Intent(this, StudentDashboardActivity.class);
        }
        startActivity(intent);
        finish();
    }
}