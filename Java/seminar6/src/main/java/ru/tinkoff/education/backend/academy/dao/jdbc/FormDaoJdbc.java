package ru.tinkoff.education.backend.academy.dao.jdbc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.tinkoff.education.backend.academy.dao.FormDao;
import ru.tinkoff.education.backend.academy.dao.QuestionDao;
import ru.tinkoff.education.backend.academy.model.dto.Form;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Profile("jdbc")
@Repository
public class FormDaoJdbc implements FormDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final QuestionDao questionDao;

    @Autowired
    public FormDaoJdbc(NamedParameterJdbcTemplate jdbcTemplate, QuestionDao questionDao) {
        this.jdbcTemplate = jdbcTemplate;
        this.questionDao = questionDao;
    }

    @Transactional
    @Override
    public Long create(String title) {
        return jdbcTemplate.queryForObject("insert into \"form\"(title) values (:title) returning id",
                new MapSqlParameterSource("title", title),
                new SingleColumnRowMapper<>(Long.class));
    }

    @Transactional
    @Override
    public Optional<Form> find(Long formId) {
        return jdbcTemplate.query("select f.id, f.title from \"form\" f where f.id = :id",
                        new MapSqlParameterSource("id", formId),
                        (rs, rowNum) -> {
                            long id = rs.getLong("id");
                            String title = rs.getString("title");
                            return new Form(id, title, new ArrayList<>());
                        })
                .stream()
                .map(form -> form.with(questionDao.findByFormId(formId)))
                .findFirst();
    }

    @Transactional
    @Override
    public void remove(List<Long> formId) {
        if (formId.isEmpty()) {
            return;
        }
        questionDao.removeByFormId(formId);
        jdbcTemplate.update("delete from \"form\" where id in (:ids)",
                new MapSqlParameterSource("ids", formId));
    }
}
