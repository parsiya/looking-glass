package looking_glass;

import java.time.Instant;
import java.util.List;

import burp.api.montoya.core.Annotations;
import burp.api.montoya.core.ToolType;
import burp.api.montoya.http.message.HttpHeader;
import burp.api.montoya.http.message.params.ParsedHttpParameter;
import burp.api.montoya.http.message.requests.HttpRequest;

public class Request {
    public String url, method, path, httpVersion, body;

    // Custom stuff I need to populate from the headers.
    public String contentType; // Get this from the header and not request.ContentType.
    public String userAgent, referer, origin, accept;

    public int contentLength;

    // I can do some processing here and even if we're not storing the value,
    // we can figure out some of the values like, is it a JWT? If it's a JWT,
    // store the claims?
    public String authorization;

    // Parameters and cookies are stored in a List<ParsedHttpParameter> object.
    // We can figure out the type using the .type field. So we can extract
    // cookies and their values.

    // I still need to figure out how to store these in the DB so they can be
    // queries easily. We have a list of key/value pairs. We cannot make a
    // separate field for each. How do we do it then?

    // Needs preprocessing and I need to figure out how to store it in the DB.
    // To make it simple I might need to store these as key/value pairs for now.
    // But I still need to figure out how to store the key/value pairs in the DB
    // in a way that can be queries. E.g., give me all requests with a specific
    // cookie or header name or value.

    public List<ParsedHttpParameter> parameters; // needs processing.
    public List<HttpHeader> headers;

    // public List<Marker> markers; // figure out what this is and if it's useful.

    // Custom fields I am adding.

    // Populated from HttpService
    public String host;
    public int port;
    public boolean isHttps;

    // Authorization type.
    public String authorizationType;

    // Date
    public Instant date;

    // HighlightColor
    public String highlightColor;

    // Notes, is this the Burp comment?
    public String notes;

    // Tool source.
    public ToolType toolSource;

    // Constructor to populate the fields from a HttpRequest object.
    public Request(HttpRequest request, Annotations annotations, ToolType toolSource) {
        this.url = request.url();
        this.method = request.method();
        this.path = request.path();
        this.httpVersion = request.httpVersion();
        this.body = request.bodyToString();

        this.host = request.httpService().host();
        this.port = request.httpService().port();
        this.isHttps = request.httpService().secure();

        this.accept = Utils.getHeader("Accept", request.headers());
        this.contentType = Utils.getHeader("Content-Type", request.headers());
        this.userAgent = Utils.getHeader("User-Agent", request.headers());
        this.referer = Utils.getHeader("Referer", request.headers());
        this.origin = Utils.getHeader("Origin", request.headers());
        
        // If the content length is not present, set it to 0.
        String contentLengthHeader = Utils.getHeader("Content-Length", request.headers());
        this.contentLength = (contentLengthHeader != null) ? Integer.parseInt(contentLengthHeader) : 0;

        // Set annotations.
        this.highlightColor = annotations.highlightColor().toString();
        this.notes = annotations.notes();

        // Set tool source.
        this.toolSource = toolSource;

        String authorization = Utils.getHeader("Authorization", request.headers());
        if (authorization != null) {
            // Do something with authorization.
            if (authorization.startsWith("Bearer ")) {
                // It's a bearer token.
                this.authorizationType = "BEARER";
                // Extract the token.
                // String token = authorization.substring(7);
                // TODO: First figure out what type of token we have. E.g., JWT?
                // TODO: If JWT, do something with the claims
                // E.g., we can figure out if it's an AAD or not, if so, we can
                // store the type as AAD.
            }
        }

        // The rest of the headers. Again, same as above, we want to skip the
        // values, later, if needed.
        this.headers = request.headers();
    }

    public String getHeader(String name) {
        return Utils.getHeader(name, this.headers);
    }

}
