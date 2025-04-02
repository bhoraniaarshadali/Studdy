package student;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studdy.R;
import adapter.Submission;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import adapter.StudentResultsAdapter;

public class StudentResultsActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private RecyclerView resultsRecyclerView;
    private StudentResultsAdapter resultsAdapter;
    private List<Submission> submissionList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_results);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        resultsRecyclerView = findViewById(R.id.resultsRecyclerView);
        submissionList = new ArrayList<>();
        resultsAdapter = new StudentResultsAdapter(submissionList);
        resultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        resultsRecyclerView.setAdapter(resultsAdapter);

        fetchStudentResults();
    }

    private void fetchStudentResults() {
        if (auth.getCurrentUser() == null) {
            finish();
            return;
        }

        String studentEmail = auth.getCurrentUser().getEmail();
        db.collection("submissions")
                .whereEqualTo("studentEmail", studentEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        submissionList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Submission submission = document.toObject(Submission.class);
                            submissionList.add(submission);
                        }
                        if (submissionList.isEmpty()) {
                            Snackbar.make(resultsRecyclerView, "No results found", Snackbar.LENGTH_LONG).show();
                        }
                        resultsAdapter.notifyDataSetChanged();
                    } else {
                        Snackbar.make(resultsRecyclerView, "Error fetching results", Snackbar.LENGTH_LONG).show();
                    }
                });
    }
}