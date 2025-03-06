package ru.tinkoff.education.backend.academy.dao.datajpa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.tinkoff.education.backend.academy.dao.UserDao;
import ru.tinkoff.education.backend.academy.dao.datajpa.repo.UserRepo;
import ru.tinkoff.education.backend.academy.model.dto.User;
import ru.tinkoff.education.backend.academy.model.entity.UserEntity;

import java.util.Optional;

@Profile("spring-data-jpa")
@Repository
public class UserDaoSpringDataJpa implements UserDao {

    private final UserRepo userRepo;

    @Autowired
    public UserDaoSpringDataJpa(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @Transactional
    @Override
    public Long create(String nickname) {
        UserEntity userEntity = new UserEntity();
        userEntity.setNickname(nickname);
        return userRepo.saveAndFlush(userEntity).getId();
    }

    @Override
    public Optional<User> find(Long userId) {
        return userRepo.findById(userId)
                .map(userEntity -> new User(userEntity.getId(), userEntity.getNickname()));
    }
}
