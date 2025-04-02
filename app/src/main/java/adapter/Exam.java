package adapter;

import java.util.List;

public class Exam {
    private String examId;
    private String title;
    private long duration;
    private long totalMarks;
    private String createdBy;
    private List<String> questionIds;

    public Exam() {
    }

    public Exam(String examId, String title, long duration, long totalMarks, String createdBy, List<String> questionIds) {
        this.examId = examId;
        this.title = title;
        this.duration = duration;
        this.totalMarks = totalMarks;
        this.createdBy = createdBy;
        this.questionIds = questionIds;
    }

    // Getters and setters
    public String getExamId() {
        return examId;
    }

    public void setExamId(String examId) {
        this.examId = examId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(long totalMarks) {
        this.totalMarks = totalMarks;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public List<String> getQuestionIds() {
        return questionIds;
    }

    public void setQuestionIds(List<String> questionIds) {
        this.questionIds = questionIds;
    }
}