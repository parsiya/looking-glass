package looking_glass;

import java.time.Instant;
import java.util.List;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.HttpHeader;

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

    // Define a static method that takes a string and returns the value of that
    // in a headers object from burp montoya API. If the header doesn't exist or
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
    public static String toJson(Object obj) {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(Instant.class,
                        (JsonSerializer<Instant>) (src, typeOfSrc, context) -> new JsonPrimitive(src.toString()))
                .create();
        return gson.toJson(obj);
    }

    // Return the value of a key from the extension's configuration.
    public static String getKey(String key) {
        return api().persistence().extensionData().getString(key);
    }

    // Set the value of a key in the extension's configuration.
    public static void setKey(String key, String value) {
        api().persistence().extensionData().setString(key, value);
    }

    // Return the database path from the extension's configuration.
    public static String getDBPath() {
        return getKey(Constants.DB_PATH_KEY);
    }

    // Set the database path in the extension's configuration.
    public static void setDBPath(String dbPath) {
        setKey(Constants.DB_PATH_KEY, dbPath);
    }

    // Set the capture status in the extension's configuration to "active."
    public static void setActiveCaptureStatus() {
        setKey(Constants.CAPTURE_STATUS_KEY, Constants.CAPTURE_STATUS_ACTIVE);
    }

    // Set the capture status in the extension's configuration to "inactive."
    public static void setInactiveCaptureStatus() {
        setKey(Constants.CAPTURE_STATUS_KEY, Constants.CAPTURE_STATUS_INACTIVE);
    }

    // Get the capture status from the extension's configuration.
    public static String getCaptureStatus() {
        return getKey(Constants.CAPTURE_STATUS_KEY);
    }
}
