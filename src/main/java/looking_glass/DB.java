package looking_glass;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.sqlite.SQLiteConfig;

import looking_glass.common.Log;

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
                request_id INTEGER,
                data JSONB,
                FOREIGN KEY (request_id) REFERENCES request (request_id)
                );
            """;

    // Connect to a SQLite database. The DB file will be created if the file
    // doesn't exist.
    public static Connection connect(String path) throws SQLException, ClassNotFoundException {
        String url = SQLITE_JDBC_PREFIX + path;
        // Is this thing on? Checks if the class is available.
        Class.forName("org.sqlite.JDBC");

        SQLiteConfig config = new SQLiteConfig();
        config.enforceForeignKeys(true);

        // Is this thing on? Can you see the class, extension?
        Connection connection = DriverManager.getConnection(url);
        Log.toOutput("Connected to the database at: " + path);

        // Run the table queries from above on the connection.
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(CREATE_REQUEST_TABLE_SQL);
            stmt.execute(CREATE_RESPONSE_TABLE_SQL);
        }
        return connection;
    }
}
