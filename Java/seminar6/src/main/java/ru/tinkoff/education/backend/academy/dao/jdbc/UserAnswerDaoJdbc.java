package ru.tinkoff.education.backend.academy.dao.jdbc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.tinkoff.education.backend.academy.dao.UserAnswerDao;
import ru.tinkoff.education.backend.academy.model.dto.Answer;

import java.util.List;

@Profile("jdbc")
@Repository
public class UserAnswerDaoJdbc implements UserAnswerDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public UserAnswerDaoJdbc(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Answer> getUserAnswers(Long userId, Long formId) {
        return jdbcTemplate.query("""
                                select a.id, a."text" from user_answer ua
                                join answer a on a.id = ua.answer_id
                                where ua.user_id = :userId and ua.form_id = :formId""",
                new MapSqlParameterSource("userId", userId)
                        .addValue("formId", formId), DataClassRowMapper.newInstance(Answer.class));
    }

    @Transactional
    @Override
    public void answer(Long userId, Long formId, Long questionId, Long answerId) {
        jdbcTemplate.update("""
                insert into user_answer(user_id, form_id, question_id, answer_id) values (:userId, :formId, :questionId, :answerId)
                on conflict (user_id, form_id, question_id) do update set answer_id = EXCLUDED.answer_id
                """,
                new MapSqlParameterSource("userId", userId)
                        .addValue("formId", formId)
                        .addValue("questionId", questionId)
                        .addValue("answerId", answerId));
    }

    @Transactional
    @Override
    public void remove(Long userId, Long formId, Long questionId) {
        jdbcTemplate.update("delete from user_answer where user_id = :userId and form_id = :formId and question_id = :questionId",
                new MapSqlParameterSource("userId", userId)
                        .addValue("formId", formId)
                        .addValue("questionId", questionId));
    }
}
