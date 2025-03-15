package ru.tinkoff.education.backend.academy.dao;

import ru.tinkoff.education.backend.academy.model.dto.Answer;

import java.util.List;

public interface UserAnswerDao {
    List<Answer> getUserAnswers(Long userId, Long formId);
    void answer(Long userId, Long formId, Long questionId, Long answerId);
    void remove(Long userId, Long formId, Long questionId);
}
