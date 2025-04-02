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

public class SubmissionAdapter extends RecyclerView.Adapter<SubmissionAdapter.SubmissionViewHolder> {

    private List<Submission> submissionList;
    private long totalMarks;

    public SubmissionAdapter(List<Submission> submissionList, long totalMarks) {
        this.submissionList = submissionList;
        this.totalMarks = totalMarks;
    }

    @NonNull
    @Override
    public SubmissionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_submission, parent, false);
        return new SubmissionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubmissionViewHolder holder, int position) {
        Submission submission = submissionList.get(position);
        holder.studentEmailTextView.setText(submission.getStudentEmail());
        holder.scoreTextView.setText("Score: " + submission.getScore() + "/" + totalMarks);

        holder.viewAnswersButton.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), student.ViewAnswersActivity.class);
            intent.putExtra("submissionId", submission.getSubmissionId());
            intent.putExtra("studentEmail", submission.getStudentEmail());
            intent.putExtra("examId", submission.getExamId());
            holder.itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return submissionList.size();
    }

    static class SubmissionViewHolder extends RecyclerView.ViewHolder {
        TextView studentEmailTextView, scoreTextView;
        Button viewAnswersButton;

        public SubmissionViewHolder(@NonNull View itemView) {
            super(itemView);
            studentEmailTextView = itemView.findViewById(R.id.studentEmailTextView);
            scoreTextView = itemView.findViewById(R.id.scoreTextView);
            viewAnswersButton = itemView.findViewById(R.id.viewAnswersButton);
        }
    }
}