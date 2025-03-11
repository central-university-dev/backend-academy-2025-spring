package ru.tinkoff.education.backend.academy.model.entity;

import jakarta.persistence.*;

import java.util.Objects;

@Table(name = "answer")
@Entity
public class AnswerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "answer_id_gen")
    @SequenceGenerator(name = "answer_id_gen", sequenceName = "answer_id_seq", allocationSize = 1)
    private Long id;

    @JoinColumn(name = "question_id", referencedColumnName = "id")
    @ManyToOne
    private QuestionEntity question;

    @Column(name = "text", nullable = false)
    private String text;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public QuestionEntity getQuestion() {
        return question;
    }

    public void setQuestion(QuestionEntity question) {
        this.question = question;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnswerEntity that = (AnswerEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(text, that.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, text);
    }
}
