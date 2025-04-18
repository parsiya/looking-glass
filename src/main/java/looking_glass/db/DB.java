package looking_glass.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;

import org.sqlite.SQLiteConfig;

import looking_glass.common.Constants;
import looking_glass.common.Utils;
import looking_glass.message.Request;
import looking_glass.message.Response;

public class DB {

    // Adding these as fields so we do not recreate them every time.
    public static String insertRequest;
    public static String insertResponse;

    // Connect to a SQLite database. The DB file will be created if the file
    // doesn't exist.
    public static Connection connect(String path) throws Exception {
        String url = Constants.SQLITE_JDBC_PREFIX + path;
        // Is this thing on? Checks if the class is available.
        Class.forName("org.sqlite.JDBC");

        SQLiteConfig config = new SQLiteConfig();
        config.enforceForeignKeys(true);

        // Is this thing on? Can you see the class, extension?
        Connection connection = DriverManager.getConnection(url);

        // Add the columns to the create table queries.
        String reqTable = generateCreateTableQuery(Constants.CREATE_REQUEST_TABLE, Constants.REQUEST_FIELDS);
        String resTable = generateCreateTableQuery(Constants.CREATE_RESPONSE_TABLE, Constants.RESPONSE_FIELDS);

        // Populate the insert queries. This method is usually called once or
        // twice per database so it's much better than creating them for each
        // use.
        DB.insertRequest = generateInsertQuery(Constants.INSERT_REQUEST, Constants.REQUEST_FIELDS);
        DB.insertResponse = generateInsertQuery(Constants.INSERT_RESPONSE, Constants.RESPONSE_FIELDS);

        // Run the table queries from above on the connection.
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(reqTable);
            stmt.execute(resTable);

            // Read /src/resources/views.sql to get the views.
            String views = Utils.readResourceFile("/views.sql");
            // Split by `;` which works for now because the views only have `;`
            // in the end. But might become problematic if we add complex views
            // in the future.
            String[] statements = views.split(";");

            for (String statement : statements) {
                statement = statement.trim();
                if (!statement.isEmpty()) {
                    // Add the statements to the batch.
                    stmt.addBatch(statement);
                }
            }
            // Add all the views.
            stmt.executeBatch();
        } catch (Exception e) {
            // Close the connection if something goes bad.
            // We do not want to do it in the finally block because we want to
            // close the connection only if we have an error.
            connection.close();
            throw e;
        }
        return connection;
    }

    // Adds the fields to the CREATE table query.
    private static String generateCreateTableQuery(String queryTemplate, String[][] fields) {
        // Adds `name type` from the constant to the create table query to get
        // CREATE TABLE IF NOT EXISTS request (
        // request_id INTEGER PRIMARY KEY,
        // data JSON,
        // url TEXT,
        // ... );
        String fieldDefinitions = String.join(
                ",",
                Arrays.stream(fields).map(field -> field[0] + " " + field[1]).toArray(String[]::new));
        return String.format(queryTemplate, fieldDefinitions);
    }

    // Adds the fields to the INSERT query. This is just text processing. The
    // query is not executed here.
    private static String generateInsertQuery(String queryTemplate, String[][] fields) {
        // Adds the field names to the first placeholder inside
        // `INSERT INTO request (%s)`.
        String fieldNames = String.join(",", Arrays.stream(fields).map(field -> field[0]).toArray(String[]::new));
        // Adds the ? to the second place holder inside `VALUES (%s);` to get
        // `VALUES (?, ?, ...);`
        String placeholders = String.join(", ", Collections.nCopies(fields.length, "?"));
        return String.format(queryTemplate, fieldNames, placeholders);
    }

    // Add the request to the request table. The query looks like
    // DB.insertRequest:
    // INSERT INTO request
    // (data,url,method,path,host,port,is_https,notes,tool_source,content_type,
    // content_length,origin,referer,parameter_names,cookie_names,header_names)
    // VALUES (?, ...);
    public static int insertRequest(Request req, Connection connection) throws Exception {
        try (PreparedStatement insertReq = connection.prepareStatement(
                DB.insertRequest,
                Statement.RETURN_GENERATED_KEYS)) {
            String reqString = Utils.toJson(req);
            // Remember, index starts at 1.
            insertReq.setString(1, reqString); // Request as JSON.
            insertReq.setString(2, req.url);
            insertReq.setString(3, req.method);
            insertReq.setString(4, req.path);
            insertReq.setString(5, req.host);
            insertReq.setInt(6, req.port);
            insertReq.setBoolean(7, req.isHttps);
            insertReq.setString(8, req.notes);
            insertReq.setString(9, req.toolSource);
            insertReq.setString(10, req.contentType);
            insertReq.setInt(11, req.contentLength);
            insertReq.setString(12, req.origin);
            insertReq.setString(13, req.referer);
            insertReq.setString(14, req.parameterNames);
            insertReq.setString(15, req.cookieNames);
            insertReq.setString(16, req.headerNames);
            insertReq.execute();

            // Get ID of the inserted request_id and return it.
            try (ResultSet generatedKeys = insertReq.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Failed to retrieve generated key");
                }
            }
        } catch (Exception e) {
            throw new SQLException("Error inserting data into the DB: " + e.getMessage());
        }
    }

    // Insert the response into the response table.
    // The query looks like:
    // INSERT INTO response (request_id,
    // data,status_code,reason_phrase,content_type,inferred_content_type,
    // content_length,date,cookie_names,tool_source,
    // content_security_policy,header_names)
    // VALUES (?, ...);
    public static void insertResponse(Response res, Connection connection, int requestId) throws Exception {

        // Insert data into the response table with the foreign key
        try (PreparedStatement insertRes = connection.prepareStatement(DB.insertResponse)) {
            String resString = Utils.toJson(res);
            insertRes.setInt(1, requestId);
            insertRes.setString(2, resString);
            insertRes.setInt(3, res.statusCode);
            insertRes.setString(4, res.reasonPhrase);
            insertRes.setString(5, res.contentType);
            insertRes.setString(6, res.burpMimeType.toString()); // ZZZ: change if we change the mimetype.
            insertRes.setInt(7, res.contentLength);
            insertRes.setString(9, res.cookieNames);
            insertRes.setString(10, res.toolSource);
            // insertRes.setString(11, res.server);
            insertRes.setBoolean(11, res.contentSecurityPolicy);
            insertRes.setString(12, res.headerNames);

            // The response header `Date` is optional, so we have to check if
            // it's null before insertion.
            if (res.date != null) {
                insertRes.setDate(8, new java.sql.Date(res.date.toEpochMilli()));
            } else {
                insertRes.setDate(8, null);
            }

            insertRes.execute();
        }
    }
}
