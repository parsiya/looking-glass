package looking_glass;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.Registration;

import looking_glass.ui.DBModal;
import looking_glass.ui.Tab;

import javax.swing.JOptionPane;

public class Extension implements BurpExtension {

    public Registration listener;

    @Override
    public void initialize(MontoyaApi api) {
        // set extension name
        api.extension().setName(Constants.EXTENSION_NAME);

        // Initialize the Utils class.
        Utils.initialize(api);

        // Add a tab to the Burp UI.
        api.userInterface().registerSuiteTab(Constants.EXTENSION_NAME, new Tab(200));

        // Read the DB Path from the extension's configuration.
        String dbPath = Utils.getDBPath();
        // If the DB path is null, set it to a default value for the message.
        if (dbPath == null) {
            dbPath = "No DB selected";
        } else {
            dbPath = "Current DB: " + dbPath;
        }
        Log.toOutput(dbPath);

        // Show the modal.
        int ret = DBModal.show(dbPath);

        // We will have three options in the result.
        switch (ret) {
            case JOptionPane.YES_OPTION:
                // Do nothing, we will continue using the DB.
                Log.toOutput("Using " + dbPath + ".");
                // Enable capture.
                Utils.setActiveCaptureStatus();
                break;
            case JOptionPane.NO_OPTION:
                // Select a DB file.
                String newDBFIle = Tab.selectDBFile();
                Log.toOutput("Using new DB: " + newDBFIle + ".");
                // Store the new DB path in the extension's configuration.
                Utils.setDBPath(newDBFIle);
                // Enable capture.
                Utils.setActiveCaptureStatus();
                break;
            case JOptionPane.CANCEL_OPTION:
                // Pause capture.
                // Store the capture status in the extension's configuration.
                Utils.setInactiveCaptureStatus();
                Log.toOutput("Pausing capture.");
                break;
        }

        // Check if capture is paused.
        String captureStatus = Utils.getCaptureStatus();

        // If capture is not paused, register the handler.
        if (captureStatus != null && captureStatus.equals("active")) {
            // Enable the HttpHandler.
            try {
                Handler httpHandler = Handler.getInstance();
                httpHandler.register(api.http().registerHttpHandler(httpHandler));
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