package student;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studdy.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import adapter.Submission;
import adapter.SubmissionAdapter;

public class ViewSubmissionsActivity extends AppCompatActivity {

    private TextView examTitleTextView;
    private RecyclerView submissionsRecyclerView;
    private List<Submission> submissionList;
    private SubmissionAdapter submissionAdapter;
    private FirebaseFirestore db;
    private String examId;
    private long totalMarks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_submissions);

        db = FirebaseFirestore.getInstance();

        examTitleTextView = findViewById(R.id.examTitleTextView);
        submissionsRecyclerView = findViewById(R.id.submissionsRecyclerView);

        examId = getIntent().getStringExtra("examId");
        String examTitle = getIntent().getStringExtra("examTitle");
        totalMarks = getIntent().getLongExtra("totalMarks", 0);

        examTitleTextView.setText(examTitle);

        submissionList = new ArrayList<>();
        submissionAdapter = new SubmissionAdapter(submissionList, totalMarks);
        submissionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        submissionsRecyclerView.setAdapter(submissionAdapter);

        loadSubmissions();
    }

    private void loadSubmissions() {
        db.collection("submissions")
                .whereEqualTo("examId", examId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        submissionList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Submission submission = document.toObject(Submission.class);
                            submission.setSubmissionId(document.getId());
                            submissionList.add(submission);
                        }
                        submissionAdapter.notifyDataSetChanged();
                    } else {
                        Snackbar.make(submissionsRecyclerView, "Error loading submissions", Snackbar.LENGTH_LONG).show();
                    }
                });
    }
}