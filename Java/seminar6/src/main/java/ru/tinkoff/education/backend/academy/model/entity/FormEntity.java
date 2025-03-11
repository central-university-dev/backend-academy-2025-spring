package ru.tinkoff.education.backend.academy.model.entity;

import jakarta.persistence.*;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Table(name = "form")
@Entity
public class FormEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "form_id_gen")
    @SequenceGenerator(name = "form_id_gen", sequenceName = "form_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @OneToMany(mappedBy = "form", cascade = CascadeType.ALL)
    private List<QuestionEntity> questions;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<QuestionEntity> getQuestions() {
        return questions == null ? Collections.emptyList() : questions;
    }

    public void setQuestions(List<QuestionEntity> questions) {
        this.questions = questions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FormEntity that = (FormEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(title, that.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title);
    }
}
