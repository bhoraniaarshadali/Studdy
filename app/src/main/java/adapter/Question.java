package adapter;

import java.util.List;

public class Question {
    private String questionId;
    private String questionText;
    private List<String> options;
    private String correctAnswer;
    private String createdBy;
    private String subject;

    public Question() {
    }

    public Question(String questionId, String questionText, List<String> options, String correctAnswer, String createdBy, String subject) {
        this.questionId = questionId;
        this.questionText = questionText;
        this.options = options;
        this.correctAnswer = correctAnswer;
        this.createdBy = createdBy;
        this.subject = subject;
    }

    // Getters and setters
    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
}