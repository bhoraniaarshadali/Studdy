package faculty;

import android.os.Bundle;

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

import adapter.Exam;
import adapter.ExamAdapter;

public class FacultyResultsActivity extends AppCompatActivity {

    private RecyclerView examsRecyclerView;
    private List<Exam> examList;
    private ExamAdapter examAdapter;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faculty_results);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        examsRecyclerView = findViewById(R.id.examsRecyclerView);

        examList = new ArrayList<>();
        examAdapter = new ExamAdapter(examList);
        examsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        examsRecyclerView.setAdapter(examAdapter);

        loadExams();
    }

    private void loadExams() {
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
                        examList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Exam exam = document.toObject(Exam.class);
                            exam.setExamId(document.getId());
                            examList.add(exam);
                        }
                        examAdapter.notifyDataSetChanged();
                    } else {
                        Snackbar.make(examsRecyclerView, "Error loading exams", Snackbar.LENGTH_LONG).show();
                    }
                });
    }
}