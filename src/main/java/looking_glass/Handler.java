package looking_glass;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;

import com.google.gson.reflect.TypeToken;

import burp.api.montoya.core.Registration;
import burp.api.montoya.core.ToolType;
import burp.api.montoya.http.handler.*;

import looking_glass.common.Constants;
import looking_glass.common.Log;
import looking_glass.common.Utils;
import looking_glass.db.DB;
import looking_glass.message.Filter;
import looking_glass.message.Request;
import looking_glass.message.Response;
import looking_glass.query.Query;

// Handler class to handle the requests and responses.

public class Handler implements HttpHandler {

    private static Handler handler;

    private Registration registration;
    private Connection connection;
    private Filter filter;
    private ExtensionSettings settings;
    private DefaultListModel<Query> queries;

    private Handler() {
    }

    // Singleton pattern.
    public static Handler getInstance() {
        if (handler == null) {
            handler = new Handler();
        }
        try {
            String settingsString = Utils.getSettings();
            handler.setSettings(new ExtensionSettings(settingsString));
            handler.loadQueries();
        } catch (Exception e) {
            Log.toError("Error reading settings: " + e.getMessage());
            handler.setSettings(ExtensionSettings.getDefault());
            Log.toError("Using default settings.");
        }
        return handler;
    }

    // ========== DB connection ==========
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

    // ========== Settings ==========
    public void setSettings(ExtensionSettings settings) {
        this.settings = settings;
        // Update the filter, too.
        this.filter = new Filter(settings);
        // Store the settings in the config.

        // Convert it to json.
        String json = "";
        try {
            json = settings.toJson();
        } catch (Exception e) {
            Log.toError("Error converting extension settings to JSON: " + e.getMessage());
            Utils.msgBox("Error converting extension settings to JSON:", e.getMessage());
            return;
        }
        // Store it in the settings key.
        Utils.setSettings(json);
    }

    public ExtensionSettings getSettings() {
        return this.settings;
    }

    // ========== Queries ==========

    public DefaultListModel<Query> getQueries() {
        return this.queries;
    }

    public void setQueries(DefaultListModel<Query> queries) {
        this.queries = queries;
        // Save queries to the extension config.
        this.saveQueries();
    }

    // Save queries to the extension config.
    public void saveQueries() {
        // Convert DefaultListModel to a List<Query>
        if (this.queries == null) {
            this.queries = new DefaultListModel<Query>();
        }
        List<Query> q = new ArrayList<>();
        for (int i = 0; i < this.queries.size(); i++) {
            q.add(this.queries.get(i));
        }
        try {
            // Type listType = new TypeToken<List<Query>>() {}.getType();
            String qString = Utils.toJson(q);
            Utils.setKey(Constants.QUERIES_KEY, qString);
        } catch (Exception e) {
            Log.toError("Couldn't convert queries to JSON: " + e.getMessage());
            Utils.setKey(Constants.QUERIES_KEY, "");
        }
    }

    // Load queries from the extension config.
    public void loadQueries() {
        this.queries = new DefaultListModel<Query>();
        String qString = Utils.getKey(Constants.QUERIES_KEY);
        try {
            if (qString != null) {
                Type type = new TypeToken<List<Query>>() {
                }.getType();
                List<Query> qu = Utils.fromJson(qString, type);
                for (Query query : qu) {
                    this.queries.addElement(query);
                }
            }
        } catch (Exception e) {
            // ZZZ: Load default queries here.
            // Couldn't read stored queries.
            Log.toError("Couldn't read stored queries: " + e.getMessage());
            Log.toError("Creating an empty list of queries.");
            this.queries = new DefaultListModel<Query>();
        }
    }

    // ========== Handler registration ==========

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

    // ========== Overriding methods for capturing traffic ==========

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

        ToolType toolType = response.toolSource().toolType();

        Response res = new Response(response, toolType);
        Request req = new Request(response.initiatingRequest(), response.annotations(), toolType);

        // Check the request against filters.

        // 1. If the host doesn't match the filter, do not store it.
        if (!this.filter.hostMatches(req))
            return ResponseReceivedAction.continueWith(response);

        // 2. If the request body should be empty, set the request body to an empty
        // string.
        if (!this.filter.storeBody(req)) {
            req.body = "";
        }

        // 3. If the response body should be empty, set the response body to an empty
        // string.
        if (!this.filter.storeBody(res)) {
            res.body = "";
        }

        // 4. Store the request/response.
        try {
            // If the connection is closed, open it.
            if (connection.isClosed()) {
                connection = DB.connect(Utils.getDBPath());
            }
            int reqId = DB.insertRequest(req, connection);
            DB.insertResponse(res, connection, reqId);
        } catch (Exception e) {
            Log.toError(e.getMessage());
        }

        // Continue with the response without any modifications.
        return ResponseReceivedAction.continueWith(response);
    }

}
