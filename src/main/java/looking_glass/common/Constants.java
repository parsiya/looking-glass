package looking_glass.common;

public class Constants {
    public static final String EXTENSION_NAME = "Looking Glass";

    // The three button labels for the DB modal.
    public static final String[] DB_MODAL_OPTIONS = {
            "Use the file", "Choose a new one", "Pause capture"
    };

    // Extension configuration strings.

    // The key for the DB path.
    public static final String DB_PATH_KEY = "db_path";
    // The key for capture status.
    public static final String CAPTURE_STATUS_KEY = "capture";
    // Value of the capture status key when the extension is capturing.
    public static final String CAPTURE_STATUS_ACTIVE = "active";
    // Value of the capture status key when the extension is not capturing.
    public static final String CAPTURE_STATUS_INACTIVE = "inactive";

    // SQL strings.

    // Create the request table if it doesn't exist.
    public static final String CREATE_REQUEST_TABLE_SQL = """
                CREATE TABLE IF NOT EXISTS request (
                request_id INTEGER PRIMARY KEY,
                data JSONB
                );
            """;
    // Create the response table if it doesn't exist.
    public static final String CREATE_RESPONSE_TABLE_SQL = """
                CREATE TABLE IF NOT EXISTS response (
                response_id INTEGER PRIMARY KEY,
                data JSONB
                );
            """;
    // Prefix for SQLite JDBC connections.
    public static final String SQLITE_JDBC_PREFIX = "jdbc:sqlite:";
}
