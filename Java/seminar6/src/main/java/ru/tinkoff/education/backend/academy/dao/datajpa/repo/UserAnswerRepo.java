package ru.tinkoff.education.backend.academy.dao.datajpa.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.tinkoff.education.backend.academy.model.entity.UserAnswerEntity;
import ru.tinkoff.education.backend.academy.model.entity.UserAnswerEntityPK;

import java.util.List;

@Repository
public interface UserAnswerRepo extends JpaRepository<UserAnswerEntity, UserAnswerEntityPK> {
    List<UserAnswerEntity> findAllByUser_IdAndForm_Id(Long userId, Long formId);
}
