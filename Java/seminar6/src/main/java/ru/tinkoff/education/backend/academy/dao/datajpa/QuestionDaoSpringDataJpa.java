package ru.tinkoff.education.backend.academy.dao.datajpa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.tinkoff.education.backend.academy.dao.QuestionDao;
import ru.tinkoff.education.backend.academy.dao.datajpa.repo.QuestionRepo;
import ru.tinkoff.education.backend.academy.model.dto.Question;
import ru.tinkoff.education.backend.academy.model.entity.FormEntity;
import ru.tinkoff.education.backend.academy.model.entity.QuestionEntity;
import ru.tinkoff.education.backend.academy.model.mapper.QuestionMapper;

import java.util.List;
import java.util.Optional;


@Profile("spring-data-jpa")
@Repository
public class QuestionDaoSpringDataJpa implements QuestionDao {

    private final QuestionRepo questionRepo;

    @Autowired
    public QuestionDaoSpringDataJpa(QuestionRepo questionRepo) {
        this.questionRepo = questionRepo;
    }

    @Transactional
    @Override
    public Long create(Long formId, String text) {
        QuestionEntity questionEntity = new QuestionEntity();
        FormEntity formEntity = new FormEntity();
        formEntity.setId(formId);
        questionEntity.setForm(formEntity);
        questionEntity.setText(text);
        return questionRepo.saveAndFlush(questionEntity ).getId();
    }

    @Override
    public Optional<Question> find(Long questionId) {
        return questionRepo.findById(questionId)
                .map(QuestionMapper::map);
    }

    @Override
    public List<Question> findByFormId(Long formId) {
        return questionRepo.findAllByForm_Id(formId)
                .stream()
                .map(QuestionMapper::map)
                .toList();
    }

    @Transactional
    @Override
    public void remove(List<Long> questionIds) {
        questionRepo.deleteAllById(questionIds);
    }

    @Transactional
    @Override
    public void removeByFormId(List<Long> formIds) {
        questionRepo.deleteAllByFormIds(formIds);
    }
}
