package faculty;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studdy.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import adapter.Question;
import adapter.QuestionAdapter;

public class QuestionBankActivity extends AppCompatActivity {

    private RecyclerView questionsRecyclerView;
    private Button addQuestionButton;
    private List<Question> questionList;
    private QuestionAdapter questionAdapter;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_bank);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        questionsRecyclerView = findViewById(R.id.questionsRecyclerView);
        addQuestionButton = findViewById(R.id.addQuestionButton);

        questionList = new ArrayList<>();
        questionAdapter = new QuestionAdapter(questionList);
        questionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        questionsRecyclerView.setAdapter(questionAdapter);

        loadQuestions();

        addQuestionButton.setOnClickListener(v -> showAddQuestionDialog());
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
                        questionAdapter.notifyDataSetChanged();
                    } else {
                        Snackbar.make(questionsRecyclerView, "Error loading questions", Snackbar.LENGTH_LONG).show();
                    }
                });
    }

    private void showAddQuestionDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_question);
        dialog.setCancelable(false);

        EditText questionTextEditText = dialog.findViewById(R.id.questionTextEditText);
        EditText optionAEditText = dialog.findViewById(R.id.optionAEditText);
        EditText optionBEditText = dialog.findViewById(R.id.optionBEditText);
        EditText optionCEditText = dialog.findViewById(R.id.optionCEditText);
        EditText optionDEditText = dialog.findViewById(R.id.optionDEditText);
        Spinner correctAnswerSpinner = dialog.findViewById(R.id.correctAnswerSpinner);
        EditText subjectEditText = dialog.findViewById(R.id.subjectEditText);
        Button cancelButton = dialog.findViewById(R.id.cancelButton);
        Button saveButton = dialog.findViewById(R.id.saveButton);

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        saveButton.setOnClickListener(v -> {
            String questionText = questionTextEditText.getText().toString().trim();
            String optionA = optionAEditText.getText().toString().trim();
            String optionB = optionBEditText.getText().toString().trim();
            String optionC = optionCEditText.getText().toString().trim();
            String optionD = optionDEditText.getText().toString().trim();
            String correctAnswer = correctAnswerSpinner.getSelectedItem().toString();
            String subject = subjectEditText.getText().toString().trim();

            if (questionText.isEmpty() || optionA.isEmpty() || optionB.isEmpty() ||
                    optionC.isEmpty() || optionD.isEmpty() || subject.isEmpty()) {
                Snackbar.make(saveButton, "Please fill all fields", Snackbar.LENGTH_LONG).show();
                return;
            }

            List<String> options = Arrays.asList(optionA, optionB, optionC, optionD);
            String questionId = UUID.randomUUID().toString();
            Question question = new Question(
                    questionId,
                    questionText,
                    options,
                    correctAnswer,
                    auth.getCurrentUser().getEmail(),
                    subject
            );

            db.collection("questions").document(questionId)
                    .set(question)
                    .addOnSuccessListener(aVoid -> {
                        dialog.dismiss();
                        loadQuestions();
                        Snackbar.make(questionsRecyclerView, "Question added successfully", Snackbar.LENGTH_LONG).show();
                    })
                    .addOnFailureListener(e -> {
                        Snackbar.make(questionsRecyclerView, "Error adding question", Snackbar.LENGTH_LONG).show();
                    });
        });

        dialog.show();
    }
}