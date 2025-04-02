package student;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.studdy.LoginActivity;
import com.example.studdy.R;
import com.example.studdy.TakeExamActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class StudentDashboardActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private TextView welcomeTextView;
    private BottomNavigationView bottomNavigationView;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "MyPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Initialize views
        welcomeTextView = findViewById(R.id.welcomeTextView);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Fetch and display the student's name
        if (auth.getCurrentUser() != null) {
            String email = auth.getCurrentUser().getEmail();
            fetchStudentName(email);
        } else {
            Intent intent = new Intent(StudentDashboardActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

//        // Setup Bottom Navigation
//        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
//            switch (item.getItemId()) {
//                case R.id.s_nav_home:
//                    // Already on home, do nothing
//                    return true;
//                case R.id.s_nav_profile:
//                    // Navigate to Profile (you can create a ProfileActivity later)
//                    Snackbar.make(bottomNavigationView, "Profile feature not implemented yet", Snackbar.LENGTH_SHORT).show();
//                    return true;
//                case R.id.s_nav_logout:
//                    auth.signOut();
//                    SharedPreferences.Editor editor = sharedPreferences.edit();
//                    editor.clear();
//                    editor.apply();
//                    Intent intent = new Intent(StudentDashboardActivity.this, LoginActivity.class);
//                    startActivity(intent);
//                    finish();
//                    return true;
//            }
//            return false;
//        });
    }

    private void fetchStudentName(String email) {
        db.collection("students")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            String name = querySnapshot.getDocuments().get(0).getString("name");
                            if (name != null && !name.isEmpty()) {
                                welcomeTextView.setText("Welcome, " + name + "!");
                            } else {
                                welcomeTextView.setText("Welcome, Student!\n" + email);
                            }
                        } else {
                            welcomeTextView.setText("Welcome, Student!\n" + email);
                            Snackbar.make(welcomeTextView, "Student data not found", Snackbar.LENGTH_LONG).show();
                        }
                    } else {
                        welcomeTextView.setText("Welcome, Student!\n" + email);
                        Snackbar.make(welcomeTextView, "Error fetching student data", Snackbar.LENGTH_LONG).show();
                    }
                });
    }

    // Methods for card click events
    public void openTakeExam(View view) {
        startActivity(new Intent(StudentDashboardActivity.this, TakeExamActivity.class));
    }

    public void openResults(View view) {
        startActivity(new Intent(StudentDashboardActivity.this, student.StudentResultsActivity.class));
    }
}