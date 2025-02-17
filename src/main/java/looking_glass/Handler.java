package looking_glass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import burp.api.montoya.core.Registration;
import burp.api.montoya.core.ToolType;
import burp.api.montoya.http.handler.*;
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
            // Set the connection.
            handler.setConnection(DB.connect(Utils.getDBPath()));
        }
        return handler;
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
        this.registration.deregister();
    }

    // Is the handler registered?
    public boolean isRegistered() {
        return this.registration.isRegistered();
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
        // Insert data into the request table
        // Insert data into the request table
        String insertRequestSql = "INSERT INTO request (data) VALUES (?)";
        try (PreparedStatement insertReq = connection.prepareStatement(insertRequestSql,
                Statement.RETURN_GENERATED_KEYS)) {
            insertReq.setString(1, reqString);
            insertReq.execute();

            // Get the request ID (foreign key)
            try (ResultSet generatedKeys = insertReq.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int requestId = generatedKeys.getInt(1);

                    // Insert data into the response table with the foreign key
                    String insertResp = "INSERT INTO response (request_id, data) VALUES (?, ?)";
                    try (PreparedStatement insertResponseStatement = connection.prepareStatement(insertResp)) {
                        insertResponseStatement.setInt(1, requestId);
                        insertResponseStatement.setString(2, resString);
                        insertResponseStatement.execute();
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
