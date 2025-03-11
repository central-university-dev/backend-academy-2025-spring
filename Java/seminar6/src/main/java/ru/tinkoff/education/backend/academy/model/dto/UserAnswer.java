package ru.tinkoff.education.backend.academy.model.dto;

public record UserAnswer(User user, Form form, Question question, Answer answer) {
}
