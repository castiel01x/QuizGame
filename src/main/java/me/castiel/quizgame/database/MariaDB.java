package me.castiel.quizgame.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.castiel.quizgame.QuizGame;
import me.castiel.quizgame.settings.Settings;
import org.bukkit.Bukkit;
import org.mariadb.jdbc.MariaDbDataSource;

import java.sql.*;
import java.util.concurrent.CompletableFuture;

public class MariaDB {

    MariaDbDataSource empty;
    private HikariDataSource dataSource;

    public MariaDB() {
        Bukkit.getScheduler().runTaskAsynchronously(QuizGame.getInstance(), () -> {
            HikariConfig config = new HikariConfig();
            String jdbc = Settings.DATABASE_JDBC;
            String hostname = Settings.DATABASE_HOST;
            String port = Settings.DATABASE_PORT;
            String database = Settings.DATABASE_NAME;
            String username = Settings.DATABASE_USERNAME;
            String password = Settings.DATABASE_PASSWORD;
            config.setConnectionTestQuery("SELECT 1");
            config.setPoolName("QuizGame Pool");
            config.setJdbcUrl(jdbc
                    .replace("%host%", hostname)
                    .replace("%port%", port)
                    .replace("%database%", database)
                    .replace("%username%", username)
                    .replace("%password%", password));
            config.setDataSourceClassName("org.mariadb.jdbc.MariaDbDataSource");
            config.setMinimumIdle(5);
            config.setMaximumPoolSize(16);
            config.setConnectionTimeout(60000);
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);
            config.setUsername(username);
            config.setPassword(password);
            dataSource = new HikariDataSource(config);
            QuizGame.getInstance().getLogger().info("Connected to MySQL database!");

            QuizGame.getInstance().getLogger().info("Creating tables...");
            try (Connection connection = dataSource.getConnection()) {
                Statement statement = connection.createStatement();
                statement.execute("USE " + database);
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS scores (player_name VARCHAR(16) NOT NULL, points INT NOT NULL, PRIMARY KEY (player_name))");
                statement.close();
            } catch (SQLException e) {
                QuizGame.getInstance().getLogger().severe("Error creating scores table: " + e.getMessage());
                e.printStackTrace();
            }
            QuizGame.getInstance().getLogger().info("Done.");
        });
    }

    public void insertScore(String playerName, int points) {
        Bukkit.getScheduler().runTaskAsynchronously(QuizGame.getInstance(), () -> {
            try (Connection connection = dataSource.getConnection()) {
                String sql = "INSERT INTO scores (player_name, points) VALUES (?, ?) " +
                        "ON DUPLICATE KEY UPDATE points = points + VALUES(points)";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, playerName);
                    statement.setInt(2, points);
                    statement.executeUpdate();
                }
            } catch (SQLException e) {
                QuizGame.getInstance().getLogger().severe("Error inserting score for player '" + playerName + "': " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    public CompletableFuture<Integer> getPlayerScore(String playerName) {
        CompletableFuture<Integer> completableFuture = new CompletableFuture<>();
        Bukkit.getScheduler().runTaskAsynchronously(QuizGame.getInstance(), () -> {
            try (Connection connection = dataSource.getConnection()) {
                String sql = "SELECT points FROM scores WHERE player_name = ?";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, playerName);
                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (resultSet.next()) {
                            completableFuture.complete(resultSet.getInt("points"));
                        }
                        else {
                            completableFuture.complete(0);
                        }
                    }
                }
            } catch (SQLException e) {
                QuizGame.getInstance().getLogger().severe("Error getting score for player '" + playerName + "': " + e.getMessage());
            }
        });
        return completableFuture;
    }


    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}