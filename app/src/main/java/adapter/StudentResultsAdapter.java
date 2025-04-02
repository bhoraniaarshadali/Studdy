package adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studdy.R;
import adapter.Submission;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class StudentResultsAdapter extends RecyclerView.Adapter<StudentResultsAdapter.ResultViewHolder> {

    private List<Submission> submissionList;
    private SimpleDateFormat dateFormat;

    public StudentResultsAdapter(List<Submission> submissionList) {
        this.submissionList = submissionList;
        this.dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
    }

    @NonNull
    @Override
    public ResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student_result, parent, false);
        return new ResultViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ResultViewHolder holder, int position) {
        Submission submission = submissionList.get(position);
        holder.examTitleTextView.setText("Exam ID: " + submission.getExamId()); // You can fetch the exam title from Firestore if needed
        holder.scoreTextView.setText("Score: " + submission.getScore() + "/100"); // Adjust based on your total marks
        holder.dateTextView.setText("Submitted: " + dateFormat.format(submission.getTimestamp().toDate()));
    }

    @Override
    public int getItemCount() {
        return submissionList.size();
    }

    static class ResultViewHolder extends RecyclerView.ViewHolder {
        TextView examTitleTextView, scoreTextView, dateTextView;

        public ResultViewHolder(@NonNull View itemView) {
            super(itemView);
            examTitleTextView = itemView.findViewById(R.id.examTitleTextView);
            scoreTextView = itemView.findViewById(R.id.scoreTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
        }
    }
}