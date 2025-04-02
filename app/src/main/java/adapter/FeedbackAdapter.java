package adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studdy.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeedbackAdapter extends RecyclerView.Adapter<FeedbackAdapter.FeedbackViewHolder> {

    private List<Question> questionList;
    private Map<String, String> answers;
    private String submissionId;
    private FirebaseFirestore db;

    public FeedbackAdapter(List<Question> questionList, Map<String, String> answers, String submissionId) {
        this.questionList = questionList;
        this.answers = answers;
        this.submissionId = submissionId;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public FeedbackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_feedback, parent, false);
        return new FeedbackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedbackViewHolder holder, int position) {
        Question question = questionList.get(position);
        holder.questionTextView.setText((position + 1) + ". " + question.getQuestionText());
        String selectedAnswer = answers.get(question.getQuestionId());
        holder.selectedAnswerTextView.setText("Selected Answer: " + (selectedAnswer != null ? selectedAnswer : "Not answered"));
        holder.correctAnswerTextView.setText("Correct Answer: " + question.getCorrectAnswer());

        holder.saveFeedbackButton.setOnClickListener(v -> {
            String feedback = holder.feedbackEditText.getText().toString().trim();
            if (feedback.isEmpty()) {
                Snackbar.make(holder.saveFeedbackButton, "Please provide feedback", Snackbar.LENGTH_LONG).show();
                return;
            }

            Map<String, Object> feedbackData = new HashMap<>();
            feedbackData.put("submissionId", submissionId);
            feedbackData.put("questionId", question.getQuestionId());
            feedbackData.put("feedback", feedback);

            db.collection("feedback").document(submissionId + "_" + question.getQuestionId())
                    .set(feedbackData)
                    .addOnSuccessListener(aVoid -> {
                        Snackbar.make(holder.saveFeedbackButton, "Feedback saved", Snackbar.LENGTH_LONG).show();
                    })
                    .addOnFailureListener(e -> {
                        Snackbar.make(holder.saveFeedbackButton, "Error saving feedback", Snackbar.LENGTH_LONG).show();
                    });
        });
    }

    @Override
    public int getItemCount() {
        return questionList.size();
    }

    static class FeedbackViewHolder extends RecyclerView.ViewHolder {
        TextView questionTextView, selectedAnswerTextView, correctAnswerTextView;
        EditText feedbackEditText;
        Button saveFeedbackButton;

        public FeedbackViewHolder(@NonNull View itemView) {
            super(itemView);
            questionTextView = itemView.findViewById(R.id.questionTextView);
            selectedAnswerTextView = itemView.findViewById(R.id.selectedAnswerTextView);
            correctAnswerTextView = itemView.findViewById(R.id.correctAnswerTextView);
            feedbackEditText = itemView.findViewById(R.id.feedbackEditText);
            saveFeedbackButton = itemView.findViewById(R.id.saveFeedbackButton);
        }
    }
}