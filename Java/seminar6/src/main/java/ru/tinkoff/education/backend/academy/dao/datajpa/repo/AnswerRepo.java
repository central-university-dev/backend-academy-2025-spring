package ru.tinkoff.education.backend.academy.dao.datajpa.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.tinkoff.education.backend.academy.model.entity.AnswerEntity;

import java.util.List;

@Repository
public interface AnswerRepo extends JpaRepository<AnswerEntity, Long> {
    List<AnswerEntity> findByQuestion_Id(Long questionId);

    @Transactional
    @Modifying
    @Query("delete from AnswerEntity where question.id in :questionIds")
    void deleteAllByQuestionIds(@Param("questionIds") List<Long> questionIds);


    @Transactional
    @Modifying
    @Query("delete from AnswerEntity where question.form.id in :formIds")
    void deleteAllByFormIds(@Param("formIds") List<Long> formIds);
}
