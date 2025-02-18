package looking_glass;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.Registration;

import looking_glass.common.Constants;
import looking_glass.common.Log;
import looking_glass.common.Utils;
import looking_glass.ui.DBModal;
import looking_glass.ui.Tab;

public class ExtensionMain implements BurpExtension {

    public Registration listener;

    @Override
    public void initialize(MontoyaApi api) {
        // Set the extension name
        api.extension().setName(Constants.EXTENSION_NAME);

        // Register the extension unload handler.
        api.extension().registerUnloadingHandler(new ExtensionUnload());

        // Initialize the Utils class.
        Utils.initialize(api);

        // Add a tab to the Burp UI.
        api.userInterface().registerSuiteTab(Constants.EXTENSION_NAME, new Tab(200));

        // Show the modal to choose a DB.
        DBModal.show();
        
        // ZZZ remove this - set capture status to active for now.
        // Utils.setActiveCaptureStatus();

        // If capture is not paused, register the handler.
        if (Utils.isCapturing()) {
            // Enable the HttpHandler.
            try {
                Handler httpHandler = Handler.getInstance();
                httpHandler.register(api.http().registerHttpHandler(httpHandler));
                Log.toOutput("Registered the handler.");
            } catch (Exception e) {
                Log.toError("Error registering handler: " + e.getMessage());
            }
        }

        //

        // if (firstItem != null) {
        // Request req = new Request(firstItem.finalRequest(), firstItem.annotations(),
        // ToolType.PROXY);
        // // Convert the request to JSON.
        // String requestJson = Utils.toJson(req);
        // Log.toOutput(requestJson);

        // }

        // // Go through the proxy history.
        // for (ProxyHttpRequestResponse item : history) {

        // // What's in the ~~box~~ request?
        // Request req = new Request(item.finalRequest(), item.annotations(),
        // ToolType.PROXY);
        // logging.logToOutput("HashCode for request:" +
        // item.finalRequest().hashCode());

        // // item.originalResponse() might be null of requests that did not get a
        // // response.
        // // Only process if it's not null.
        // if (item.originalResponse() != null) {
        // Response res = new Response(item.originalResponse(), ToolType.PROXY);
        // logging.logToOutput("HashCode for response:" +
        // item.originalResponse().hashCode());
        // }
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