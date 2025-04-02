package adapter;

import com.google.firebase.Timestamp;

import java.util.Map;

public class Submission {
    private String submissionId;
    private String examId;
    private String studentEmail;
    private Map<String, String> answers;
    private Timestamp timestamp;
    private long score;

    public Submission() {
    }

    public Submission(String submissionId, String examId, String studentEmail, Map<String, String> answers, Timestamp timestamp, long score) {
        this.submissionId = submissionId;
        this.examId = examId;
        this.studentEmail = studentEmail;
        this.answers = answers;
        this.timestamp = timestamp;
        this.score = score;
    }

    public String getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(String submissionId) {
        this.submissionId = submissionId;
    }

    public String getExamId() {
        return examId;
    }

    public void setExamId(String examId) {
        this.examId = examId;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }

    public Map<String, String> getAnswers() {
        return answers;
    }

    public void setAnswers(Map<String, String> answers) {
        this.answers = answers;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public long getScore() {
        return score;
    }

    public void setScore(long score) {
        this.score = score;
    }
}