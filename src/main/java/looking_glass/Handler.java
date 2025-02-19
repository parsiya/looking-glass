package looking_glass;

import java.sql.Connection;
import java.sql.SQLException;

import burp.api.montoya.core.Registration;
import burp.api.montoya.core.ToolType;
import burp.api.montoya.http.handler.*;
import looking_glass.common.Log;
import looking_glass.db.DB;
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
    public static Handler getInstance() {
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

    public Connection getConnection() {
        return this.connection;
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
            Log.toOutput("The registration is null, skipping deregister().");
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

        try {
            int reqId = DB.insertRequest(req, connection);
            DB.insertResponse(res, connection, reqId);
        } catch (Exception e) {
            Log.toError(e.getMessage());
        }

        // Continue with the response without any modifications.
        return ResponseReceivedAction.continueWith(response);
    }

}
