package faculty;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.studdy.LoginActivity;
import com.example.studdy.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class FacultyDashboardActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "MyPrefs";
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private TextView welcomeTextView;
    private BottomNavigationView bottomNavigationView;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faculty_dashboard);

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Initialize views
        welcomeTextView = findViewById(R.id.welcomeTextView);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Fetch and display the faculty's name
        if (auth.getCurrentUser() != null) {
            String email = auth.getCurrentUser().getEmail();
            fetchFacultyName(email);
        } else {
            Intent intent = new Intent(FacultyDashboardActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        // Setup Bottom Navigation
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.s_nav_home) {
                return true;
            } else if (itemId == R.id.s_nav_profile) {
                Snackbar.make(bottomNavigationView, "Profile feature not implemented yet", Snackbar.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.s_nav_logout) {
                auth.signOut();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.apply();
                Intent intent = new Intent(FacultyDashboardActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });

    }

    private void fetchFacultyName(String email) {
        db.collection("faculty")
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
                                welcomeTextView.setText("Welcome, Faculty!\n" + email);
                            }
                        } else {
                            welcomeTextView.setText("Welcome, Faculty!\n" + email);
                            Snackbar.make(welcomeTextView, "Faculty data not found", Snackbar.LENGTH_LONG).show();
                        }
                    } else {
                        welcomeTextView.setText("Welcome, Faculty!\n" + email);
                        Snackbar.make(welcomeTextView, "Error fetching faculty data", Snackbar.LENGTH_LONG).show();
                    }
                });
    }

    // Methods for card click events
    public void openQuestionBank(View view) {
        startActivity(new Intent(FacultyDashboardActivity.this, faculty.QuestionBankActivity.class));
    }

    public void openCreateExam(View view) {
        startActivity(new Intent(FacultyDashboardActivity.this, faculty.CreateExamActivity.class));
    }

    public void openResults(View view) {
        startActivity(new Intent(FacultyDashboardActivity.this, faculty.FacultyResultsActivity.class));
    }

    public void openAnalytics(View view) {
        startActivity(new Intent(FacultyDashboardActivity.this, faculty.FacultyAnalyticsActivity.class));
    }
}