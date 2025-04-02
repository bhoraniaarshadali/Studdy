package faculty;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studdy.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import adapter.AnalyticsAdapter;
import adapter.Exam;
import adapter.ExamAnalytics;
import adapter.Submission;

public class FacultyAnalyticsActivity extends AppCompatActivity {

    private RecyclerView analyticsRecyclerView;
    private List<ExamAnalytics> analyticsList;
    private AnalyticsAdapter analyticsAdapter;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private Exam exam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faculty_analytics);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        analyticsRecyclerView = findViewById(R.id.analyticsRecyclerView);

        analyticsList = new ArrayList<>();
        analyticsAdapter = new AnalyticsAdapter(analyticsList);
        analyticsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        analyticsRecyclerView.setAdapter(analyticsAdapter);

        loadAnalytics();
    }

    private void loadAnalytics() {
        if (auth.getCurrentUser() == null) {
            finish();
            return;
        }

        String facultyEmail = auth.getCurrentUser().getEmail();
        db.collection("exams")
                .whereEqualTo("createdBy", facultyEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        analyticsList.clear();
                        for (QueryDocumentSnapshot examDoc : task.getResult()) {
                            Exam exam = examDoc.toObject(Exam.class);
                            exam.setExamId(examDoc.getId());
                            calculateAnalytics(exam);
                        }
                    } else {
                        Snackbar.make(analyticsRecyclerView, "Error loading exams", Snackbar.LENGTH_LONG).show();
                    }
                });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void calculateAnalytics(Exam exam) {
        this.exam = exam;
        db.collection("submissions")
                .whereEqualTo("examId", exam.getExamId())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Long> scores = new ArrayList<>();
                        for (QueryDocumentSnapshot submissionDoc : task.getResult()) {
                            Submission submission = submissionDoc.toObject(Submission.class);
                            scores.add(submission.getScore());
                        }

                        if (scores.isEmpty()) {
                            return;
                        }

                        double averageScore = scores.stream().mapToLong(Long::longValue).average().orElse(0.0);
                        long highestScore = scores.stream().mapToLong(Long::longValue).max().orElse(0);
                        long lowestScore = scores.stream().mapToLong(Long::longValue).min().orElse(0);

                        ExamAnalytics analytics = new ExamAnalytics(
                                exam.getExamId(),
                                exam.getTitle(),
                                averageScore,
                                highestScore,
                                lowestScore
                        );
                        analyticsList.add(analytics);
                        analyticsAdapter.notifyDataSetChanged();
                    }
                });
    }
}