package ru.tinkoff.education.backend.academy.dao.datajpa;

import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.tinkoff.education.backend.academy.dao.UserAnswerDao;
import ru.tinkoff.education.backend.academy.dao.datajpa.repo.UserAnswerRepo;
import ru.tinkoff.education.backend.academy.model.dto.Answer;
import ru.tinkoff.education.backend.academy.model.entity.AnswerEntity;
import ru.tinkoff.education.backend.academy.model.entity.FormEntity;
import ru.tinkoff.education.backend.academy.model.entity.QuestionEntity;
import ru.tinkoff.education.backend.academy.model.entity.UserAnswerEntity;
import ru.tinkoff.education.backend.academy.model.entity.UserAnswerEntityPK;
import ru.tinkoff.education.backend.academy.model.entity.UserEntity;
import ru.tinkoff.education.backend.academy.model.mapper.AnswerMapper;

import java.util.List;

@Profile("spring-data-jpa")
@Repository
public class UserAnswerDaoSpringDataJpa implements UserAnswerDao {

    private final UserAnswerRepo userAnswerRepo;
    private final EntityManager entityManager;

    @Autowired
    public UserAnswerDaoSpringDataJpa(UserAnswerRepo userAnswerRepo, EntityManager entityManager) {
        this.userAnswerRepo = userAnswerRepo;
        this.entityManager = entityManager;
    }

    @Override
    public List<Answer> getUserAnswers(Long userId, Long formId) {
        return userAnswerRepo.findAllByUser_IdAndForm_Id(userId, formId)
                .stream()
                .map(userAnswerEntity -> AnswerMapper.map(userAnswerEntity.getAnswer()))
                .toList();
    }

    @Transactional
    @Override
    public void answer(Long userId, Long formId, Long questionId, Long answerId) {
        UserAnswerEntityPK id = getId(userId, formId, questionId);
        UserAnswerEntity userAnswerEntity = userAnswerRepo.findById(id)
                .orElseGet(() -> {
                    UserAnswerEntity userAnswer = new UserAnswerEntity();
                    userAnswer.setId(id);
                    UserEntity userEntity = entityManager.getReference(UserEntity.class, userId);//Почему бы не использовать new UserEntity().setId(userId)?
                    FormEntity formEntity = entityManager.getReference(FormEntity.class, formId);
                    QuestionEntity questionEntity = entityManager.getReference(QuestionEntity.class, questionId);
                    userAnswer.setUser(userEntity);
                    userAnswer.setForm(formEntity);
                    userAnswer.setQuestion(questionEntity);
                    return userAnswer;
                });
        AnswerEntity answerEntity = entityManager.getReference(AnswerEntity.class, answerId);
        userAnswerEntity.setAnswer(answerEntity);
        userAnswerRepo.saveAndFlush(userAnswerEntity);
    }

    @Transactional
    @Override
    public void remove(Long userId, Long formId, Long questionId) {
        UserAnswerEntityPK id = getId(userId, formId, questionId);
        userAnswerRepo.deleteById(id);
    }

    private static UserAnswerEntityPK getId(Long userId, Long formId, Long questionId) {
        UserAnswerEntityPK id = new UserAnswerEntityPK();
        id.setUserId(userId);
        id.setFormId(formId);
        id.setQuestionId(questionId);
        return id;
    }
}
