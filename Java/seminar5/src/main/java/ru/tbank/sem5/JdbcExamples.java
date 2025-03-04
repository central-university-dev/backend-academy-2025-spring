package ru.tbank.sem5;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class JdbcExamples {

    public static void main(String[] args) throws SQLException {
        listRegisteredDrivers();
        getConnectionExample();
        statementExample();
        preparedStatementExample();
        dataSourceUsage();
    }

    private static void listRegisteredDrivers() {
        var drivers = DriverManager.getDrivers();

        while (drivers.hasMoreElements()) {
            var driver = drivers.nextElement();
            System.out.println(driver);
        }
    }

    private static Connection getConnectionExample() throws SQLException {
        String url = "jdbc:postgresql://localhost:5432/seminar5";
        String username = "postgres";
        String password = "password";
        var connection = DriverManager.getConnection(url, username, password);

        System.out.println(connection);
        return connection;
    }

    private static void statementExample() throws SQLException {
        var conn = getConnectionExample();

        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT 1 + 1");

        while (rs.next()) {
            var num = rs.getInt(1);
            System.out.println(num);
        }
    }

    private static void preparedStatementExample() throws SQLException {
        var conn = getConnectionExample();

        PreparedStatement stmt = conn.prepareStatement("SELECT ? * 3");
        stmt.setInt(1, 12);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            var num = rs.getInt(1);
            System.out.println(num);
        }
    }

    private static void transactionExample() throws SQLException {
        var conn = getConnectionExample();

        conn.setAutoCommit(false);
        try {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("UPDATE users SET balance = balance - 100 WHERE id = 1");
            stmt.executeUpdate("UPDATE users SET balance = balance + 100 WHERE id = 2");
            conn.commit(); // Фиксируем изменения
        } catch (SQLException e) {
            conn.rollback(); // Откат изменений при ошибке
        } finally {
            conn.close();
        }
    }

    public static DataSource createDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://localhost:5432/seminar5");
        config.setUsername("postgres");
        config.setPassword("password");
        config.setMaximumPoolSize(10); // Максимальное количество соединений в пуле
        config.setMinimumIdle(2); // Минимальное количество свободных соединений
        config.setIdleTimeout(30000); // Время простоя перед закрытием соединения
        return new HikariDataSource(config);
    }

    public static void dataSourceUsage() throws SQLException {
        var ds = createDataSource();
        var connection = ds.getConnection();

        // выполнение запросов как обычно
        connection.createStatement();
        // ...
    }



}
