
package com.example.admission;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseUtil {

    private DatabaseUtil() {
    }

    public static Connection getConnection() throws SQLException {
        String url = System.getenv().getOrDefault(
            "DB_URL",
            "jdbc:mysql://localhost:3306/student_registration"
        );

        String user = System.getenv().getOrDefault("DB_USER", "root");
        String password = System.getenv().getOrDefault("DB_PASSWORD", "");

        return DriverManager.getConnection(url, user, password);
    }
}
