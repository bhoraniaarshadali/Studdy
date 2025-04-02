package adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studdy.R;

import java.util.ArrayList;
import java.util.List;

public class SelectQuestionAdapter extends RecyclerView.Adapter<SelectQuestionAdapter.SelectQuestionViewHolder> {

    private List<Question> questionList;
    private List<String> selectedQuestionIds;

    public SelectQuestionAdapter(List<Question> questionList) {
        this.questionList = questionList;
        this.selectedQuestionIds = new ArrayList<>();
    }

    @NonNull
    @Override
    public SelectQuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_select_question, parent, false);
        return new SelectQuestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SelectQuestionViewHolder holder, int position) {
        Question question = questionList.get(position);
        holder.questionTextView.setText(question.getQuestionText());
        holder.subjectTextView.setText("Subject: " + question.getSubject());

        holder.selectCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedQuestionIds.add(question.getQuestionId());
            } else {
                selectedQuestionIds.remove(question.getQuestionId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return questionList.size();
    }

    public List<String> getSelectedQuestionIds() {
        return selectedQuestionIds;
    }

    static class SelectQuestionViewHolder extends RecyclerView.ViewHolder {
        TextView questionTextView, subjectTextView;
        CheckBox selectCheckBox;

        public SelectQuestionViewHolder(@NonNull View itemView) {
            super(itemView);
            questionTextView = itemView.findViewById(R.id.questionTextView);
            subjectTextView = itemView.findViewById(R.id.subjectTextView);
            selectCheckBox = itemView.findViewById(R.id.selectCheckBox);
        }
    }
}