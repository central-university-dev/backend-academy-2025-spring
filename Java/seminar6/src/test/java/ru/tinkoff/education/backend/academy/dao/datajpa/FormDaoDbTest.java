package ru.tinkoff.education.backend.academy.dao.datajpa;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.tinkoff.education.backend.academy.dao.FormDao;
import ru.tinkoff.education.backend.academy.dao.QuestionDao;
import ru.tinkoff.education.backend.academy.model.dto.Form;

import java.util.List;

@ContextConfiguration(classes = { FormDaoSpringDataJpa.class })
class FormDaoDbTest extends DbTest {

    @Autowired
    private FormDao formDao;

    @MockitoBean
    private QuestionDao questionDao;

    @Test
    @DisplayName("Успешное создание опроса")
    public void testCreate() {
        long generatedFormId = 1L;
        String title = "Социогастрономический опрос";

        Long formId = formDao.create(title);
        Form form = formDao.find(formId).get();

        Assertions.assertEquals(generatedFormId, formId);
        Assertions.assertEquals(title, form.title());
        Assertions.assertTrue(formDao.find(formId).isPresent());
    }

    @Test
    @DisplayName("Успешное удаление опроса")
    public void testRemove() {
        long generatedFormId = 1L;
        String title = "Опрос самых успешных людей";
        Long formId = formDao.create(title);
        Assertions.assertEquals(generatedFormId, formId);

        formDao.remove(List.of(formId));
        Assertions.assertTrue(formDao.find(formId).isEmpty());
    }
}