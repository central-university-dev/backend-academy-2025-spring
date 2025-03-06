package ru.tinkoff.education.backend.academy.dao.jdbc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.tinkoff.education.backend.academy.dao.AnswerDao;
import ru.tinkoff.education.backend.academy.model.dto.Answer;

import java.util.List;
import java.util.Optional;

@Profile("jdbc")
@Repository
public class AnswerDaoJdbc implements AnswerDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public AnswerDaoJdbc(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    @Override
    public Long create(Long questionId, String text) {
        return jdbcTemplate.queryForObject("insert into answer(question_id, \"text\") values(:questionId, :text) returning id",
                new MapSqlParameterSource("text", text).addValue("questionId", questionId),
                SingleColumnRowMapper.newInstance(Long.class));
    }

    @Override
    public Optional<Answer> find(Long answerId) {
        return jdbcTemplate.query("select a.id, a.\"text\" from answer a where a.id = :id",
                new MapSqlParameterSource("id", answerId),
                DataClassRowMapper.newInstance(Answer.class))
                .stream()
                .findFirst();
    }

    @Transactional
    @Override
    public void remove(Long answerId) {
        jdbcTemplate.update("delete from answer where id = :id",
                new MapSqlParameterSource("id", answerId));
    }

    @Override
    public List<Answer> findByQuestionId(Long questionId) {
        return jdbcTemplate.query("select a.id, a.\"text\" from answer a where a.question_id = :questionId",
                new MapSqlParameterSource("questionId", questionId),
                DataClassRowMapper.newInstance(Answer.class));
    }

    @Transactional
    @Override
    public void removeByQuestionIds(List<Long> questionIds) {
        jdbcTemplate.update("delete from answer where question_id in (:questionIds)",
                new MapSqlParameterSource("questionIds", questionIds));
    }

    @Transactional
    @Override
    public void removeByFormIds(List<Long> formIds) {
        jdbcTemplate.update("""
                with cte as (
                    select q.id from question q
                    where q.form_id in (:formIds)
                )
                delete from answer where question_id in (select id from cte)
                """, new MapSqlParameterSource("formIds", formIds));
    }
}
