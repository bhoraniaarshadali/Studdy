package com.example.studdy;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import adapter.Question;

public class ExamQuestionAdapter extends RecyclerView.Adapter<ExamQuestionAdapter.ExamQuestionViewHolder> {

    private List<Question> questionList;
    private Map<String, String> answers;

    public ExamQuestionAdapter(List<Question> questionList) {
        this.questionList = questionList;
        this.answers = new HashMap<>();
    }

    @NonNull
    @Override
    public ExamQuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_exam_question, parent, false);
        return new ExamQuestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExamQuestionViewHolder holder, int position) {
        Question question = questionList.get(position);
        holder.questionTextView.setText((position + 1) + ". " + question.getQuestionText());
        holder.optionA.setText("A: " + question.getOptions().get(0));
        holder.optionB.setText("B: " + question.getOptions().get(1));
        holder.optionC.setText("C: " + question.getOptions().get(2));
        holder.optionD.setText("D: " + question.getOptions().get(3));

        holder.optionsRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String selectedAnswer = "";
            if (checkedId == R.id.radioButton1) {
                selectedAnswer = "A";
            } else if (checkedId == R.id.radioButton2) {
                selectedAnswer = "B";
            } else if (checkedId == R.id.radioButton3) {
                selectedAnswer = "C";
            } else if (checkedId == R.id.radioButton4) {
                selectedAnswer = "D";
            }
            answers.put(question.getQuestionId(), selectedAnswer);
        });
    }

    @Override
    public int getItemCount() {
        return questionList.size();
    }

    public Map<String, String> getAnswers() {
        return answers;
    }

    static class ExamQuestionViewHolder extends RecyclerView.ViewHolder {
        TextView questionTextView;
        RadioGroup optionsRadioGroup;
        RadioButton optionA, optionB, optionC, optionD;

        public ExamQuestionViewHolder(@NonNull View itemView) {
            super(itemView);
            questionTextView = itemView.findViewById(R.id.questionTextView);
            optionsRadioGroup = itemView.findViewById(R.id.optionsRadioGroup);
            optionA = itemView.findViewById(R.id.radioButton1);
            optionB = itemView.findViewById(R.id.radioButton2);
            optionC = itemView.findViewById(R.id.radioButton3);
            optionD = itemView.findViewById(R.id.radioButton4);
        }
    }
}