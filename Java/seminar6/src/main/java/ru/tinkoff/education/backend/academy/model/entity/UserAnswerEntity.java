package ru.tinkoff.education.backend.academy.model.entity;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "user_answer")
public class UserAnswerEntity {

    @EmbeddedId
    private UserAnswerEntityPK id;

    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "form_id", insertable = false, updatable = false)
    private FormEntity form;

    @ManyToOne
    @JoinColumn(name = "question_id", insertable = false, updatable = false)
    private QuestionEntity question;

    @ManyToOne
    @JoinColumn(name = "answer_id")
    private AnswerEntity answer;

    public UserAnswerEntityPK getId() {
        return id;
    }

    public void setId(UserAnswerEntityPK id) {
        this.id = id;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public FormEntity getForm() {
        return form;
    }

    public void setForm(FormEntity form) {
        this.form = form;
    }

    public QuestionEntity getQuestion() {
        return question;
    }

    public void setQuestion(QuestionEntity question) {
        this.question = question;
    }

    public AnswerEntity getAnswer() {
        return answer;
    }

    public void setAnswer(AnswerEntity answer) {
        this.answer = answer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserAnswerEntity that = (UserAnswerEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
