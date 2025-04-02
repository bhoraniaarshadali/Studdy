package adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studdy.R;

import java.util.List;

public class ExamAdapter extends RecyclerView.Adapter<ExamAdapter.ExamViewHolder> {

    private List<Exam> examList;

    public ExamAdapter(List<Exam> examList) {
        this.examList = examList;
    }

    @NonNull
    @Override
    public ExamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_exam, parent, false);
        return new ExamViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExamViewHolder holder, int position) {
        Exam exam = examList.get(position);
        holder.examTitleTextView.setText(exam.getTitle());
        holder.durationTextView.setText("Duration: " + exam.getDuration() + " minutes");
        holder.totalMarksTextView.setText("Total Marks: " + exam.getTotalMarks());

        holder.viewSubmissionsButton.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), student.ViewSubmissionsActivity.class);
            intent.putExtra("examId", exam.getExamId());
            intent.putExtra("examTitle", exam.getTitle());
            intent.putExtra("totalMarks", exam.getTotalMarks());
            holder.itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return examList.size();
    }

    static class ExamViewHolder extends RecyclerView.ViewHolder {
        TextView examTitleTextView, durationTextView, totalMarksTextView;
        Button viewSubmissionsButton;

        public ExamViewHolder(@NonNull View itemView) {
            super(itemView);
            examTitleTextView = itemView.findViewById(R.id.examTitleTextView);
            durationTextView = itemView.findViewById(R.id.durationTextView);
            totalMarksTextView = itemView.findViewById(R.id.totalMarksTextView);
            viewSubmissionsButton = itemView.findViewById(R.id.viewSubmissionsButton);
        }
    }
}