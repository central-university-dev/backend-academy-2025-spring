package ru.tinkoff.education.backend.academy.dao.jpa;

import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.tinkoff.education.backend.academy.dao.AnswerDao;
import ru.tinkoff.education.backend.academy.model.dto.Answer;
import ru.tinkoff.education.backend.academy.model.entity.AnswerEntity;
import ru.tinkoff.education.backend.academy.model.entity.QuestionEntity;
import ru.tinkoff.education.backend.academy.model.mapper.AnswerMapper;

import java.util.List;
import java.util.Optional;

@Profile("jpa")
@Repository
public class AnswerDaoJpa implements AnswerDao {

    private final EntityManager entityManager;

    @Autowired
    public AnswerDaoJpa(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional
    @Override
    public Long create(Long questionId, String text) {
        AnswerEntity answerEntity = new AnswerEntity();
        QuestionEntity questionEntity = new QuestionEntity();

        questionEntity.setId(questionId);
        answerEntity.setText(text);
        answerEntity.setQuestion(questionEntity);
        entityManager.persist(answerEntity);
        return answerEntity.getId();
    }

    @Override
    public Optional<Answer> find(Long answerId) {
        return Optional.ofNullable(entityManager.find(AnswerEntity.class, answerId))
                .map(AnswerMapper::map);
    }

    @Transactional
    @Override
    public void remove(Long answerId) {
        AnswerEntity answerEntity = entityManager.getReference(AnswerEntity.class, answerId);
        entityManager.remove(answerEntity);
    }

    @Override
    public List<Answer> findByQuestionId(Long questionId) {
        return entityManager.createQuery("select a from AnswerEntity a where a.question.id = :questionId", AnswerEntity.class)
                .setParameter("questionId", questionId)
                .getResultStream()
                .map(AnswerMapper::map)
                .toList();
    }

    @Transactional
    @Override
    public void removeByQuestionIds(List<Long> questionIds) {
        entityManager.createQuery("delete from AnswerEntity where question.id in :questionIds")
                .setParameter("questionIds", questionIds)
                .executeUpdate();
    }

    @Transactional
    @Override
    public void removeByFormIds(List<Long> formIds) {
        entityManager.createQuery("delete from AnswerEntity where question.form.id in :formIds")
                .setParameter("formIds", formIds)
                .executeUpdate();
    }
}
