package looking_glass.common;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.HttpHeader;
import looking_glass.message.Header;

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

    // Returns a comma separated string of names of the list. This list 
    // String headerNames = list.stream()
    // .map(h -> h.name())
    // .collect(Collectors.joining(", "));

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

    // Returns true if the capture status is "active."
    public static boolean isCapturing() {
        return Constants.CAPTURE_STATUS_ACTIVE.equals(getCaptureStatus());
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
