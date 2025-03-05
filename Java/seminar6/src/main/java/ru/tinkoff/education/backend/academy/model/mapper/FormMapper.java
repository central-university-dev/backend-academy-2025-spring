package ru.tinkoff.education.backend.academy.model.mapper;

import ru.tinkoff.education.backend.academy.model.dto.Form;
import ru.tinkoff.education.backend.academy.model.dto.Question;
import ru.tinkoff.education.backend.academy.model.entity.FormEntity;

import java.util.List;

public class FormMapper {
    public static Form map(FormEntity formEntity) {
        List<Question> questions = QuestionMapper.map(formEntity.getQuestions());
        return new Form(formEntity.getId(), formEntity.getTitle(), questions);
    }
}
