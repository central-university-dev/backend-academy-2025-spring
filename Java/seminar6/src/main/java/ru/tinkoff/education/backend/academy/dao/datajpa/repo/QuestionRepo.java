package ru.tinkoff.education.backend.academy.dao.datajpa.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.tinkoff.education.backend.academy.model.entity.QuestionEntity;

import java.util.List;

@Repository
public interface QuestionRepo extends JpaRepository<QuestionEntity, Long> {
    List<QuestionEntity> findAllByForm_Id(Long formId);

    @Transactional
    @Modifying
    @Query("delete from QuestionEntity where form.id in :formIds")
    void deleteAllByFormIds(@Param("formIds") List<Long> formIds);
}
