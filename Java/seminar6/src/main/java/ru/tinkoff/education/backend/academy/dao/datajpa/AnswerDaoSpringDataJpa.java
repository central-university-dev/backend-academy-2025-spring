package ru.tinkoff.education.backend.academy.dao.datajpa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.tinkoff.education.backend.academy.dao.AnswerDao;
import ru.tinkoff.education.backend.academy.dao.datajpa.repo.AnswerRepo;
import ru.tinkoff.education.backend.academy.model.dto.Answer;
import ru.tinkoff.education.backend.academy.model.entity.AnswerEntity;
import ru.tinkoff.education.backend.academy.model.entity.QuestionEntity;
import ru.tinkoff.education.backend.academy.model.mapper.AnswerMapper;

import java.util.List;
import java.util.Optional;

@Profile("spring-data-jpa")
@Repository
public class AnswerDaoSpringDataJpa implements AnswerDao {

    private final AnswerRepo answerRepo;

    @Autowired
    public AnswerDaoSpringDataJpa(AnswerRepo answerRepo) {
        this.answerRepo = answerRepo;
    }

    @Transactional
    @Override
    public Long create(Long questionId, String text) {
        AnswerEntity answerEntity = new AnswerEntity();
        QuestionEntity question = new QuestionEntity();
        question.setId(questionId);
        answerEntity.setQuestion(question);
        answerEntity.setText(text);
        return answerRepo.saveAndFlush(answerEntity).getId();
    }

    @Override
    public Optional<Answer> find(Long answerId) {
        return answerRepo.findById(answerId)
                .map(AnswerMapper::map);
    }

    @Transactional
    @Override
    public void remove(Long answerId) {
        answerRepo.deleteById(answerId);
    }

    @Override
    public List<Answer> findByQuestionId(Long questionId) {
        return answerRepo.findByQuestion_Id(questionId)
                .stream()
                .map(AnswerMapper::map)
                .toList();
    }

    @Transactional
    @Override
    public void removeByQuestionIds(List<Long> questionIds) {
        answerRepo.deleteAllByQuestionIds(questionIds);
    }

    @Transactional
    @Override
    public void removeByFormIds(List<Long> formIds) {
        answerRepo.deleteAllByFormIds(formIds);
    }
}
