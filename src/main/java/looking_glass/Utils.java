package looking_glass;

import java.time.Instant;
import java.util.List;

import burp.api.montoya.http.message.HttpHeader;

public class Utils {

    // Define a static method that takes a string and returns the value of that
    // in a headers object from burp montoya API. If the header doesn't exist or
    // returns null.
    public static String getHeader(String name, List<HttpHeader> headers) {
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

    // Do not let users instantiate this class.
    private Utils() {
    }
}
