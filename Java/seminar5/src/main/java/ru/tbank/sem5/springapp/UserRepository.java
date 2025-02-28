package ru.tbank.sem5.springapp;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
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
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    RowMapper<User> rowMapper = new RowMapper<User>() {
        @Override public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new User(
                rs.getString("name"),
                rs.getInt("balance")
            );
        }
    };

    public List<User> getUsers() {
        return jdbcTemplate.query("SELECT * FROM users",
            (rs, __) -> new User(rs.getString("name"), rs.getInt("balance")));
    }

    public List<User> getUsersWithRowMapper() {
        return jdbcTemplate.query("SELECT * FROM users", rowMapper);
    }

    public User getUserByIdNamed(Long id) {
        String sql = "SELECT * FROM users WHERE id = :id";
        Map<String, Object> params = Map.of("id", id);
        return namedJdbcTemplate.queryForObject(sql, params, rowMapper);
    }

    public void createUser(String name, int balance) {
        String sql = "INSERT INTO users (name, balance) VALUES (?, ?)";
        jdbcTemplate.update(sql, name, balance);
    }

    public void createUserNamed(String name, int balance) {
        String sql = "INSERT INTO users (name, balance) VALUES (:name, :balance)";
        namedJdbcTemplate.update(sql, Map.of("name", name, "balance", balance));
    }

    public void updateUserBalance(Long id, int newBalance) {
        String sql = "UPDATE users SET balance = ? WHERE id = ?";
        jdbcTemplate.update(sql, newBalance, id);
    }

    @Transactional
    public void transferMoney(Long fromUser, Long toUser, int amount) {
        jdbcTemplate.update("UPDATE users SET balance = balance - ? WHERE id = ?", amount, fromUser);
        jdbcTemplate.update("UPDATE users SET balance = balance + ? WHERE id = ?", amount, toUser);
        // Если произойдет ошибка, Spring сам сделает rollback
    }

    public void updateUserBalanceNamed(Long id, int newBalance) {
        String sql = "UPDATE users SET balance = :balance WHERE id = :id";
        Map<String, Object> params = Map.of("id", id, "balance", newBalance);
        namedJdbcTemplate.update(sql, params);
    }

    public void deleteUser(Long id) {
        String sql = "DELETE FROM users WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    public void deleteUserNamed(Long id) {
        String sql = "DELETE FROM users WHERE id = :id";
        Map<String, Object> params = Map.of("id", id);
        namedJdbcTemplate.update(sql, params);
    }

    /*
        Пример ResultSetExtractor

        ResultSetExtractor — это интерфейс Spring JDBC, который позволяет обработать ResultSet
         и вернуть кастомный объект или сложную структуру данных (например, Map, List и т. д.).

        Когда использовать ResultSetExtractor?
        Когда результат запроса не просто список записей (List<T>), а сложная структура (например, Map<Long, List<T>>).
        Когда нужно обработать весь ResultSet за один раз (а не построчно, как RowMapper).

        Запрос вернёт всех пользователей и сохранит их в Map, где name — ключ, а User — значение.
     */

    public Map<String, User> getUsersAsMap() {
        String sql = "SELECT * FROM users";

        return jdbcTemplate.query(sql, new ResultSetExtractor<Map<String, User>>() {
            @Override public Map<String, User> extractData(ResultSet rs) throws SQLException {
                Map<String, User> userMap = new HashMap<>();
                while (rs.next()) {
                    User user = new User(
                        rs.getString("name"),
                        rs.getInt("balance")
                    );

                    userMap.put(user.getName(), user);
                }
                return userMap;
            }
        });
    }

    public void batchInsertUsers(List<User> users) {
        String sql = "INSERT INTO users (name, balance) VALUES (?, ?)";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setString(1, users.get(i).getName());
                ps.setInt(2, users.get(i).getBalance());
            }

            @Override
            public int getBatchSize() {
                return users.size();
            }
        });
    }

    public void batchInsertUsersNamed(List<User> users) {
        String sql = "INSERT INTO users (name, balance) VALUES (:name, :balance)";

        List<Map<String, Object>> batchValues = users.stream()
            .map(user -> Map.<String, Object>of(
                "name", user.getName(),
                "balance", user.getBalance()
            ))
            .toList();

        namedJdbcTemplate.batchUpdate(sql, batchValues.toArray(new Map[0]));
    }


}
