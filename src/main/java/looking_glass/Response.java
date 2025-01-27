package looking_glass;

import java.util.List;
import java.time.Instant;

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

    // Constructor to populate the fields from a HttpResponse object.
    public Response(HttpResponse response) {
        this.statusCode = response.statusCode();
        this.reasonPhrase = response.reasonPhrase();
        this.httpVersion = response.httpVersion();
        this.body = response.bodyToString();

        this.contentType = Utils.getHeader("Content-Type", response.headers());
        this.inferredContentType = response.inferredMimeType().toString();
        this.contentLength = Integer.parseInt(Utils.getHeader("Content-Length", response.headers()));

        this.headers = response.headers();
        this.cookies = response.cookies();
    }

    public String getHeader(String name) {
        return Utils.getHeader(name, this.headers);
    }
}
