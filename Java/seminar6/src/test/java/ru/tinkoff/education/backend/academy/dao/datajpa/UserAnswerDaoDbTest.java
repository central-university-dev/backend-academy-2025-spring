package ru.tinkoff.education.backend.academy.dao.datajpa;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import ru.tinkoff.education.backend.academy.dao.UserAnswerDao;
import ru.tinkoff.education.backend.academy.model.dto.Answer;

import java.util.List;

@ContextConfiguration(classes = UserAnswerDaoSpringDataJpa.class)
class UserAnswerDaoDbTest extends DbTest {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private UserAnswerDao userAnswerDao;

    @BeforeEach
    public void createUserFormQuestion() {
        jdbcTemplate.update("insert into \"user\"(nickname) values ('Melon Tusk')", EmptySqlParameterSource.INSTANCE);
        jdbcTemplate.update("insert into form(title) values ('Coin universe')", EmptySqlParameterSource.INSTANCE);
        jdbcTemplate.update("insert into question(form_id, \"text\") values (1, 'Is DOGE coin stonks?')", EmptySqlParameterSource.INSTANCE);
        jdbcTemplate.update("insert into answer(question_id, \"text\") values (1, 'Yes'), (1, 'PEPE is supreme overall'), (1, 'Цифровой рубль здесь отец')", EmptySqlParameterSource.INSTANCE);
    }

    @Test
    @DisplayName("Сохранение первого ответа пользователя (ранее на вопрос не было ответа)")
    public void testAnswer() {
        userAnswerDao.answer(1L, 1L, 1L, 1L);

        List<Answer> userAnswers = userAnswerDao.getUserAnswers(1L, 1L);

        Assertions.assertEquals(1, userAnswers.size());
        Assertions.assertEquals("Yes", userAnswers.get(0).text());
    }

    @Test
    @DisplayName("Сохранение нового ответа пользователя (ранее на вопрос был ответ)")
    public void testNewAnswer() {
        userAnswerDao.answer(1L, 1L, 1L, 1L);
        userAnswerDao.answer(1L, 1L, 1L, 3L);

        List<Answer> userAnswers = userAnswerDao.getUserAnswers(1L, 1L);

        Assertions.assertEquals(1, userAnswers.size());
        Assertions.assertEquals("Цифровой рубль здесь отец", userAnswers.get(0).text());
    }

    @Test
    @DisplayName("Очиста ответа пользователя по вопросу")
    public void testRemove() {
        userAnswerDao.answer(1L, 1L, 1L, 1L);

        userAnswerDao.remove(1L, 1L, 1L);
        List<Answer> userAnswers = userAnswerDao.getUserAnswers(1L, 1L);

        Assertions.assertTrue(userAnswers.isEmpty());
    }
}