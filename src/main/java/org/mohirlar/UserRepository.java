package org.mohirlar;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UserRepository {

    private static final String UPSERT_SQL = """
        INSERT INTO bot_users (telegram_id, username, full_name, location)
        VALUES (?, ?, ?, NULL)
        ON CONFLICT (telegram_id) DO UPDATE
          SET username = EXCLUDED.username,
              full_name = EXCLUDED.full_name,
              location = bot_users.location
        """;

    public void upsertUser(long telegramId, String username, String fullName) {
        try (Connection conn = DriverManager.getConnection(
                DbConfig.JDBC_URL, DbConfig.JDBC_USER, DbConfig.JDBC_PASS);
             PreparedStatement ps = conn.prepareStatement(UPSERT_SQL)) {

            ps.setLong(1, telegramId);
            ps.setString(2, username);   // null bo‘lishi mumkin
            ps.setString(3, fullName);   // NOT NULL

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("DB upsert error: " + e.getMessage(), e);
        }
    }
}
