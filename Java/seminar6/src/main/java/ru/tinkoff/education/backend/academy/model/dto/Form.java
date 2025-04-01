package ru.tinkoff.education.backend.academy.model.dto;

import java.util.Collections;
import java.util.List;

public record Form(Long id, String title, List<Question> questions) {
    public Form with(List<Question> questions) {
        return new Form(id, title, Collections.unmodifiableList(questions));
    }
}
