package ru.tinkoff.education.backend.academy.dao.jpa;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import ru.tinkoff.education.backend.academy.dao.QuestionDao;
import ru.tinkoff.education.backend.academy.model.dto.Question;

import java.util.Collections;
import java.util.List;

@ContextConfiguration(classes = { QuestionDaoJpa.class })
class QuestionDaoDbTest extends DbTest {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private QuestionDao questionDao;

    @BeforeEach
    public void insertForm() {
        jdbcTemplate.update("insert into \"form\"(title) values ('Maestro survey')", EmptySqlParameterSource.INSTANCE);
    }

    @Test
    @DisplayName("Успешное создание вопроса")
    public void testCreate() {
        long generatedFormId = 1L, generatedQuestionId = 1L;
        String text = "Слушали ли вы 8 симфонию Чайковского?";

        Long questionId = questionDao.create(generatedFormId, text);
        Question question = questionDao.find(questionId).get();

        Assertions.assertEquals(generatedQuestionId, question.id());
        Assertions.assertEquals(text, question.text());
    }

    @Test
    @DisplayName("Должны возвращаться вопросы опроса")
    public void testFindByForm() {
        long formId = 1L;
        Question spongeQuestion = insertQuestion("Кто проживает на дне океана");
        Question theWitcherQuestion = insertQuestion("Кого вы выбрали?");
        Question bubbleGumQuestion = insertQuestion("Сколько стоит жвачка по рублю?");

        List<Question> questions = questionDao.findByFormId(formId);

        Assertions.assertEquals(3, questions.size());
        Assertions.assertEquals(spongeQuestion, questions.get(0));
        Assertions.assertEquals(theWitcherQuestion, questions.get(1));
        Assertions.assertEquals(bubbleGumQuestion, questions.get(2));
    }

    @Test
    @DisplayName("Успешное удаление вопроса")
    public void testRemove() {
        long formId = 1L;

        Long questionId = questionDao.create(formId, "GTA IV vs GTA V?");
        questionDao.remove(List.of(questionId));

        Assertions.assertTrue(questionDao.find(questionId).isEmpty());
    }

    private Question insertQuestion(String text) {
        Long questionId = questionDao.create(1L, text);
        return new Question(questionId, text, Collections.emptyList());
    }
}