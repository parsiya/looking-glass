package looking_glass;

import burp.api.montoya.core.ToolType;
import burp.api.montoya.http.handler.*;

// Handler class to handle the requests and responses.

public class Handler implements HttpHandler {

    // What happens to each request immediately before it's sent out.
    @Override
    public RequestToBeSentAction handleHttpRequestToBeSent(HttpRequestToBeSent request) {

        // For now we will ignore the outgoing requests and only focus on
        // requests that have a response.
        // Request req = new Request(request, request.annotations(), request.toolSource().toolType());

        // // Do we trust Burp API's hashcode to generate a unique number for each
        // // request or response?

        // Just send the request as is.
        return RequestToBeSentAction.continueWith(request);
    }

    // What happens to the response after it's received.
    @Override
    public ResponseReceivedAction handleHttpResponseReceived(HttpResponseReceived response) {
        // Implement your logic here
        ToolType toolType = response.toolSource().toolType();

        Response res = new Response(response, toolType);
        Request req = new Request(response.initiatingRequest(), response.annotations(), toolType);

        

        // System.out.println("Handling HTTP response received: " + response.);
        return ResponseReceivedAction.continueWith(response); // Modify and return the response if needed
    }

    
}
