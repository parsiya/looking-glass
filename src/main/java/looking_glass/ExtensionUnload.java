package looking_glass;

import burp.api.montoya.extension.ExtensionUnloadingHandler;
import looking_glass.common.Log;
import looking_glass.common.Utils;

// Handles the unloading of the extension.
// It should deregister the handler and close the DB connection.
public class ExtensionUnload implements ExtensionUnloadingHandler {

    @Override
    public void extensionUnloaded() {
        Log.toOutput("Unloading the extension.");
        try {
            Utils.stopCapture();
        } catch (Exception e) {
            Log.toError("Error unloading the extension: " + e.getMessage());
        }
    }
}
