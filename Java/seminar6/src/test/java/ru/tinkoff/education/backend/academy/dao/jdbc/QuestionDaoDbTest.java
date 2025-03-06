package ru.tinkoff.education.backend.academy.dao.jdbc;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.tinkoff.education.backend.academy.dao.AnswerDao;
import ru.tinkoff.education.backend.academy.dao.QuestionDao;
import ru.tinkoff.education.backend.academy.dao.util.Utils;
import ru.tinkoff.education.backend.academy.model.dto.Answer;
import ru.tinkoff.education.backend.academy.model.dto.Question;

import java.util.List;

@ContextConfiguration(classes = QuestionDaoJdbc.class)
class QuestionDaoDbTest extends DbTest {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private QuestionDao questionDao;

    @MockitoBean
    private AnswerDao answerDao;

    @BeforeEach
    public void insertForm() {
        jdbcTemplate.update("insert into \"form\"(title) values ('Maestro survey')", EmptySqlParameterSource.INSTANCE);
    }

    @Test
    @DisplayName("Успешное создание вопроса")
    public void testCreate() {
        long generatedFormId = 1L, generatedQuestionId = 1L;
        String text = "Слушали ли вы 8 симфонию Чайковского?";
        List<Answer> answers = Utils.getAnswers("Да, ну, конечно!", "Нет", "Думаешь, я тебя не переиграю? Я тебя не уничтожу!?");
        Mockito.doReturn(answers)
                .when(answerDao)
                .findByQuestionId(generatedQuestionId);

        Long questionId = questionDao.create(generatedFormId, text);
        Question question = questionDao.find(questionId).get();

        Assertions.assertEquals(generatedQuestionId, question.id());
        Assertions.assertEquals(text, question.text());
        Assertions.assertEquals(answers, question.answers());
    }

    @Test
    @DisplayName("Должны возвращаться вопросы опроса")
    public void testFindByForm() {
        long formId = 1L;
        Question spongeQuestion = insertQuestion("Кто проживает на дне океана", "Песок", "Вода", "Бездна", "Губка");
        Question theWitcherQuestion = insertQuestion("Кого вы выбрали?", "Трисс", "Йенифэр", "Быть счастливым");
        Question bubbleGumQuestion = insertQuestion("Сколько стоит жвачка по рублю?", "10 рублей", "5 рублей");

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
        Mockito.verify(answerDao)
                .removeByQuestionIds(List.of(questionId));
    }

    private Question insertQuestion(String text, String... answers) {
        Long questionId = questionDao.create(1L, text);
        List<Answer> answerList = Utils.getAnswers(answers);
        Mockito.doReturn(answerList)
                .when(answerDao)
                .findByQuestionId(questionId);
        return new Question(questionId, text, answerList);
    }
}