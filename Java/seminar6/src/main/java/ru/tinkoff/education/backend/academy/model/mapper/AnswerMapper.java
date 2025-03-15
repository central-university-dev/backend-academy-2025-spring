package ru.tinkoff.education.backend.academy.model.mapper;

import ru.tinkoff.education.backend.academy.model.dto.Answer;
import ru.tinkoff.education.backend.academy.model.entity.AnswerEntity;

import java.util.List;

public class AnswerMapper {

    public static Answer map(AnswerEntity answerEntity) {
        return new Answer(answerEntity.getId(), answerEntity.getText());
    }

    public static List<Answer> map(List<AnswerEntity> answers) {
        return answers.stream()
                .map(AnswerMapper::map)
                .toList();
    }
}
