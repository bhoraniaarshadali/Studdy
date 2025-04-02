package faculty;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

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
import java.util.UUID;

import adapter.Exam;
import adapter.Question;
import adapter.SelectQuestionAdapter;

public class CreateExamActivity extends AppCompatActivity {

    private EditText examTitleEditText, durationEditText, totalMarksEditText;
    private RecyclerView selectQuestionsRecyclerView;
    private Button createExamButton;
    private List<Question> questionList;
    private SelectQuestionAdapter selectQuestionAdapter;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_exam);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        examTitleEditText = findViewById(R.id.examTitleEditText);
        durationEditText = findViewById(R.id.durationEditText);
        totalMarksEditText = findViewById(R.id.totalMarksEditText);
        selectQuestionsRecyclerView = findViewById(R.id.selectQuestionsRecyclerView);
        createExamButton = findViewById(R.id.createExamButton);

        questionList = new ArrayList<>();
        selectQuestionAdapter = new SelectQuestionAdapter(questionList);
        selectQuestionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        selectQuestionsRecyclerView.setAdapter(selectQuestionAdapter);

        loadQuestions();

        createExamButton.setOnClickListener(v -> createExam());
    }

    private void loadQuestions() {
        if (auth.getCurrentUser() == null) {
            finish();
            return;
        }

        String facultyEmail = auth.getCurrentUser().getEmail();
        db.collection("questions")
                .whereEqualTo("createdBy", facultyEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        questionList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Question question = document.toObject(Question.class);
                            question.setQuestionId(document.getId());
                            questionList.add(question);
                        }
                        selectQuestionAdapter.notifyDataSetChanged();
                    } else {
                        Snackbar.make(selectQuestionsRecyclerView, "Error loading questions", Snackbar.LENGTH_LONG).show();
                    }
                });
    }

    private void createExam() {
        String title = examTitleEditText.getText().toString().trim();
        String durationStr = durationEditText.getText().toString().trim();
        String totalMarksStr = totalMarksEditText.getText().toString().trim();
        List<String> selectedQuestionIds = selectQuestionAdapter.getSelectedQuestionIds();

        if (title.isEmpty() || durationStr.isEmpty() || totalMarksStr.isEmpty()) {
            Snackbar.make(createExamButton, "Please fill all fields", Snackbar.LENGTH_LONG).show();
            return;
        }

        if (selectedQuestionIds.isEmpty()) {
            Snackbar.make(createExamButton, "Please select at least one question", Snackbar.LENGTH_LONG).show();
            return;
        }

        long duration = Long.parseLong(durationStr);
        long totalMarks = Long.parseLong(totalMarksStr);

        String examId = UUID.randomUUID().toString();
        Exam exam = new Exam(
                examId,
                title,
                duration,
                totalMarks,
                auth.getCurrentUser().getEmail(),
                selectedQuestionIds
        );

        db.collection("exams").document(examId)
                .set(exam)
                .addOnSuccessListener(aVoid -> {
                    Snackbar.make(createExamButton, "Exam created successfully", Snackbar.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Snackbar.make(createExamButton, "Error creating exam", Snackbar.LENGTH_LONG).show();
                });
    }
}