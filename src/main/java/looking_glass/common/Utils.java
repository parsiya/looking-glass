package looking_glass.common;

import java.awt.Color;
import java.awt.Component;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JOptionPane;

import java.time.Instant;
import java.util.List;
import java.lang.reflect.Type;
import java.nio.file.Files;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.HttpHeader;
import burp.api.montoya.ui.Theme;

import looking_glass.Handler;
import looking_glass.message.Header;
import looking_glass.ui.DBModal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

public class Utils {

    private static MontoyaApi api;

    public static void initialize(MontoyaApi api) {
        Utils.api = api;
    }

    // Return the MontoyaAPI object.
    public static MontoyaApi api() {
        return api;
    }

    // Returns the value of the string in the list. If the header doesn't exist
    // returns null.
    public static String getHeader(String name, List<Header> headers) {
        return headers.stream()
                .filter(header -> header.name().equalsIgnoreCase(name))
                .map(HttpHeader::value)
                .findFirst()
                .orElse(null);
    }

    // Convert a string containing an HTTP date to a java.time.Instant.
    public static Instant parseHttpDate(String date) {
        return DateUtils.parseStandardDate(date);
    }

    // Convert a Java object to JSON.
    public static String toJson(Object obj) throws Exception {
        Gson gson = new GsonBuilder()
                .disableHtmlEscaping()
                .serializeNulls()
                .setPrettyPrinting()
                .registerTypeAdapter(Instant.class,
                        (JsonSerializer<Instant>) (src, typeOfSrc, context) -> new JsonPrimitive(src.toString()))
                .create();
        return gson.toJson(obj);
    }

    public static <T> T fromJson(String json, Type typeOfT) throws Exception {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Instant.class,
                        (JsonSerializer<Instant>) (src, typeOfSrc, context) -> new JsonPrimitive(src.toString()))
                .create();
        return gson.fromJson(json, typeOfT);
    }

    // ==================== Burp Settings Utilities ====================

    // Return the value of a key from the extension's settings.
    public static String getKey(String key) {
        return api().persistence().preferences().getString(key);
    }

    // Set the value of a key in the extension's settings.
    public static void setKey(String key, String value) {
        api().persistence().preferences().setString(key, value);
    }

    // Return the database path from the extension's settings.
    public static String getDBPath() {
        return getKey(Constants.DB_PATH_KEY);
    }

    // Set the database path in the extension's settings.
    public static void setDBPath(String dbPath) {

        setKey(Constants.DB_PATH_KEY, dbPath);
    }

    // Return the stored queries from the extension's settings.
    public static String getQueries() {
        return getKey(Constants.QUERIES_KEY);
    }
    // Set the stored queries in the extension's settings.
    public static void setQueries(String queries) {
        setKey(Constants.QUERIES_KEY, queries);
    }

    // ==================== Capture Utilities ====================

    // Returns true if the capture status is "active."
    public static boolean isCapturing() {
        return Constants.CAPTURE_STATUS_ACTIVE.equals(getCaptureStatus());
    }

    // Sets the capture status in extension settings.
    // True: "active".
    // False: "inactive".
    // Note: This doesn't pause capture on the fly. But when the extension wants to
    // register a handler on startup, it will check this and act accordingly.
    public static void setCaptureStatus(boolean status) {
        if (status) {
            setKey(Constants.CAPTURE_STATUS_KEY, Constants.CAPTURE_STATUS_ACTIVE);
        } else {
            setKey(Constants.CAPTURE_STATUS_KEY, Constants.CAPTURE_STATUS_INACTIVE);
        }
    }

    // Get the capture status from the extension's settings.
    public static String getCaptureStatus() {
        return getKey(Constants.CAPTURE_STATUS_KEY);
    }

    // Register the HTTPHandler and start capturing.
    public static void startCapture(boolean startup) {
        // Enable the HttpHandler.
        try {
            // Get the handler.
            Handler httpHandler = Handler.getInstance();
            // If the database connection is not established, show the DB modal.
            if (httpHandler.getConnection() == null) {
                // Detect if we're running in the extension or in the startup.
                if (startup) {
                    // If we're running in the startup, don't show the DB modal.
                    DBModal.showStartup();
                } else {
                    // Otherwise, show the DB modal.
                    DBModal.show();
                }
            }
            // Check if the handler has a connection. If not, it means the user
            // did not choose a DB in the DBModal.
            if (httpHandler.getConnection() == null) {
                msgBox("Error", "Please choose a DB to start capturing.");
                Log.toError("User did not choose a DB, capture did not start.");
                setCaptureStatus(false);
                return;
            }
            httpHandler.register(api().http().registerHttpHandler(httpHandler));
            // Log.toOutput("Registered the handler.");
            setCaptureStatus(true);
        } catch (Exception e) {
            msgBox("Error", "Error registering handler: " + e.getMessage());
            Log.toError("Error registering handler: " + e.getMessage());
        }
    }

    // Deregister the HTTPHandler and stop capturing.
    public static void stopCapture() {
        try {
            Handler httpHandler = Handler.getInstance();
            httpHandler.deregister();
            setCaptureStatus(false);
            // Close the DB connection in case we want to use the DB while the extension is
            // loaded.
            httpHandler.closeDBConnection();
            // Store the queries in the extension config.
            Handler.getInstance().saveQueries();
        } catch (Exception e) {
            msgBox("Error", "Error deregistering handler: " + e.getMessage());
            Log.toError("Error deregistering handler: " + e.getMessage());
        }
    }

    // Return the settings string from the extension settings.
    public static String getSettings() {
        return getKey(Constants.SETTINGS_KEY);
    }

    // Save the settings string to the extension settings.
    public static void setSettings(String settings) {
        setKey(Constants.SETTINGS_KEY, settings);
    }

    // ==================== Java Swing Utilities ====================

    // Return the Burp frame to use in Swing.
    public static Component burpFrame() {
        return api().userInterface().swingUtils().suiteFrame();
    }

    // Display a message box.
    public static void msgBox(String title, String message) {
        JOptionPane.showMessageDialog(burpFrame(), message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    // Apply Burp look and feel to a component. Doesn't look like its working
    // for buttons. It keeps the background color of the button.
    public static void applyBurpStyle(Component component) {
        api().userInterface().applyThemeToComponent(component);
    }

    // Set the background color of a Component based on theme.
    // Dark theme: 76, 80, 82
    // Light theme: white
    public static void setBackground(Component component) {
        if (api().userInterface().currentTheme() == Theme.DARK) {
            component.setBackground(new Color(76, 80, 82));
        } else {
            component.setBackground(Color.white);
        }
    }

    // Read a file from /src/main/resources/ and return it as a string.
    public static String readResourceFile(String fileName) throws Exception {
        try (InputStream inputStream = Utils.class.getResourceAsStream(fileName)) {
            return new String(inputStream.readAllBytes());
        }
    }
}
