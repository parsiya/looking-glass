package looking_glass;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;

import org.sqlite.SQLiteConfig;

import looking_glass.common.Constants;
import looking_glass.common.Log;

public class DB {

    // Adding these as fields so we do not recreate them every time.
    public static String insertRequest;
    public static String insertResponse;

    // Connect to a SQLite database. The DB file will be created if the file
    // doesn't exist.
    public static Connection connect(String path) throws SQLException, ClassNotFoundException {
        String url = Constants.SQLITE_JDBC_PREFIX + path;
        // Is this thing on? Checks if the class is available.
        Class.forName("org.sqlite.JDBC");

        SQLiteConfig config = new SQLiteConfig();
        config.enforceForeignKeys(true);

        // Is this thing on? Can you see the class, extension?
        Connection connection = DriverManager.getConnection(url);
        Log.toOutput("Connected to the database at: " + path);

        // Add the columns to the create table queries.
        String reqTable = generateCreateTableQuery(Constants.CREATE_REQUEST_TABLE, Constants.REQUEST_FIELDS);
        String resTable = generateCreateTableQuery(Constants.CREATE_RESPONSE_TABLE, Constants.RESPONSE_FIELDS);

        // ZZZ remove after debugging
        // Log.toOutput("reqTable: " + reqTable);
        // Log.toOutput("resTable: " + resTable);
        // String insertReq = generateInsertQuery(Constants.INSERT_REQUEST, Constants.REQUEST_FIELDS);
        // String insertRes = generateInsertQuery(Constants.INSERT_RESPONSE, Constants.RESPONSE_FIELDS);


        // Populate the insert queries. This method usually called once or twice
        // per database so it's much better than creating them for each use.
        DB.insertRequest = generateInsertQuery(Constants.INSERT_REQUEST, Constants.REQUEST_FIELDS);
        DB.insertResponse = generateInsertQuery(Constants.INSERT_RESPONSE, Constants.RESPONSE_FIELDS);
        // ZZZ remove after debugging
        // Log.toOutput("insertReq: " + DB.insertRequest);
        // Log.toOutput("insertRes: " + DB.insertResponse);

        // Run the table queries from above on the connection.
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(reqTable);
            stmt.execute(resTable);
        }
        return connection;
    }

    // Adds the fields to the CREATE table query.
    private static String generateCreateTableQuery(String queryTemplate, String[][] fields) {
        // Adds `name type` from the constant to the create table query to get
        // CREATE TABLE IF NOT EXISTS request (
        // request_id INTEGER PRIMARY KEY,
        // data JSONB,
        // url TEXT,
        // ... );
        String fieldDefinitions = String.join(
                ",",
                Arrays.stream(fields).map(field -> field[0] + " " + field[1]).toArray(String[]::new));
        return String.format(queryTemplate, fieldDefinitions);
    }

    // Adds the fields to the INSERT query.
    private static String generateInsertQuery(String queryTemplate, String[][] fields) {
        // Adds the field names to the first placeholder inside
        // `INSERT INTO request (%s)`.
        String fieldNames = String.join(",", Arrays.stream(fields).map(field -> field[0]).toArray(String[]::new));
        // Adds the ? to the second place holder inside `VALUES (%s);` to get
        // `VALUES (?, ?, ...);`
        String placeholders = String.join(", ", Collections.nCopies(fields.length, "?"));
        return String.format(queryTemplate, fieldNames, placeholders);
    }
}
