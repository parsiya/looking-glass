package looking_glass.ui;

import java.awt.Component;
import javax.swing.JOptionPane;

import looking_glass.Constants;
import looking_glass.Utils;

public class DBModal {

    public static int show(String message) {
        // Get burp frame.
        Component burpFrame = Utils.api().userInterface().swingUtils().suiteFrame();
        int ret = JOptionPane.showOptionDialog(
                burpFrame,                          // Parent component
                message,                            // Message
                null,                         // Title
                JOptionPane.YES_NO_CANCEL_OPTION,   // Option type
                JOptionPane.PLAIN_MESSAGE,          // Message type
                null,                          // Icon 
                Constants.DB_MODAL_OPTIONS,         // Options
                null                   // Initial value
        );
        return ret;
    }
}