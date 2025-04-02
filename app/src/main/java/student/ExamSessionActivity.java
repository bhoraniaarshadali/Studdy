package student;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studdy.ExamQuestionAdapter;
import com.example.studdy.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import adapter.Exam;
import adapter.Question;
import adapter.Submission;

public class ExamSessionActivity extends AppCompatActivity {

    private TextView examTitleTextView, timerTextView;
    private RecyclerView questionsRecyclerView;
    private Button submitButton;
    private List<Question> questionList;
    private ExamQuestionAdapter questionAdapter;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String examId;
    private long durationInMinutes;
    private long totalMarks;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam_session);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        examTitleTextView = findViewById(R.id.examTitleTextView);
        timerTextView = findViewById(R.id.timerTextView);
        questionsRecyclerView = findViewById(R.id.questionsRecyclerView);
        submitButton = findViewById(R.id.submitButton);

        examId = getIntent().getStringExtra("examId");
        String examTitle = getIntent().getStringExtra("examTitle");
        durationInMinutes = getIntent().getLongExtra("duration", 0);
        totalMarks = getIntent().getLongExtra("totalMarks", 0);

        examTitleTextView.setText(examTitle);

        questionList = new ArrayList<>();
        questionAdapter = new ExamQuestionAdapter(questionList);
        questionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        questionsRecyclerView.setAdapter(questionAdapter);

        startTimer();
        loadQuestions();

        submitButton.setOnClickListener(v -> submitExam());
    }

    private void startTimer() {
        long durationInMillis = durationInMinutes * 60 * 1000;
        countDownTimer = new CountDownTimer(durationInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = (millisUntilFinished / 1000) / 60;
                long seconds = (millisUntilFinished / 1000) % 60;
                timerTextView.setText(String.format("Time Remaining: %02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                timerTextView.setText("Time's Up!");
                submitExam();
            }
        }.start();
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
                            if (questionIds == null || questionIds.isEmpty()) {
                                Snackbar.make(questionsRecyclerView, "No questions found for this exam", Snackbar.LENGTH_LONG).show();
                                return;
                            }
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
                                                    questionAdapter.notifyDataSetChanged();
                                                }
                                            }
                                        });
                            }
                        } else {
                            Snackbar.make(questionsRecyclerView, "Exam not found", Snackbar.LENGTH_LONG).show();
                        }
                    } else {
                        Snackbar.make(questionsRecyclerView, "Error loading exam", Snackbar.LENGTH_LONG).show();
                    }
                });
    }

    private void submitExam() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        if (auth.getCurrentUser() == null) {
            finish();
            return;
        }

        if (questionList.isEmpty()) {
            Snackbar.make(submitButton, "No questions to submit", Snackbar.LENGTH_LONG).show();
            finish();
            return;
        }

        // Calculate score
        long score = 0;
        long marksPerQuestion = totalMarks / questionList.size();
        for (Question question : questionList) {
            String selectedAnswer = questionAdapter.getAnswers().get(question.getQuestionId());
            if (selectedAnswer != null && selectedAnswer.equals(question.getCorrectAnswer())) {
                score += marksPerQuestion;
            }
        }

        // Create submission
        String submissionId = UUID.randomUUID().toString();
        Submission submission = new Submission(
                submissionId,
                examId,
                auth.getCurrentUser().getEmail(),
                questionAdapter.getAnswers(),
                Timestamp.now(),
                score
        );

        // Save submission to Firestore
        long finalScore = score;
        db.collection("submissions").document(submissionId)
                .set(submission)
                .addOnSuccessListener(aVoid -> {
                    Snackbar.make(submitButton, "Exam submitted successfully! Score: " + finalScore + "/" + totalMarks, Snackbar.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Snackbar.make(submitButton, "Error submitting exam", Snackbar.LENGTH_LONG).show();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}