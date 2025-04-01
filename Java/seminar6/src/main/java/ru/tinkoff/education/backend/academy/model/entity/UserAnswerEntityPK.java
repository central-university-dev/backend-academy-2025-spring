package ru.tinkoff.education.backend.academy.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class UserAnswerEntityPK {

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "form_id")
    private Long formId;

    @Column(name = "question_id")
    private Long questionId;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getFormId() {
        return formId;
    }

    public void setFormId(Long formId) {
        this.formId = formId;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserAnswerEntityPK that = (UserAnswerEntityPK) o;
        return Objects.equals(userId, that.userId) && Objects.equals(formId, that.formId) && Objects.equals(questionId, that.questionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, formId, questionId);
    }
}
