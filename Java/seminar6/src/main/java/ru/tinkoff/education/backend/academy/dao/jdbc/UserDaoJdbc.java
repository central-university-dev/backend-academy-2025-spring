package ru.tinkoff.education.backend.academy.dao.jdbc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.tinkoff.education.backend.academy.dao.UserDao;
import ru.tinkoff.education.backend.academy.model.dto.User;

import java.util.Optional;

@Profile("jdbc")
@Repository
public class UserDaoJdbc implements UserDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public UserDaoJdbc(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    @Override
    public Long create(String nickname) {
        return jdbcTemplate.queryForObject("insert into \"user\"(nickname) values (:nickname) returning id",
                new MapSqlParameterSource("nickname", nickname),
                SingleColumnRowMapper.newInstance(Long.class));
    }

    @Override
    public Optional<User> find(Long userId) {
        return jdbcTemplate.query("select u.id, u.nickname from \"user\" u where u.id = :id",
                        new MapSqlParameterSource("id", userId),
                        DataClassRowMapper.newInstance(User.class))
                .stream()
                .findFirst();
    }
}
