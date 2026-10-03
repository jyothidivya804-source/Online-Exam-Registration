import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    public static Connection getConnection() throws SQLException {
        String url = System.getenv().getOrDefault(
            "ONLINE_EXAM_DB_URL",
            "jdbc:mysql://localhost:3306/online_exam"
        );
        String username = System.getenv("ONLINE_EXAM_DB_USER");
        String password = System.getenv("ONLINE_EXAM_DB_PASSWORD");
        if (username == null || username.isBlank()
                || password == null || password.isBlank()) {
            throw new SQLException(
                "Set ONLINE_EXAM_DB_USER and ONLINE_EXAM_DB_PASSWORD."
            );
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(url, username, password);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL Connector/J is not available.", e);
        }
    }
}