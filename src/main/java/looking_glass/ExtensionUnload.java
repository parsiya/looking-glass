package looking_glass;

import burp.api.montoya.extension.ExtensionUnloadingHandler;

import looking_glass.common.Log;

// Handles the unloading of the extension.
public class ExtensionUnload implements ExtensionUnloadingHandler {

    @Override
    public void extensionUnloaded() {
        Log.toOutput("Unloading the extension.");
        try {
            Handler handler = Handler.getInstance();
            if (handler.isRegistered()) {
                handler.deregister();
            }
            handler.closeConnection();
        } catch (Exception e) {
            Log.toError("Error unloading the extension: " + e.getMessage());
        }
    }
}
