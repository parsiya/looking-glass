package looking_glass.ui;

import java.awt.*;
import java.io.File;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import burp.api.montoya.core.ToolType;
import burp.api.montoya.proxy.Proxy;
import burp.api.montoya.proxy.ProxyHttpRequestResponse;

import looking_glass.Handler;
import looking_glass.common.Log;
import looking_glass.common.Utils;
import looking_glass.db.DB;
import looking_glass.message.Request;
import looking_glass.message.Response;
import looking_glass.query.Query;

public class Tab extends JSplitPane {

    private static final String RUN_LABEL = "Run";
    private static final String RUN_TOOLTIP = "Run the query against the database";
    private static final String SAVE_LABEL = "Save";
    private static final String SAVE_TOOLTIP = "Save the query to the sidebar";
    private static final String CLEAR_LABEL = "Clear";
    private static final String CLEAR_TOOLTIP = "Clear the query text area";
    private static final String DB_LABEL = "Select DB";
    private static final String DB_TOOLTIP = "Configure the database connection, choose a new one, or stop logging";
    private static final String PROXY_LABEL = "Import Proxy History";
    private static final String PROXY_TOOLTIP = "Import the proxy history into the database";
    private static final String SETTINGS_LABEL = "Extension Settings";
    private static final String SETTINGS_TOOLTIP = "Looking Glass settings";
    private static final String CAPTURE_LABEL_ON = "Capture On";
    private static final String CAPTURE_LABEL_OFF = "Capture Off";
    private static final String CAPTURE_LABEL_RUNNING = "Click to stop capture";
    private static final String CAPTURE_LABEL_STOPPED = "Click to start capture";
    private static final String SAVE_QUERY_LABEL = "Save Queries";
    private static final String SAVE_QUERY_TOOLTIP = "Save the queries to a JSON file";
    private static final String LOAD_QUERY_LABEL = "Load Queries";
    private static final String LOAD_QUERY_TOOLTIP = "Load queries from a JSON file";

    private Sidebar sidebar;
    private JTextArea queryTextArea;
    private JScrollPane queryTextAreaScrollPane;
    private JTable resultsTable;
    private JScrollPane resultsTableScrollPane;
    private JSplitPane resultsSplitPane;

    public Tab(int dividerLocation) {

        this.setOrientation(JSplitPane.HORIZONTAL_SPLIT);

        // Run button
        JButton runBtn = new JButton(RUN_LABEL);
        runBtn.setToolTipText(RUN_TOOLTIP);
        // Color of Burp Repeater's "Send" button.
        runBtn.setBackground(new Color(255, 102, 51));
        runBtn.setForeground(Color.WHITE);
        runBtn.setFont(runBtn.getFont().deriveFont(Font.BOLD));
        runBtn.addActionListener(e -> runAction());

        // Save button.
        JButton saveBtn = new JButton(SAVE_LABEL);
        saveBtn.setToolTipText(SAVE_TOOLTIP);
        // saveBtn action is added later after the Sidebar is created.

        // Clear button.
        JButton clearBtn = new JButton(CLEAR_LABEL);
        clearBtn.setToolTipText(CLEAR_TOOLTIP);
        clearBtn.addActionListener(e -> queryTextArea.setText(""));

        JButton importProxyBtn = new JButton(PROXY_LABEL);
        importProxyBtn.setToolTipText(PROXY_TOOLTIP);
        importProxyBtn.setFont(importProxyBtn.getFont().deriveFont(Font.BOLD));
        importProxyBtn.addActionListener(e -> importProxyHistory());

        JButton captureBtn = new JButton(CAPTURE_LABEL_OFF);
        captureBtn.setFont(captureBtn.getFont().deriveFont(Font.BOLD));
        // Draw the button based on the capture status at startup.
        paintCaptureBtn(captureBtn);
        captureBtn.addActionListener(e -> {
            // Toggle the capture status after button click.
            if (Utils.isCapturing()) {
                Utils.stopCapture();
            } else {
                Utils.startCapture(false);
            }
            paintCaptureBtn(captureBtn);
        });

        JButton settingsBtn = new JButton(SETTINGS_LABEL);
        settingsBtn.setToolTipText(SETTINGS_TOOLTIP);
        settingsBtn.setFont(settingsBtn.getFont().deriveFont(Font.BOLD));
        settingsBtn.addActionListener(e -> {
            try {
                SettingsDialog myFrame = new SettingsDialog();
                myFrame.display();
            } catch (Exception exception) {
                Utils.msgBox("error", exception.getMessage());
            }
        });

        JButton dbBtn = new JButton(DB_LABEL);
        dbBtn.setToolTipText(DB_TOOLTIP);
        dbBtn.setFont(dbBtn.getFont().deriveFont(Font.BOLD));
        dbBtn.addActionListener(e -> DBModal.show());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        buttonPanel.add(runBtn);
        buttonPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        buttonPanel.add(saveBtn);
        buttonPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        buttonPanel.add(clearBtn);
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(importProxyBtn);
        buttonPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        buttonPanel.add(captureBtn);
        buttonPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        buttonPanel.add(settingsBtn);
        buttonPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        buttonPanel.add(dbBtn);
        buttonPanel.add(Box.createRigidArea(new Dimension(20, 0)));

        // Create the text area.
        queryTextArea = new JTextArea();
        queryTextArea.setLineWrap(true);
        queryTextArea.setWrapStyleWord(true);
        queryTextArea.setEditable(true);
        queryTextAreaScrollPane = new JScrollPane(queryTextArea);

        // Add the button panel to the bottom of the query text area.
        JPanel queryTextAreaWithButtonsPanel = new JPanel();
        queryTextAreaWithButtonsPanel.setLayout(new BoxLayout(queryTextAreaWithButtonsPanel, BoxLayout.Y_AXIS));
        queryTextAreaWithButtonsPanel.add(queryTextAreaScrollPane);
        queryTextAreaWithButtonsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        queryTextAreaWithButtonsPanel.add(buttonPanel);
        queryTextAreaWithButtonsPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        // Create the table.
        resultsTable = new JTable();
        resultsTableScrollPane = new JScrollPane(resultsTable);

        resultsSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        resultsSplitPane.setTopComponent(queryTextAreaWithButtonsPanel);
        resultsSplitPane.setBottomComponent(resultsTableScrollPane);
        resultsSplitPane.setDividerLocation(dividerLocation);

        // ========== Sidebar ==========

        sidebar = new Sidebar(queryTextArea);
        sidebar.setPreferredSize(new Dimension(200, 0));
        // Add the action to the saveBtn because otherwise it will be null.
        saveBtn.addActionListener(e -> this.sidebar.saveQueryDetails());

        JButton saveQueries = new JButton(SAVE_QUERY_LABEL);
        saveQueries.setToolTipText(SAVE_QUERY_TOOLTIP);
        saveQueries.addActionListener(e -> saveQueries());
        JButton loadQueries = new JButton(LOAD_QUERY_LABEL);
        loadQueries.setToolTipText(LOAD_QUERY_TOOLTIP);
        loadQueries.addActionListener(e -> loadQueries());
        JPanel querySidebar = new JPanel();
        querySidebar.setLayout(new BoxLayout(querySidebar, BoxLayout.Y_AXIS));

        querySidebar.add(saveQueries);
        querySidebar.add(Box.createRigidArea(new Dimension(0, 5)));
        querySidebar.add(loadQueries);
        querySidebar.add(Box.createRigidArea(new Dimension(0, 5)));
        querySidebar.add(sidebar);

        // Set the sidebar to the left component of the tab.
        this.setLeftComponent(querySidebar);
        // Set the resultsSplitPane to the right component of the tab.
        this.setRightComponent(resultsSplitPane);

        // Apply the Burp theme to the UI.
        Utils.applyBurpStyle(this);
    }

    // Run the query against the DB.
    private void runAction() {
        // Get the query from the text area.
        String query = queryTextArea.getText();
        // Check if the query is empty.
        if (query.isEmpty()) {
            Utils.msgBox("Error", "Query is empty.");
            return;
        }
        // We want to run this query against the connection in the handler.
        Handler handler = Handler.getInstance();
        if (handler.getConnection() == null) {
            // If connection is empty, ask the user to select a database.
            DBModal.show();
        }
        // If the handler's connection is still null, it means the user did not
        // choose a DB. Show and error and return.
        if (handler.getConnection() == null) {
            Utils.msgBox("Error", "Please choose a DB to run the query.");
            Log.toError("User did not choose a DB, the extension didn't run the query.");
            return;
        }
        // Run the query and get the results.
        Connection conn = handler.getConnection();
        try {
            // If the connection is null, select a DB.

            if (conn == null) {
                // If connection is empty or if the connection is closed ask the
                // user to select a database.
                DBModal.show();
            }

            // If the connection exists but is closed, reuse it in the DBPath.
            if (conn.isClosed()) {
                conn = DB.connect(Utils.getDBPath());
            }

            // Run the query.
            ResultSet rs = conn.createStatement().executeQuery(query);
            // Get the results metadata to be able to create the table.
            ResultSetMetaData metaData = rs.getMetaData();

            // Create a table model from the result's metadata.
            DefaultTableModel resultModel = new DefaultTableModel();
            // Add the columns.
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                resultModel.addColumn(metaData.getColumnName(i));
            }
            // Add the rows.
            while (rs.next()) {
                Object[] row = new Object[metaData.getColumnCount()];
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    row[i - 1] = rs.getObject(i);
                }
                resultModel.addRow(row);
            }

            // Set the model to the table.
            resultsTable.setModel(resultModel);

            // ZZZ: Is this needed? Copilot suggested it but I don't think it's
            // needed.
            // Resize the columns to fit the content.
            for (int i = 0; i < metaData.getColumnCount(); i++) {
                resultsTable.getColumnModel().getColumn(i).setPreferredWidth(100);
            }
            // Close the result set.
            rs.close();
            // If we close the connection here, it might mess with the handler
            // inserting data in to the DB if we're in the middle of a capture.
            // Right now, the handler will check if the connection is closed
            // right before the insert and will open it if not to account for
            // the situation where we're running queries in the middle of a
            // capture.
            conn.close();
        } catch (Exception ex) {
            DefaultTableModel errorModel = new DefaultTableModel();
            errorModel.addColumn("Error");
            errorModel.addRow(new Object[] { "Error running the query: " + ex.getMessage() });
            resultsTable.setModel(errorModel);
            Log.toError("Error running the query: " + ex.getMessage());
        }
    }

    // Import proxy history into the DB.
    private static void importProxyHistory() {
        // Get the proxy history.
        Proxy proxy = Utils.api().proxy();
        List<ProxyHttpRequestResponse> history = proxy.history();

        // Return if history is empty.
        if (history.isEmpty()) {
            // Show a message box.
            Utils.msgBox("Error", "Proxy history is empty.");
            Log.toOutput("Proxy history is empty.");
            return;
        }

        // Check if a database is selected or not.
        Handler handler = Handler.getInstance();
        if (handler.getConnection() == null) {
            // If connection is empty, ask the user to select a database.
            DBModal.show();
        }

        // If the handler's connection is still null, it means the user did not
        // choose a DB. Show and error and return.
        if (handler.getConnection() == null) {
            Utils.msgBox("Error", "Please choose a DB to import the proxy history.");
            Log.toError("User did not choose a DB, the extension didn't import the proxy history.");
            return;
        }

        Log.toOutput("Storing the proxy history in the DB.");

        // Go through the proxy history.
        IntStream.range(0, history.size()).forEach(i -> {
            try {
                ProxyHttpRequestResponse item = history.get(i);
                Request req = new Request(item.finalRequest(), item.annotations(), ToolType.PROXY);
                Response res = new Response(item.originalResponse(), ToolType.PROXY);

                // Store the request and responses in the DB.
                int reqId = DB.insertRequest(req, handler.getConnection());
                DB.insertResponse(res, handler.getConnection(), reqId);
            } catch (Exception e) {
                String errorResult = String.format("Stored %d pairs in the DB before an error.\n" + e.getMessage(), i);
                Utils.msgBox("Error", errorResult);
                Log.toError(errorResult);
                return;
            }
        });
        String importResult = String.format("Proxy History successfully imported. Stored %d pairs in the DB.",
                history.size());
        Utils.msgBox("Success", importResult);
        Log.toOutput(importResult);
    }

    // Change the color of the capture button based on the capture status.
    private static void paintCaptureBtn(JButton btn) {
        if (Utils.isCapturing()) {
            btn.setText(CAPTURE_LABEL_ON);
            btn.setToolTipText(CAPTURE_LABEL_RUNNING);
            btn.setBackground(new Color(38, 100, 157));
        } else {
            btn.setText(CAPTURE_LABEL_OFF);
            btn.setToolTipText(CAPTURE_LABEL_STOPPED);
            Utils.setBackground(btn);
        }
    }

    // Save the queries to a JSON file.
    private void saveQueries() {
        // Save the queries to a JSON file.
        List<Query> qq = this.sidebar.getQueries();

        if (qq == null || qq.isEmpty()) {
            Utils.msgBox("Error", "No queries to save.");
            return;
        }
        // Convert the queries to JSON.
        String json = "";
        try {
            json = Utils.toJson(qq);
        } catch (Exception e) {
            Utils.msgBox("Error", "Error converting queries to JSON: " + e.getMessage());
            return;
        }
        // Open a file chooser to save the JSON file.
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Queries");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("JSON files", "json"));
        int returnValue = fileChooser.showSaveDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            // Get the selected file.
            File selectedFile = fileChooser.getSelectedFile();
            // Check if the file already exists.
            if (selectedFile.exists()) {
                int overwrite = JOptionPane.showConfirmDialog(null,
                        "File already exists. Do you want to overwrite it?", "Overwrite file",
                        JOptionPane.YES_NO_OPTION);
                if (overwrite != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            // Save the JSON to the file.
            try {
                Files.writeString(selectedFile.toPath(), json);
            } catch (Exception e) {
                Utils.msgBox("Error", "Error saving queries: " + e.getMessage());
            }
        }
    }

    // Load the queries from a JSON file.
    private void loadQueries() {
        // Load the queries from a JSON file.
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Load Queries");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("JSON files", "json"));
        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            // Get the selected file.
            File selectedFile = fileChooser.getSelectedFile();
            // Read the JSON from the file.
            String json = "";
            try {
                json = Files.readString(selectedFile.toPath());
            } catch (Exception e) {
                Utils.msgBox("Error", "Error reading queries: " + e.getMessage());
                return;
            }
            // Convert the JSON to a list of queries.
            List<Query> queries = new ArrayList<>();
            try {
                Type type = new TypeToken<List<Query>>() {
                }.getType();
                queries = Utils.fromJson(json, type);
                // Set the queries in the sidebar.
                this.sidebar.setQueries(queries);
            } catch (Exception e) {
                Utils.msgBox("Error", "Error loading queries: " + e.getMessage());
                return;
            }
        }
    }
}
