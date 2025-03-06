package ru.tinkoff.education.backend.academy.model.mapper;


import ru.tinkoff.education.backend.academy.model.dto.Answer;
import ru.tinkoff.education.backend.academy.model.dto.Form;
import ru.tinkoff.education.backend.academy.model.dto.Question;
import ru.tinkoff.education.backend.academy.model.dto.User;
import ru.tinkoff.education.backend.academy.model.dto.UserAnswer;
import ru.tinkoff.education.backend.academy.model.entity.UserAnswerEntity;
import ru.tinkoff.education.backend.academy.model.entity.UserEntity;

public class UserAnswerMapper {

    public static UserAnswer map(UserAnswerEntity userAnswerEntity) {
        UserEntity userEntity = userAnswerEntity.getUser();
        User user = new User(userEntity.getId(), userEntity.getNickname());
        Form form = FormMapper.map(userAnswerEntity.getForm());
        Question question = QuestionMapper.map(userAnswerEntity.getQuestion());
        Answer answer = AnswerMapper.map(userAnswerEntity.getAnswer());
        return new UserAnswer(user, form, question, answer);
    }

}
