package looking_glass;


import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.logging.Logging;
import burp.api.montoya.proxy.*;
import burp.api.montoya.core.*;
import burp.api.montoya.http.*;
import burp.api.montoya.http.message.*;
import burp.api.montoya.http.message.params.ParsedHttpParameter;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;

import java.util.List;

public class Extension implements BurpExtension {
    @Override
    public void initialize(MontoyaApi api) {
        // set extension name
        api.extension().setName("Looking Glass");

        Logging logging = api.logging();

        // write a message to our output stream
        logging.logToOutput("Hello output.");

        // Get the proxy history.
        Proxy proxy = api.proxy();
        List<ProxyHttpRequestResponse> history = proxy.history();

        // Go through the proxy history.
        for (ProxyHttpRequestResponse item : history) {

            // What's in the ~~box~~ request?
            Request req = new Request(item.finalRequest(), item.annotations());
            Response res = new Response(item.originalResponse());



            // HttpService service = request.httpService();
            // String host = service.host();
            // int port = service.port();
            // boolean isHttps = service.secure();

            // String method = request.method();
            // String url = request.url();
            // String path = request.path();
            // String httpVersion = request.httpVersion();
            // List<HttpHeader> headers = request.headers(); // .name and .value
            // List<ParsedHttpParameter> parameters = request.parameters(); // .type, .name, .value, .location

            // // String or ByteArray for body. We can choose either, I am going with String.
            // String body = request.bodyToString();

            // // Does requests come with markers? Can extensions add markers to requests in history?
            // // I don't think so, but we need to experiment.
            // List<Marker> markers = request.markers();  // .range

            // // This is limited to a few in an enum recognized by Burp.
            // // NONE, UNKNOWN, AMF, JSON, MULTIPART, URL_ENCODED, XML
            // ContentType contentType = request.contentType();

            // // Optionally, we can store the entire request as a string with.
            // // String raw = request.toString();

            // // We can also dissect the response.
            // HttpResponse response = item.originalResponse();

            // short statusCode = response.statusCode();
            // String reasonPhrase = response.reasonPhrase();
            

        }

        // // write a message to our error stream
        // logging.logToError("Hello error.");

        // // write a message to the Burp alerts tab
        // logging.raiseInfoEvent("Hello info event.");
        // logging.raiseDebugEvent("Hello debug event.");
        // logging.raiseErrorEvent("Hello error event.");
        // logging.raiseCriticalEvent("Hello critical event.");

        // // throw an exception that will appear in our error stream
        // throw new RuntimeException("Hello exception.");

    }
}