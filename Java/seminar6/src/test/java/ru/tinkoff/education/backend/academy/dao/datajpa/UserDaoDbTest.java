package ru.tinkoff.education.backend.academy.dao.datajpa;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import ru.tinkoff.education.backend.academy.dao.UserDao;

@ContextConfiguration(classes = {UserDaoSpringDataJpa.class})
class UserDaoDbTest extends DbTest {

    @Autowired
    private UserDao userDao;

    @Test
    @DisplayName("Успешное создание пользователя")
    public void testCreate() {
        Long userId = userDao.create("baron");

        Assertions.assertEquals(1L, userId);
        Assertions.assertTrue(userDao.find(userId).isPresent());
    }
}