package looking_glass;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DB {
    private static final String SQLITE_JDBC_PREFIX = "jdbc:sqlite:";

    private static final String CREATE_REQUEST_TABLE_SQL = """
        CREATE TABLE IF NOT EXISTS request (
        request_id INTEGER PRIMARY KEY,
        data JSONB
        );
    """;

    private static final String CREATE_RESPONSE_TABLE_SQL = """
        CREATE TABLE IF NOT EXISTS response (
        response_id INTEGER PRIMARY KEY,
        data JSONB
        );
    """;

    private static Connection connection;

    // Connect to a SQLite database. The DB file will be created if the file
    // doesn't exist.
    public static void connect(String path) throws SQLException {
        String url = SQLITE_JDBC_PREFIX + path;
        Connection connection = DriverManager.getConnection(url);
        Log.toOutput("Connected to the database at: " + path);
        DB.connection = connection;

        // Run the table queries from above on the connection.
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(CREATE_REQUEST_TABLE_SQL);
            stmt.execute(CREATE_RESPONSE_TABLE_SQL);
        }
    }
}
