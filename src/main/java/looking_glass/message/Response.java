package looking_glass.message;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.time.Instant;

import com.google.gson.annotations.Expose;

import burp.api.montoya.core.ToolType;
import burp.api.montoya.http.message.MimeType;
import burp.api.montoya.http.message.responses.HttpResponse;

import looking_glass.common.Utils;

public class Response {
    public short statusCode;
    public String reasonPhrase, httpVersion, body;

    // Custom fields from headers.

    // Burp's response.MimeType returns an enum with only the Burp's recognized
    // types (see below). I want something freeform here.
    public String contentType;
    // MimeType recognized by Burp. Note, this is limited.
    // @Expose(serialize = false) // Do not serialize this to JSON.
    public MimeType burpMimeType;

    public int contentLength;

    // Date.
    public Instant date;

    // Tool source.
    public String toolSource;

    public List<Header> headers;
    public List<Cookie> cookies;

    // True if the `Content-Security-Policy` exists.
    public boolean contentSecurityPolicy;

    // Some special response headers.
    public String server;

    // Comma-separated header and cookie names.
    public String headerNames, cookieNames;

    // End of fields.

    // Constructor to populate the fields from a HttpResponse object.
    public Response(HttpResponse response, ToolType toolSource) {

        response.mimeType();
        response.statedMimeType();
        response.inferredMimeType();
        // Go through the headers and store them in a list of Header objects.
        List<Header> parsedHeaders = response.headers().stream()
                .map(header -> new Header(header.name(), header.value()))
                .collect(Collectors.toList());

        // Store parsed headers.
        this.headers = parsedHeaders;

        this.statusCode = response.statusCode();
        this.reasonPhrase = response.reasonPhrase();
        this.httpVersion = response.httpVersion();
        this.body = response.bodyToString();

        this.contentType = this.getHeader("Content-Type");
        this.burpMimeType = response.mimeType();

        // Only parse if the `Date` header exists, otherwise set it to null.
        String dateHeader = this.getHeader("Date");
        this.date = (dateHeader != null) ? Utils.parseHttpDate(dateHeader) : null;

        this.toolSource = toolSource.toolName();

        this.server = this.getHeader("Server");

        // What about other CSP headers that might be present without
        // the "Content-Security-Policy" header like "-Report-Only"?
        String cspHeader = this.getHeader("Content-Security-Policy");
        this.contentSecurityPolicy = (cspHeader != null) ? true : false;

        // Only parse if the `Content-Length` header exists, otherwise set it to 0.
        String contentLengthHeader = this.getHeader("Content-Length");
        this.contentLength = (contentLengthHeader != null) ? Integer.parseInt(contentLengthHeader) : 0;

        // Convert cookies from Burp format to ours.
        this.cookies = new ArrayList<Cookie>();
        for (burp.api.montoya.http.message.Cookie burpCookie : response.cookies()) {
            this.cookies.add(new Cookie(burpCookie));
        }

        // Extract cookie names.
        this.cookieNames = this.cookies.stream()
                .map(c -> c.name)
                .collect(Collectors.joining(","));

        this.headerNames = this.headers.stream()
                .map(h -> h.name())
                .collect(Collectors.joining(","));
    }

    public String getHeader(String name) {
        return Utils.getHeader(name, this.headers);
    }
}
