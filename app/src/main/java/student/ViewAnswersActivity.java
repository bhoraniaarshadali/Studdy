package student;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studdy.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import adapter.Exam;
import adapter.FeedbackAdapter;
import adapter.Question;
import adapter.Submission;

public class ViewAnswersActivity extends AppCompatActivity {

    private TextView studentEmailTextView;
    private RecyclerView answersRecyclerView;
    private List<Question> questionList;
    private FeedbackAdapter feedbackAdapter;
    private FirebaseFirestore db;
    private String examId;
    private String submissionId;
    private Map<String, String> answers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_answers);

        db = FirebaseFirestore.getInstance();

        studentEmailTextView = findViewById(R.id.studentEmailTextView);
        answersRecyclerView = findViewById(R.id.answersRecyclerView);

        submissionId = getIntent().getStringExtra("submissionId");
        String studentEmail = getIntent().getStringExtra("studentEmail");
        examId = getIntent().getStringExtra("examId");

        studentEmailTextView.setText(studentEmail);

        questionList = new ArrayList<>();
        answersRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadSubmission();
    }

    private void loadSubmission() {
        db.collection("submissions").document(submissionId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Submission submission = document.toObject(Submission.class);
                            answers = submission.getAnswers();
                            loadQuestions();
                        }
                    } else {
                        Snackbar.make(answersRecyclerView, "Error loading submission", Snackbar.LENGTH_LONG).show();
                    }
                });
    }

    private void loadQuestions() {
        db.collection("exams").document(examId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Exam exam = document.toObject(Exam.class);
                            List<String> questionIds = exam.getQuestionIds();
                            for (String questionId : questionIds) {
                                db.collection("questions").document(questionId)
                                        .get()
                                        .addOnCompleteListener(questionTask -> {
                                            if (questionTask.isSuccessful()) {
                                                DocumentSnapshot questionDoc = questionTask.getResult();
                                                if (questionDoc.exists()) {
                                                    Question question = questionDoc.toObject(Question.class);
                                                    question.setQuestionId(questionDoc.getId());
                                                    questionList.add(question);
                                                    if (questionList.size() == questionIds.size()) {
                                                        feedbackAdapter = new FeedbackAdapter(questionList, answers, submissionId);
                                                        answersRecyclerView.setAdapter(feedbackAdapter);
                                                    }
                                                }
                                            }
                                        });
                            }
                        }
                    } else {
                        Snackbar.make(answersRecyclerView, "Error loading exam", Snackbar.LENGTH_LONG).show();
                    }
                });
    }
}