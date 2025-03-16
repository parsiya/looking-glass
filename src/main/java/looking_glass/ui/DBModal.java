package looking_glass.ui;

import java.io.File;
import java.sql.Connection;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import looking_glass.Handler;
import looking_glass.common.Log;
import looking_glass.common.Utils;
import looking_glass.db.DB;

public class DBModal {

    // Let users choose a new DB.
    // "Choose DB": JFileChooser to choose a new DB.
    // "Cancel" or closing the dialog: Pause capture.
    private static void newDBDialog(String message) {
        // Show the modal.
        int ret = JOptionPane.showOptionDialog(
                Utils.burpFrame(), // Parent component
                message, // Message
                "Choose DB", // Title
                JOptionPane.YES_NO_OPTION, // Option type
                JOptionPane.PLAIN_MESSAGE, // Message type
                null, // Icon
                new String[] { "Choose a New DB", "Cancel & Pause Capture" }, // Options
                JOptionPane.NO_OPTION // Initial value is "cancel"
        );
        // Process the options.
        switch (ret) {
            // "Choose a new DB"
            case JOptionPane.YES_OPTION:
                selectDBFile();
                break;
            // Do nothing if user clicks cancel or closes the window.
            case JOptionPane.NO_OPTION:
            default:
                Log.toOutput("User did not choose a new DB. Pausing capture.");
                Utils.setCaptureStatus(false);
        }
    }

    // Let users choose a new DB or keep using the current one.
    // "Choose a new DB": JFileChooser to choose a new DB.
    // "Cancel" or closing the dialog: Keep using the current one.
    private static void chooseDBDialog() {
        // Show the modal.
        int ret = JOptionPane.showOptionDialog(
                Utils.burpFrame(), // Parent component
                "Current DB: " + Utils.getDBPath(),
                "Choose DB",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                new String[] { "Choose a New DB", "Keep Current DB" }, // Options
                JOptionPane.NO_OPTION // Initial value is "cancel"
        );
        // Process the options.
        switch (ret) {
            // "Choose a new DB"
            case JOptionPane.YES_OPTION:
                selectDBFile();
                break;
            // Do nothing if user clicks cancel or closes the window.
            case JOptionPane.NO_OPTION:
            default:
        }
    }

    public static void show() {
        // Read the DB Path from the extension's settings.
        String dbPath = Utils.getDBPath();

        // If dbPath is null, show a message to choose a new DB.
        if (dbPath == null) {
            Log.toOutput("DB path in extension settings is empty, asking user to choose a new one.");
            newDBDialog("No DB selected.");
            // It's OK if users do not choose a DB here, we will check when we they enable
            // capture.
        } else {
            // Try to connect to the DB.
            Log.toOutput("Checking the DB connection to: " + dbPath);
            try {
                Connection conn = DB.connect(dbPath);
                conn.close(); // Close the connection, we will make it again later.
                chooseDBDialog(); // Allow the user to choose a new one if wanted.
            } catch (Exception e) {
                // If the current connection doesn't exist.
                Utils.msgBox("Error", "DB Connection failed, choose a new one\n" + e.getMessage());
                newDBDialog("DB Connection failed, choose a new one.\n" + e.getMessage());
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
            Log.toOutput("Connected to the database at: " + dbPath);
            Connection connection = DB.connect(dbPath);
            Handler.getInstance().setConnection(connection);
        } catch (Exception e) {
            Log.toError("Error connecting to DB: " + e.getMessage());
        }
    }

    // Opens a file chooser dialog to select a file.
    private static void selectDBFile() {
        // Create a file chooser and set the file extension filter.
        DBFileChooser fileChooser = new DBFileChooser();

        // Show the file chooser dialog.
        int returnVal = fileChooser.show(Utils.burpFrame());

        // Check if a file was selected.
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            // Get the selected file.
            File file = fileChooser.getSelectedFile();

            // If the file doesn't exist, get the path.
            if (!file.exists()) {
                Log.toOutput("DB doesn't exist, creating a new DB at: " + file.getAbsolutePath());
            }
            try {
                // Store the file path in the extension's settings.
                Utils.setDBPath(file.getAbsolutePath());
                final String NEW_DB_CREATED_MSG = "Using the DB at: ";
                Utils.msgBox("", NEW_DB_CREATED_MSG + file.getAbsolutePath());
                Log.toOutput(NEW_DB_CREATED_MSG + file.getAbsolutePath());
                // Set capture status to active.
                Utils.setCaptureStatus(true);
            } catch (Exception e) {
                Utils.msgBox("Error", "Error creating DB.\n" + e.getMessage());
                Log.toError("Error creating DB: " + e.getMessage());
            }
        }
    }
}