package adapter;

public class ExamAnalytics {
    private String examId;
    private String examTitle;
    private double averageScore;
    private long highestScore;
    private long lowestScore;

    public ExamAnalytics(String examId, String examTitle, double averageScore, long highestScore, long lowestScore) {
        this.examId = examId;
        this.examTitle = examTitle;
        this.averageScore = averageScore;
        this.highestScore = highestScore;
        this.lowestScore = lowestScore;
    }

    // Getters
    public String getExamId() {
        return examId;
    }

    public String getExamTitle() {
        return examTitle;
    }

    public double getAverageScore() {
        return averageScore;
    }

    public long getHighestScore() {
        return highestScore;
    }

    public long getLowestScore() {
        return lowestScore;
    }
}