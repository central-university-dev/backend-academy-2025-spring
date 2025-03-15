package ru.tinkoff.education.backend.academy.dao.jdbc;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import ru.tinkoff.education.backend.academy.dao.AnswerDao;
import ru.tinkoff.education.backend.academy.model.dto.Answer;

@ContextConfiguration(classes = AnswerDaoJdbc.class)
class AnswerDaoDbTest extends DbTest {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private AnswerDao answerDao;

    @BeforeEach
    public void insertFormAndQuestion() {
        jdbcTemplate.update("""
                    with cte as (insert into form(title) values ('Survey') returning id)
                    insert into question(form_id, "text")
                    select c.id, 'Are you?' from cte c
            """, EmptySqlParameterSource.INSTANCE);
    }

    @Test
    @DisplayName("Успешное создание ответа")
    public void testCreate() {
        String text = "I am!";

        Long answerId = answerDao.create(1L, text);
        Answer answer = answerDao.find(answerId).get();

        Assertions.assertEquals(1L, answer.id());
        Assertions.assertEquals(text, answer.text());
    }

    @Test
    @DisplayName("Успешное удаление ответа")
    public void testRemove() {

        Long answerId = answerDao.create(1L, "Вероятность крайне мала");
        answerDao.remove(answerId);

        Assertions.assertTrue(answerDao.find(answerId).isEmpty());
    }
}