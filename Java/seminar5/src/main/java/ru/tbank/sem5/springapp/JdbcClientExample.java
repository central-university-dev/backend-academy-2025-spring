package ru.tbank.sem5.springapp;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class JdbcClientExample {

    private final JdbcClient jdbcClient;

    public List<User> getAllUsers() {
        return jdbcClient.sql("SELECT * FROM users")
            .query(User.class) // Автоматическое маппинг в POJO (если настроен RowMapper)
            .list();
    }

    public List<User> getAllUsersRowMapper() {
        return jdbcClient.sql("SELECT * FROM users")
            .query((rs, rowNum) -> new User(
                rs.getString("name"),
                rs.getInt("balance")
            ))
            .list();
    }

    public void createUser(String name, int balance) {
        jdbcClient.sql("INSERT INTO users (name, balance) VALUES (:name, :balance)")
            .param("name", name)
            .param("balance", balance)
            .update();
    }

    public void updateBalance(Long userId, int newBalance) {
        jdbcClient.sql("UPDATE users SET balance = :balance WHERE id = :id")
            .param("balance", newBalance)
            .param("id", userId)
            .update();
    }

    public void deleteUser(Long userId) {
        jdbcClient.sql("DELETE FROM users WHERE id = :id")
            .param("id", userId)
            .update();
    }

}
