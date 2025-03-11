package ru.tinkoff.education.backend.academy.dao.jpa;

import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.tinkoff.education.backend.academy.dao.QuestionDao;
import ru.tinkoff.education.backend.academy.model.dto.Question;
import ru.tinkoff.education.backend.academy.model.entity.FormEntity;
import ru.tinkoff.education.backend.academy.model.entity.QuestionEntity;
import ru.tinkoff.education.backend.academy.model.mapper.QuestionMapper;

import java.util.List;
import java.util.Optional;

@Profile("jpa")
@Repository
public class QuestionDaoJpa implements QuestionDao {

    private final EntityManager entityManager;

    @Autowired
    public QuestionDaoJpa(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional
    @Override
    public Long create(Long formId, String text) {
        QuestionEntity questionEntity = new QuestionEntity();
        FormEntity formEntity = new FormEntity();
        formEntity.setId(formId);

        questionEntity.setForm(formEntity);
        questionEntity.setText(text);
        entityManager.persist(questionEntity);
        return questionEntity.getId();
    }

    @Override
    public Optional<Question> find(Long questionId) {
        return Optional.ofNullable(entityManager.find(QuestionEntity.class, questionId))
                .map(QuestionMapper::map);
    }

    @Override
    public List<Question> findByFormId(Long formId) {
        return entityManager.createQuery("select q from QuestionEntity q where q.form.id = :formId", QuestionEntity.class)
                .setParameter("formId", formId)
                .getResultStream()
                .map(QuestionMapper::map)
                .toList();
    }

    @Transactional
    @Override
    public void remove(List<Long> questionIds) {
        questionIds.stream()
                .map(questionId -> entityManager.getReference(QuestionEntity.class, questionId))
                .forEach(entityManager::remove);
    }

    @Transactional
    @Override
    public void removeByFormId(List<Long> formIds) {
        entityManager.createQuery("delete from QuestionEntity where from.id in :formIds")
                .setParameter("formIds", formIds)
                .executeUpdate();
    }
}
