package com.example.studdy;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.View;
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
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.regex.Pattern;

import faculty.FacultyDashboardActivity;
import student.StudentDashboardActivity;

public class LoginActivity extends AppCompatActivity {

    private static final Pattern STAFF_CODE_PATTERN = Pattern.compile("^[A-Z0-9]{6,}$"); // Uppercase, more than 6 characters
    private RadioGroup roleRadioGroup;
    private EditText usernameEditText, passwordEditText;
    private ImageView passwordToggle, facultyRegistration;
    private AppCompatButton signInButton;
    private SignInButton googleSignInButton;
    private TextView forgotPasswordTextView;
    private boolean isPasswordVisible = false;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private Dialog loadingDialog;
    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Initialize views
        roleRadioGroup = findViewById(R.id.roleRadioGroup);
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        passwordToggle = findViewById(R.id.passwordToggle);
        facultyRegistration = findViewById(R.id.facultyRegistration);
        signInButton = findViewById(R.id.signInButton);
        googleSignInButton = findViewById(R.id.googleSignInButton);
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);

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

// Update hint based on role selection
        roleRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.facultyRadioButton) {
                usernameEditText.setHint("Enter Staff Code");
                usernameEditText.setInputType(InputType.TYPE_CLASS_TEXT);

                // Apply uppercase filter and max length of 6
                usernameEditText.setFilters(new InputFilter[]{
                        new InputFilter.AllCaps(),  // Converts input to uppercase
                        new InputFilter.LengthFilter(6)  // Restricts input to max 6 characters
                });

            } else {
                usernameEditText.setHint("Enter University Email");
                usernameEditText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

                // Remove restrictions for email input
                usernameEditText.setFilters(new InputFilter[]{});
            }
        });


        // Faculty registration click
        facultyRegistration.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Select Role")
                    .setItems(new String[]{"Faculty", "Student"}, (dialog, which) -> {
                        if (which == 0) {
                            startActivity(new Intent(LoginActivity.this, FacultyCodeRegistrationActivity.class));
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
            String input = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (!validateInputs(role, input, password)) {
                return;
            }

            loadingDialog.show();
            authenticateUser(role, input, password);
        });

        // Google sign-in button click
        googleSignInButton.setOnClickListener(v -> {
            loadingDialog.show();
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, 100);
        });

        forgotPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(LoginActivity.this, "Forgot Password Clicked", Toast.LENGTH_SHORT).show();
            }
        });

//        forgotPasswordTextView.setOnClickListener(v -> {
//            String email = usernameEditText.getText().toString().trim();
//
//            if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
//                usernameEditText.setError("Enter a valid email address");
//                return;
//            }
//
//            // Check if the email belongs to a faculty member
//            db.collection("faculty")
//                    .whereEqualTo("email", email)
//                    .get()
//                    .addOnCompleteListener(task -> {
//                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
//                            // Faculty found, restrict reset
//                            Toast.makeText(LoginActivity.this, "Faculty must contact the admin for password reset", Toast.LENGTH_SHORT).show();
//                        } else {
//                            // Email is not in faculty collection, allow password reset
//                            loadingDialog.show();
//                            auth.sendPasswordResetEmail(email)
//                                    .addOnCompleteListener(task1 -> {
//                                        loadingDialog.dismiss();
//                                        if (task1.isSuccessful()) {
//                                            Toast.makeText(LoginActivity.this, "Password reset email sent", Toast.LENGTH_SHORT).show();
//                                        } else {
//                                            Toast.makeText(LoginActivity.this, "Failed to send reset email: " + task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
//                                        }
//                                    });
//                        }
//                    });
//        });

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
                usernameEditText.setError("Staff code must be uppercase and more than 6 characters");
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
            // Fetch email from Firestore using staff code
            db.collection("faculty")
                    .whereEqualTo("staff_code", input)
                    .get()
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
            // For student, use the provided email
            signInWithEmail(input, password, role);
        }
    }

    private void signInWithEmail(String email, String password, String role) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        validateUserRoleFromFirestore(role, email);

                        // Update Firestore password if the role is "Student"
                        if (role.equals("Student")) {
                            updatePasswordInFirestore(email, password);
                        }
                    } else {
                        loadingDialog.dismiss();
                        Toast.makeText(LoginActivity.this,
                                "Sign in failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Function to update password in Firestore
    private void updatePasswordInFirestore(String email, String newPassword) {
        db.collection("students")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        String documentId = task.getResult().getDocuments().get(0).getId();
                        db.collection("students").document(documentId)
                                .update("password", newPassword)
                                .addOnSuccessListener(aVoid ->
                                        Toast.makeText(LoginActivity.this, "Password updated in Firestore", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e ->
                                        Toast.makeText(LoginActivity.this, "Failed to update password in Firestore", Toast.LENGTH_SHORT).show());
                    }
                });
    }


    private void validateUserRoleFromFirestore(String role, String email) {
        String collection = role.equals("Faculty") ? "faculty" : "students";
        db.collection(collection)
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    loadingDialog.dismiss();
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        Toast.makeText(LoginActivity.this, "Sign in successful as " + role, Toast.LENGTH_SHORT).show();
                        navigateBasedOnRole(role);
                    } else {
                        auth.signOut(); // Sign out if role validation fails
                        Toast.makeText(LoginActivity.this, "User not found in " + role + " records", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigateBasedOnRole(String role) {
        Intent intent;
        if (role.equals("Faculty")) {
            Toast.makeText(LoginActivity.this, "Faculty", Toast.LENGTH_SHORT).show();
            intent = new Intent(this, FacultyDashboardActivity.class);
        } else {
            Toast.makeText(LoginActivity.this, "Student", Toast.LENGTH_SHORT).show();
            intent = new Intent(this, StudentDashboardActivity.class);
        }
        startActivity(intent);
        finish();
    }
}