package looking_glass;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ToolType;
import burp.api.montoya.proxy.*;

import java.util.List;

public class Extension implements BurpExtension {

    @Override
    public void initialize(MontoyaApi api) {
        // set extension name
        api.extension().setName("Looking Glass");

        // Initialize the Utils class.
        Utils.initialize(api);

        Log.toOutput("Hello output.");

        // Add a tab to the Burp UI.
        api.userInterface().registerSuiteTab("Looking Glass", new UI(200));

        // Get the proxy history.
        Proxy proxy = api.proxy();
        List<ProxyHttpRequestResponse> history = proxy.history();

        ProxyHttpRequestResponse firstItem = history.get(16);

        if (firstItem != null) {
            Request req = new Request(firstItem.finalRequest(), firstItem.annotations(), ToolType.PROXY);
            // Convert the request to JSON.
            String requestJson = Utils.toJson(req);
            Log.toOutput(requestJson);
            
        }

        // // Go through the proxy history.
        // for (ProxyHttpRequestResponse item : history) {

        //     // What's in the ~~box~~ request?
        //     Request req = new Request(item.finalRequest(), item.annotations(), ToolType.PROXY);
        //     logging.logToOutput("HashCode for request:" + item.finalRequest().hashCode());

        //     // item.originalResponse() might be null of requests that did not get a
        //     // response.
        //     // Only process if it's not null.
        //     if (item.originalResponse() != null) {
        //         Response res = new Response(item.originalResponse(), ToolType.PROXY);
        //         logging.logToOutput("HashCode for response:" + item.originalResponse().hashCode());
        //     }
        // }

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