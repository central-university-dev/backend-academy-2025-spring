package ru.tinkoff.education.backend.academy.dao.datajpa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.tinkoff.education.backend.academy.dao.FormDao;
import ru.tinkoff.education.backend.academy.dao.datajpa.repo.FormRepo;
import ru.tinkoff.education.backend.academy.model.dto.Form;
import ru.tinkoff.education.backend.academy.model.entity.FormEntity;
import ru.tinkoff.education.backend.academy.model.mapper.FormMapper;

import java.util.List;
import java.util.Optional;

@Profile("spring-data-jpa")
@Repository
public class FormDaoSpringDataJpa implements FormDao {

    private final FormRepo formRepo;

    @Autowired
    public FormDaoSpringDataJpa(FormRepo formRepo) {
        this.formRepo = formRepo;
    }

    @Transactional
    @Override
    public Long create(String title) {
        FormEntity formEntity = new FormEntity();
        formEntity.setTitle(title);
        return formRepo.saveAndFlush(formEntity).getId();
    }

    @Override
    public Optional<Form> find(Long formId) {
        return formRepo.findById(formId)
                .map(FormMapper::map);
    }

    @Transactional
    @Override
    public void remove(List<Long> formId) {
        formRepo.deleteAllById(formId);
    }
}
