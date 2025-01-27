package lookingglass;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

    // Convert a string containing an HTTP date to a java.util.Date object.
    public static Date parseHttpDate(String date) {
        // Parse the date.
        return new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.UK).parse(date);

        // There is an apache package that does it, see if I can just get the
        // code for the method and not import all of the package because it's
        // too big. The license is APache 2.0 so it should be fine.
        // make sure to include the code in a separate file with the license
        // and attribution.
    }
}
