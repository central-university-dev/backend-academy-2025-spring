package ru.tinkoff.education.backend.academy.dao.datajpa.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.tinkoff.education.backend.academy.model.entity.FormEntity;

@Repository
public interface FormRepo extends JpaRepository<FormEntity, Long> {
}
