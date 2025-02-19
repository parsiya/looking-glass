package looking_glass;

import java.util.List;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.Registration;
import burp.api.montoya.proxy.ProxyHttpRequestResponse;
import burp.api.montoya.core.*;

import looking_glass.common.Constants;
import looking_glass.common.Log;
import looking_glass.common.Utils;
import looking_glass.message.Request;
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
    }
}