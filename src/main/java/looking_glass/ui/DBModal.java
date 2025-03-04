package looking_glass.ui;

import java.awt.Component;
import java.io.File;
import java.sql.Connection;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import looking_glass.Handler;
import looking_glass.common.Constants;
import looking_glass.common.Log;
import looking_glass.common.Utils;
import looking_glass.db.DB;

public class DBModal {

    // Show the modal with two options.
    // "Choose a new one" and "Pause capture".
    private static void twoOptions(String message) {
        // Take the last two button names.
        String[] options = new String[] { Constants.DB_MODAL_OPTIONS[1], Constants.DB_MODAL_OPTIONS[2] };

        // Get burp frame, we will set it as a the parent of the modal.
        Component burpFrame = Utils.burpFrame();

        // Show the modal.
        int ret = JOptionPane.showOptionDialog(
                burpFrame, // Parent component
                message, // Message
                "Choose a new DB", // Title
                JOptionPane.YES_NO_OPTION, // Option type
                JOptionPane.PLAIN_MESSAGE, // Message type
                null, // Icon
                options, // Options
                JOptionPane.NO_OPTION // Initial value is "pause logging"
        );

        // Process the options.
        switch (ret) {
            // "Choose a new file"
            case JOptionPane.YES_OPTION:
                chooseNewDB();
                break;
            // "Pause capture"
            case JOptionPane.NO_OPTION:
                // Also pause capture if the user closes the dialog.
            default:
                pauseCapture();
        }
    }

    // Show the modal with three options.
    // "Use the file", "Choose a new one", and "Pause capture".
    private static void threeOptions(String message) {
        // Get burp frame, we will set it as a the parent of the modal.
        final Component burpFrame = Utils.burpFrame();

        // Show the modal.
        int ret = JOptionPane.showOptionDialog(
                burpFrame, // Parent component
                message, // Message
                "DB Options", // Title
                JOptionPane.YES_NO_CANCEL_OPTION, // Option type
                JOptionPane.PLAIN_MESSAGE, // Message type
                null, // Icon
                Constants.DB_MODAL_OPTIONS, // Options
                JOptionPane.NO_OPTION // Initial value is "pause logging"
        );

        // Process the options.
        switch (ret) {
            // "Use the file"
            case JOptionPane.YES_OPTION:
                // Do nothing, use the same file.
                Log.toOutput("Keep using the current DB: " + Utils.getDBPath());
                break;
            // "Choose a new one"
            case JOptionPane.NO_OPTION:
                chooseNewDB();
                break;
            // "Pause capture"
            case JOptionPane.CANCEL_OPTION:
                // Also pause capture if the user closes the dialog.
            default:
                pauseCapture();
        }
    }

    public static void show() {
        // Read the DB Path from the extension's settings.
        String dbPath = Utils.getDBPath();

        // If dbPath is null, show a message to choose a new DB.
        if (dbPath == null) {
            twoOptions("No DB selected.\nChoose a new one:");
        } else {
            // Try to connect to the DB.
            Log.toOutput("Checking the DB connection to: " + dbPath);
            try {
                Connection conn = DB.connect(dbPath);
                conn.close(); // Close the connection, we will make it again later.
                threeOptions("Current DB: " + dbPath);
            } catch (Exception e) {
                Log.toError(e.getMessage());
                twoOptions("The DB connection to: " + dbPath + " failed.\nChoose a new one:");
            }
        }

        // Now that we've chosen a DB, read it from the extension's settings.
        dbPath = Utils.getDBPath();

        // If dbPath is still null, we've paused capture, do not create a connection.
        if (dbPath == null) {
            return;
        }
        // Create a connection to it and store it in the Handler.
        try {
            Connection connection = DB.connect(dbPath);
            Handler.getInstance().setConnection(connection);
        } catch (Exception e) {
            Log.toError("Error connecting to DB: " + e.getMessage());
        }
    }

    // Use a new DB file.
    private static void chooseNewDB() {

        String newDB = selectDBFile();
        // Keep showing the file chooser dialog until a valid file is selected or the
        // dialog is canceled.
        while (newDB == null) {
            newDB = selectDBFile();
        }

        // ZZZ what happens here if we cancel the dialog. Will the result be empty?
        Log.toOutput("Using new DB: " + newDB + ".");
        // Store the new DB path in the extension's settings.
        Utils.setDBPath(newDB);
        // Enable capture.
        Utils.setActiveCaptureStatus();
    }

    private static void pauseCapture() {
        // Store the capture status in the extension's settings.
        Utils.setInactiveCaptureStatus();
        Log.toOutput("Capture paused.");
    }

    // Opens a file chooser dialog to select a file.
    private static String selectDBFile() {
        // Create a file chooser and set the file extension filter.
        DBFileChooser fileChooser = new DBFileChooser();

        // Show the file chooser dialog
        int returnVal = fileChooser.show(null);

        // Check if a file was selected
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            // Get the selected file
            File file = fileChooser.getSelectedFile();

            // If the file doesn't exist, get the path.
            if (!file.exists()) {
                Log.toOutput("DB doesn't exist, creating a new DB at: " + file.getAbsolutePath());
            }
            try {
                // Store the file path in the extension's settings.
                Utils.setDBPath(file.getAbsolutePath());
                return file.getAbsolutePath();

            } catch (Exception e) {
                Log.toError("Error creating DB: " + e.getMessage());
                return null;
            }
        }
        return null;
    }
}