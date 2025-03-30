package looking_glass;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.Registration;

import looking_glass.common.Constants;
import looking_glass.common.Utils;
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

        Handler handler = Handler.getInstance();
        if (handler.getSettings().captureOnStartup) {
            Utils.startCapture(handler.getSettings().captureOnStartup);
        }

        // Add a tab to the Burp UI.
        api.userInterface().registerSuiteTab(Constants.EXTENSION_NAME, new Tab(200));
    }
}