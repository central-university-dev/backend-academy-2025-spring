package ru.tinkoff.education.backend.academy.model.dto;

import java.util.Collections;
import java.util.List;

public record Question(Long id, String text, List<Answer> answers) {
    public Question with(List<Answer> answers) {
        return new Question(id, text, Collections.unmodifiableList(answers));
    }
}
