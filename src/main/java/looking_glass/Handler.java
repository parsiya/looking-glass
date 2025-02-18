package looking_glass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.ZoneId;
import java.util.Date;

import burp.api.montoya.core.Registration;
import burp.api.montoya.core.ToolType;
import burp.api.montoya.http.handler.*;
import looking_glass.common.Log;
import looking_glass.common.Utils;
import looking_glass.message.Request;
import looking_glass.message.Response;

// Handler class to handle the requests and responses.

public class Handler implements HttpHandler {

    private static Handler handler;

    private Registration registration;
    private Connection connection;

    private Handler() {
    }

    // Singleton pattern.
    public static Handler getInstance() throws Exception {
        if (handler == null) {
            handler = new Handler();
        }
        return handler;
    }

    public void closeDBConnection() throws SQLException {
        if (connection != null) {
            connection.close();
            Log.toOutput("Closed the DB connection.");
        }
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    // Set the registration object.
    public void register(Registration registration) {
        this.registration = registration;
    }

    // Deregister the handler.
    public void deregister() {
        if (this.registration != null) {
            this.registration.deregister();
            Log.toOutput("Deregistered the handler.");
        } else {
            Log.toOutput("The handler is null, skipping deregister().");
        }

    }

    // Is the handler registered?
    public boolean isRegistered() {
        if (this.registration != null) {
            return this.registration.isRegistered();
        }
        return false;
    }

    // What happens to each request immediately before it's sent out.
    @Override
    public RequestToBeSentAction handleHttpRequestToBeSent(HttpRequestToBeSent request) {

        // Request req = new Request(request, request.annotations(),
        // request.toolSource().toolType());

        // Do we trust Burp API's hashcode to generate a unique number for each
        // request or response?

        // For now we will ignore the outgoing requests and only focus on
        // requests that have a response.
        // Just send the request as is for now.
        return RequestToBeSentAction.continueWith(request);
    }

    // What happens to the response immediately after it's received.
    @Override
    public ResponseReceivedAction handleHttpResponseReceived(HttpResponseReceived response) {
        // Implement your logic here
        ToolType toolType = response.toolSource().toolType();

        Response res = new Response(response, toolType);
        Request req = new Request(response.initiatingRequest(), response.annotations(), toolType);

        String reqString = Utils.toJson(req);
        String resString = Utils.toJson(res);

        // Store the request and response in the DB.
        // Insert data into the request table.
        // DB.insertRequest:
        // INSERT INTO request
        // (data,url,method,path,host,port,is_https,notes,tool_source,content_type,content_length,origin,referer,parameter_names,cookie_names,header_names)
        // VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
        try (PreparedStatement insertReq = connection.prepareStatement(
                DB.insertRequest,
                Statement.RETURN_GENERATED_KEYS)) {
            // Remember, index starts at 1.
            insertReq.setString(1, reqString);
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

            // Get the request ID (foreign key)
            try (ResultSet generatedKeys = insertReq.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int requestId = generatedKeys.getInt(1);

                    // INSERT INTO response (request_id,
                    // data,status_code,reason_phrase,content_type,inferred_content_type,content_length,date,cookie_names,tool_source,server,content_security_policy,header_names,inferred_content_type)
                    // VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);

                    // Insert data into the response table with the foreign key
                    try (PreparedStatement insertRes = connection.prepareStatement(DB.insertResponse)) {
                        insertRes.setInt(1, requestId);
                        insertRes.setString(2, resString);
                        insertRes.setInt(3, res.statusCode);
                        insertRes.setString(4, res.reasonPhrase);
                        insertRes.setString(5, res.contentType);
                        insertRes.setString(6, res.inferredContentType);
                        insertRes.setInt(7, res.contentLength);
                        insertRes.setDate(8, new java.sql.Date(res.date.toEpochMilli()));
                        insertRes.setString(9, res.cookieNames);
                        insertRes.setString(10, res.toolSource);
                        insertRes.setString(11, res.server);
                        insertRes.setBoolean(12, res.contentSecurityPolicy);
                        insertRes.setString(13, res.headerNames);
                        insertRes.setString(14, res.inferredContentType);
                        insertRes.execute();
                    }
                } else {
                    throw new SQLException("Failed to retrieve generated key");
                }
            }
        } catch (Exception e) {
            Log.toError("Error inserting data into the DB: " + e.getMessage());
        }
        // Continue with the response without any modifications.
        return ResponseReceivedAction.continueWith(response);
    }

}
