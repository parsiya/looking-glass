package looking_glass.message;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import burp.api.montoya.core.Annotations;
import burp.api.montoya.core.ToolType;
import burp.api.montoya.http.message.params.ParsedHttpParameter;
import burp.api.montoya.http.message.requests.HttpRequest;
import looking_glass.common.Utils;

public class Request {
    public String url, method, path, httpVersion, body;

    public List<Parameter> parameters;
    public List<Header> headers;
    public List<Cookie> cookies;

    // public List<Marker> markers; // figure out what this is and if it's useful.

    // Custom fields I am adding.

    // Populated from HttpService
    public String host;
    public int port;
    public boolean isHttps;

    // Authorization type.
    public String authorizationType;

    // HighlightColor
    public String highlightColor;

    // Notes, this is the Burp comments.
    public String notes;

    // Tool source.
    public String toolSource;

    // Extracted fields from headers.
    // These can be extracted from the headers before the insert query, but I am
    // processing and adding them here. This will be useful when we want to
    // store these objects in something like Cosmos DB or PostgreSQL that indexes
    // JSON fields.
    public String contentType, origin, referer;
    public int contentLength;
    // These are comma-separated (for now) names of parameters, names, and cookies.
    // This is useful in case we want to see IF a request has one of these or not.
    public String parameterNames, cookieNames, headerNames;

    // End of fields.

    // Constructor to populate the fields from a HttpRequest object.
    public Request(HttpRequest request, Annotations annotations, ToolType toolSource) {

        // Go through the headers and store them in a list of Header objects.
        List<Header> parsedHeaders = request.headers().stream()
                .map(header -> new Header(header.name(), header.value()))
                .collect(Collectors.toList());

        // Now we have to parse some headers specifically.

        // Content-Length might not exist for requests without a body, but we
        // will add a header with the value 0 if it does not.
        String contentLengthHeader = Utils.getHeader("Content-Length", parsedHeaders);
        if (contentLengthHeader == null) {
            parsedHeaders.add(new Header("Content-Length", "0"));
        }

        // Store parsed headers so we can use this.getHeader in the rest of the
        // method instead of Utils.getHeader and save a few precious storage bytes.
        this.headers = parsedHeaders;

        // Process the Authorization header.
        String authorization = this.getHeader("Authorization");
        if (authorization != null) {
            // Do something with authorization.
            if (authorization.startsWith("Bearer ")) {
                // It's a bearer token.
                // Extract the token.
                String token = authorization.substring(7);
                // Check if it's a JWT.
                if (token.chars().filter(ch -> ch == '.').count() == 2) {
                    this.authorizationType = "JWT";
                }
                // TODO: First figure out what type of token we have. E.g., JWT?
                // TODO: If JWT, do something with the claims
                // E.g., we can figure out if it's an AAD or not, if so, we can
                // store the type as AAD.
            }
        } else {
            // Add an empty authorization header.
            parsedHeaders.add(new Header("Authorization", ""));
        }

        this.url = request.url();
        this.method = request.method();
        this.httpVersion = request.httpVersion();
        this.body = request.bodyToString();

        this.host = request.httpService().host();
        this.port = request.httpService().port();
        this.isHttps = request.httpService().secure();

        // Path includes all the query string parameters here. Store everything
        // before the first `?`.
        this.path = request.path().split("\\?")[0];

        // Set annotations.
        this.highlightColor = annotations.highlightColor().toString();
        this.notes = annotations.notes();

        // Set tool source.
        this.toolSource = toolSource.toolName();

        // Parse the parameters.
        List<Parameter> parsedParameters = new ArrayList<Parameter>();
        List<Cookie> parsedCookies = new ArrayList<Cookie>();

        // Go through all request parameters and use a switch case statement to
        // store the COOKIEs in parsedCookies and the rest in parsedParameters.
        for (ParsedHttpParameter parameter : request.parameters()) {
            switch (parameter.type()) {
                case COOKIE:
                    parsedCookies.add(new Cookie(parameter.name(), parameter.value()));
                    break;
                default:
                    parsedParameters
                            .add(new Parameter(parameter.type().toString(), parameter.name(), parameter.value()));
                    break;
            }
        }

        // Populate the extracted fields from headers.
        this.contentType = this.getHeader("Content-Type");
        this.origin = this.getHeader("Origin");
        this.referer = this.getHeader("referer");
        
        // Content-Length has been set to 0 if it did not exist above, so we can
        // just parse it as int.
        this.contentLength = Integer.parseInt(this.getHeader("Content-Length"));

        // Create the comma-separated list of header/cookie/parameter names.
        this.headerNames = parsedHeaders.stream()
            .map(h -> h.name())
            .collect(Collectors.joining(","));

        this.cookieNames = parsedCookies.stream()
            .map(c -> c.name)
            .collect(Collectors.joining(","));

        this.parameterNames = parsedParameters.stream()
            .map(p -> p.name)
            .collect(Collectors.joining(","));

        // Postprocess any special parameters.

        // Add the cookies/parameters to the Request object.
        this.cookies = parsedCookies;
        this.parameters = parsedParameters;

    }

    public String getHeader(String name) {
        return Utils.getHeader(name, this.headers);
    }

}
