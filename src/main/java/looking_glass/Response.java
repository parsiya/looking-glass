package looking_glass;

import java.util.List;
import java.time.Instant;

import burp.api.montoya.core.ToolType;
import burp.api.montoya.http.message.Cookie;
import burp.api.montoya.http.message.HttpHeader;
import burp.api.montoya.http.message.responses.HttpResponse;

public class Response {
    public short statusCode;
    public String reasonPhrase, httpVersion, body;

    // Custom fields from headers.

    // Burp's response.MimeType returns an enum with recognized types. I want
    // something freeform here.
    public String contentType;

    // response.inferredMimeType returns an enum with some types that Burp has
    // found. Let's store it in case I need it.
    public String inferredContentType;

    // Get these from Attribute. What is inside `Attribute`?
    public int contentLength;

    // Date.
    public Instant date;

    public List<HttpHeader> headers;

    // Response cookies are different from request cookies because they contain
    // more info like path.
    public List<Cookie> cookies;

    // Tool source.
    public ToolType toolSource;

    // Constructor to populate the fields from a HttpResponse object.
    public Response(HttpResponse response, ToolType toolSource) {
        this.statusCode = response.statusCode();
        this.reasonPhrase = response.reasonPhrase();
        this.httpVersion = response.httpVersion();
        this.body = response.bodyToString();

        this.contentType = Utils.getHeader("Content-Type", response.headers());
        this.inferredContentType = response.inferredMimeType().toString();

        // Only parse if the `Date` header exists, otherwise set it to null.
        String dateHeader = Utils.getHeader("Date", response.headers());
        this.date = (dateHeader != null) ? Utils.parseHttpDate(dateHeader) : null;

        // Only parse if the `Content-Length` header exists, otherwise set it to 0.
        String contentLengthHeader = Utils.getHeader("Content-Length", response.headers());
        this.contentLength = (contentLengthHeader != null) ? Integer.parseInt(contentLengthHeader) : 0;

        this.headers = response.headers();
        this.cookies = response.cookies();

        this.toolSource = toolSource;
    }

    public String getHeader(String name) {
        return Utils.getHeader(name, this.headers);
    }
}
