package ru.tinkoff.education.backend.academy.dao.jpa;

import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.tinkoff.education.backend.academy.dao.UserAnswerDao;
import ru.tinkoff.education.backend.academy.model.dto.Answer;
import ru.tinkoff.education.backend.academy.model.entity.AnswerEntity;
import ru.tinkoff.education.backend.academy.model.entity.UserAnswerEntity;
import ru.tinkoff.education.backend.academy.model.entity.UserAnswerEntityPK;
import ru.tinkoff.education.backend.academy.model.mapper.AnswerMapper;

import java.util.List;

@Profile("jpa")
@Repository
public class UserAnswerDaoJpa implements UserAnswerDao {

    private final EntityManager entityManager;

    @Autowired
    public UserAnswerDaoJpa(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<Answer> getUserAnswers(Long userId, Long formId) {
        return entityManager.createQuery("""
                        select a from UserAnswerEntity uae join uae.answer a where uae.user.id = :userId and uae.form.id = :formId
                        """, AnswerEntity.class)
                .setParameter("userId", userId)
                .setParameter("formId", formId)
                .getResultStream()
                .map(AnswerMapper::map)
                .toList();
    }

    @Transactional
    @Override
    public void answer(Long userId, Long formId, Long questionId, Long answerId) {
        UserAnswerEntity userAnswerEntity = createUserAnswerEntity(userId, formId, questionId, answerId);

        entityManager.merge(userAnswerEntity);
    }

    @Transactional
    @Override
    public void remove(Long userId, Long formId, Long questionId) {
        UserAnswerEntity userAnswerEntity = createUserAnswerEntity(userId, formId, questionId, null);
        UserAnswerEntityPK id = userAnswerEntity.getId();
        UserAnswerEntity reference = entityManager.getReference(UserAnswerEntity.class, id);
        entityManager.remove(reference);
    }

    private static UserAnswerEntity createUserAnswerEntity(Long userId, Long formId, Long questionId, Long answerId) {
        UserAnswerEntity userAnswerEntity = new UserAnswerEntity();

        UserAnswerEntityPK id = new UserAnswerEntityPK();
        id.setUserId(userId);
        id.setFormId(formId);
        id.setQuestionId(questionId);

        AnswerEntity answerEntity = new AnswerEntity();
        answerEntity.setId(answerId);

        userAnswerEntity.setId(id);
        userAnswerEntity.setAnswer(answerEntity);
        return userAnswerEntity;
    }
}
