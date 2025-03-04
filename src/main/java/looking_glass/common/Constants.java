package looking_glass.common;

public class Constants {
    public static final String EXTENSION_NAME = "Looking Glass";

    // --------------------------------------------------
    // Extension settings strings
    // --------------------------------------------------

    // The key for the DB path.
    public static final String DB_PATH_KEY = "db_path";
    // The key for capture status.
    public static final String CAPTURE_STATUS_KEY = "capture";
    // Value of the capture status key when the extension is capturing.
    public static final String CAPTURE_STATUS_ACTIVE = "active";
    // Value of the capture status key when the extension is not capturing.
    public static final String CAPTURE_STATUS_INACTIVE = "inactive";
    // Value of the settings key.
    public static final String SETTINGS_KEY = "settings";

    // --------------------------------------------------
    // SQL strings and field definitions
    // --------------------------------------------------

    // Create the request table if it doesn't exist.
    // The format string adds a `,` after `data JSONB`.
    public static final String CREATE_REQUEST_TABLE = "CREATE TABLE IF NOT EXISTS request (request_id INTEGER PRIMARY KEY, %s);";
    // Create the response table if it doesn't exist.
    // The format string adds a `,` after `data JSONB`.
    public static final String CREATE_RESPONSE_TABLE = """
                CREATE TABLE IF NOT EXISTS response (
                response_id INTEGER PRIMARY KEY,
                request_id INTEGER,
                %s,
                FOREIGN KEY (request_id) REFERENCES request (request_id)
                );
            """;

    public static final String INSERT_REQUEST = "INSERT INTO request (%s) VALUES (%s);";

    public static final String INSERT_RESPONSE = "INSERT INTO response (request_id, %s) VALUES (?, %s);";

    // Prefix for SQLite JDBC connections.
    public static final String SQLITE_JDBC_PREFIX = "jdbc:sqlite:";

    // Columns for request fields that are extracted and stored separately.
    public static final String[][] REQUEST_FIELDS = {
            { "data", "JSONB" },
            { "url", "TEXT" },
            { "method", "TEXT" },
            { "path", "TEXT" },
            { "host", "TEXT" },
            { "port", "INTEGER" },
            { "is_https", "BOOLEAN" },
            { "notes", "TEXT" },
            { "tool_source", "TEXT" },
            { "content_type", "TEXT" },
            { "content_length", "INTEGER" },
            { "origin", "TEXT" },
            { "referer", "TEXT" },
            { "parameter_names", "TEXT" },
            { "cookie_names", "TEXT" },
            // Comma-separated list of all headers.
            // This is useful to see if the request has a header that we can use to extract
            // from the JSON blob.
            { "header_names", "TEXT" },
    };
    // Headers skipped in the above:
    // "authorization_type": When I manage to populate it and figure out what I want
    // there.
    // "highlight_color": People could tag interesting requests and then retrieve
    // them later.
    // "http_version": No idea how useful this would be.
    // "user_agent": Useful for thick client tests to see which app sent what.
    // "jwt_claims": Name of the claims extracted from a JWT (if any). This might
    // not be as useful because a lot of places do not use JWT. But JWT is popular
    // enough to justify an empty column?
    // "sec_fetch_mode": Might have useful info.
    // https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Sec-Fetch-Mode
    // "sec_fetch_site": Might have useful info. E.g., find user-initiated requests.
    // https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Sec-Fetch-Site
    // x-forwarded headers. Are they useful enough to warrant separate columns?

    // Columns for request fields that are extracted and stored separately.
    public static final String[][] RESPONSE_FIELDS = {
            { "data", "JSONB" },
            { "status_code", "INTEGER" },
            { "reason_phrase", "TEXT" },
            { "content_type", "TEXT" },
            { "inferred_content_type", "TEXT" },
            { "content_length", "INTEGER" },
            { "date", "NUMERIC" },
            // List of cookie names set by the response.
            { "cookie_names", "TEXT" },
            { "tool_source", "TEXT" },
            { "server", "TEXT" },
            // Should I store the value or just a field that says it exists.
            // If it's just a field that says it's there, it can be skipped for
            // "header_names"
            { "content_security_policy", "TEXT" },
            // Comma-separated list of all headers.
            // This is useful to see if the request has a header that we can use to extract
            // from the JSON blob.
            { "header_names", "TEXT" },
            // inferred content type from Burp.
    };
    // Headers skipped in the above:
    // "http_version": How useful?
    // "Access-Control-*" headers.

    // --------------------------------------------------
    // UI Text
    // --------------------------------------------------

    // The three button labels for the DB modal.
    public static final String[] DB_MODAL_OPTIONS = {
            "Use the file", "Choose a new one", "Pause capture"
    };
}
