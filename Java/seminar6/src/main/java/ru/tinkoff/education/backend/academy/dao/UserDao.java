package ru.tinkoff.education.backend.academy.dao;

import ru.tinkoff.education.backend.academy.model.dto.User;

import java.util.Optional;

public interface UserDao {
    Long create(String nickname);
    Optional<User> find(Long userId);
}
