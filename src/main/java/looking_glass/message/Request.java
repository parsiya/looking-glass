package looking_glass.message;

import java.time.Instant;
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

    public List<Parameter> parameters; // needs processing.
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

        // Process the Authorization header.
        String authorization = Utils.getHeader("Authorization", parsedHeaders);
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
        this.path = request.path();
        this.httpVersion = request.httpVersion();
        this.body = request.bodyToString();

        this.host = request.httpService().host();
        this.port = request.httpService().port();
        this.isHttps = request.httpService().secure();

        // // If the content length is not present, set it to 0.
        // String contentLengthHeader = Utils.getHeader("Content-Length",
        // request.headers());
        // this.contentLength = (contentLengthHeader != null) ?
        // Integer.parseInt(contentLengthHeader) : 0;

        // Set annotations.
        this.highlightColor = annotations.highlightColor().toString();
        this.notes = annotations.notes();

        // Set tool source.
        this.toolSource = toolSource;

        
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
                    parsedParameters.add(new Parameter(parameter.type().toString(), parameter.name(), parameter.value()));
                    break;
            }
        }

        // Postprocess any special parameters.

        // Process cookies.
        // Go through the parsedParameters and find the cookies.

        // Add everything to the Request object.
        this.headers = parsedHeaders;
        this.cookies = parsedCookies;
        this.parameters = parsedParameters;

    }

    public String getHeader(String name) {
        return Utils.getHeader(name, this.headers);
    }

}
