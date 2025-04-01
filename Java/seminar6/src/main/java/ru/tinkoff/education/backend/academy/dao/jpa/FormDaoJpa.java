package ru.tinkoff.education.backend.academy.dao.jpa;

import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.tinkoff.education.backend.academy.dao.FormDao;
import ru.tinkoff.education.backend.academy.model.dto.Form;
import ru.tinkoff.education.backend.academy.model.entity.FormEntity;
import ru.tinkoff.education.backend.academy.model.mapper.FormMapper;

import java.util.List;
import java.util.Optional;

@Profile("jpa")
@Repository
public class FormDaoJpa implements FormDao {

    private final EntityManager entityManager;

    @Autowired
    public FormDaoJpa(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional
    @Override
    public Long create(String title) {
        FormEntity formEntity = new FormEntity();
        formEntity.setTitle(title);
        entityManager.persist(formEntity);
        return formEntity.getId();
    }

    @Override
    public Optional<Form> find(Long formId) {
        return Optional.ofNullable(entityManager.find(FormEntity.class, formId))
                .map(FormMapper::map);
    }

    @Transactional
    @Override
    public void remove(List<Long> formIds) {
        formIds.stream()
                .map(formId -> entityManager.getReference(FormEntity.class, formId))
                .forEach(entityManager::remove);
    }
}
