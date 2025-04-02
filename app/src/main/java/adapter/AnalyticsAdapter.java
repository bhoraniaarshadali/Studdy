package adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studdy.R;

import java.util.List;

public class AnalyticsAdapter extends RecyclerView.Adapter<AnalyticsAdapter.AnalyticsViewHolder> {

    private List<ExamAnalytics> analyticsList;

    public AnalyticsAdapter(List<ExamAnalytics> analyticsList) {
        this.analyticsList = analyticsList;
    }

    @NonNull
    @Override
    public AnalyticsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_analytics, parent, false);
        return new AnalyticsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AnalyticsViewHolder holder, int position) {
        ExamAnalytics analytics = analyticsList.get(position);
        holder.examTitleTextView.setText(analytics.getExamTitle());
        holder.averageScoreTextView.setText("Average Score: " + analytics.getAverageScore());
        holder.highestScoreTextView.setText("Highest Score: " + analytics.getHighestScore());
        holder.lowestScoreTextView.setText("Lowest Score: " + analytics.getLowestScore());
    }

    @Override
    public int getItemCount() {
        return analyticsList.size();
    }

    static class AnalyticsViewHolder extends RecyclerView.ViewHolder {
        TextView examTitleTextView, averageScoreTextView, highestScoreTextView, lowestScoreTextView;

        public AnalyticsViewHolder(@NonNull View itemView) {
            super(itemView);
            examTitleTextView = itemView.findViewById(R.id.examTitleTextView);
            averageScoreTextView = itemView.findViewById(R.id.averageScoreTextView);
            highestScoreTextView = itemView.findViewById(R.id.highestScoreTextView);
            lowestScoreTextView = itemView.findViewById(R.id.lowestScoreTextView);
        }
    }
}