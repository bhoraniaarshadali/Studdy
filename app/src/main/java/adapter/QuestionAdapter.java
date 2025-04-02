package adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studdy.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder> {

    private List<Question> questionList;
    private FirebaseFirestore db;

    public QuestionAdapter(List<Question> questionList) {
        this.questionList = questionList;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public QuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_question, parent, false);
        return new QuestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionViewHolder holder, int position) {
        Question question = questionList.get(position);
        holder.questionTextView.setText(question.getQuestionText());
        holder.optionsTextView.setText("A: " + question.getOptions().get(0) + ", B: " + question.getOptions().get(1) +
                ", C: " + question.getOptions().get(2) + ", D: " + question.getOptions().get(3));
        holder.correctAnswerTextView.setText("Correct Answer: " + question.getCorrectAnswer());
        holder.subjectTextView.setText("Subject: " + question.getSubject());

        holder.deleteButton.setOnClickListener(v -> {
            db.collection("questions").document(question.getQuestionId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        questionList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, questionList.size());
                    })
                    .addOnFailureListener(e -> {
                        // Handle error
                    });
        });
    }

    @Override
    public int getItemCount() {
        return questionList.size();
    }

    static class QuestionViewHolder extends RecyclerView.ViewHolder {
        TextView questionTextView, optionsTextView, correctAnswerTextView, subjectTextView;
        Button deleteButton;

        public QuestionViewHolder(@NonNull View itemView) {
            super(itemView);
            questionTextView = itemView.findViewById(R.id.questionTextView);
            optionsTextView = itemView.findViewById(R.id.optionsTextView);
            correctAnswerTextView = itemView.findViewById(R.id.correctAnswerTextView);
            subjectTextView = itemView.findViewById(R.id.subjectTextView);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}