package looking_glass.ui;

import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

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

public class Tab extends JSplitPane {

    private static final String RUN_BUTTON = "Run";
    private static final String SAVE_BUTTON = "Save";
    private static final String CLEAR_BUTTON = "Clear";
    private static final String DB_BUTTON = "Select DB";
    private static final String DB_BUTTON_TOOLTIP = "Configure the database connection, choose a new one, or stop logging.";
    private static final String PROXY_BUTTON_TEXT = "Import Proxy History";
    private static final String PROXY_BUTTON_TOOLTIP = "Import the proxy history into the database";
    private static final String SETTINGS_BUTTON = "Extension Settings";
    private static final String SETTINGS_BUTTON_TOOLTIP = "Looking Glass settings";
    private static final String CAPTURE_BUTTON_ON = "Capture On";
    private static final String CAPTURE_BUTTON_OFF = "Capture Off";

    private Sidebar querySidebar;
    private JTextArea queryTextArea;
    private JScrollPane queryTextAreaScrollPane;
    private JTable resultsTable;
    private JScrollPane resultsTableScrollPane;
    private JSplitPane resultsSplitPane;

    public Tab(int dividerLocation) {

        this.setOrientation(JSplitPane.HORIZONTAL_SPLIT);

        // Run button
        JButton runBtn = new JButton(RUN_BUTTON);
        // Color of Burp Repeater's "Send" button.
        runBtn.setBackground(new Color(255, 102, 51));
        runBtn.setForeground(Color.WHITE);
        runBtn.setFont(runBtn.getFont().deriveFont(Font.BOLD));
        runBtn.addActionListener(e -> {
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
                    // If connection is empty or if the connection is closed ask the user to select a database.
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
                
                // ZZZ: Is this needed? Copilot suggested it but I don't think it's needed.
                // Resize the columns to fit the content.
                for (int i = 0; i < metaData.getColumnCount(); i++) {
                    resultsTable.getColumnModel().getColumn(i).setPreferredWidth(100);
                }
                // Close the connection and the result set.
                conn.close();
                rs.close();
            } catch (Exception ex) {
                // ZZZ: Let's see if this is visible in the UI and useful.
                DefaultTableModel errorModel = new DefaultTableModel();
                errorModel.addColumn("Error");
                errorModel.addRow(new Object[]{"Error running the query: " + ex.getMessage()});
                resultsTable.setModel(errorModel);
                Log.toError("Error running the query: " + ex.getMessage());
            }
        });

        // Save button.
        JButton saveBtn = new JButton(SAVE_BUTTON);
        // saveBtn action is added later after the Sidebar is created.

        // Clear button.
        JButton clearBtn = new JButton(CLEAR_BUTTON);
        clearBtn.addActionListener(e -> queryTextArea.setText(""));

        JButton importProxyBtn = new JButton(PROXY_BUTTON_TEXT);
        importProxyBtn.setToolTipText(PROXY_BUTTON_TOOLTIP);
        importProxyBtn.setFont(importProxyBtn.getFont().deriveFont(Font.BOLD));
        importProxyBtn.addActionListener(e -> importProxyHistory());

        JButton captureBtn = new JButton(CAPTURE_BUTTON_OFF);
        captureBtn.setFont(captureBtn.getFont().deriveFont(Font.BOLD));
        // Draw the button based on the capture status at startup.
        paintCaptureBtn(captureBtn);
        captureBtn.addActionListener(e -> {
            // Toggle the capture status after button click.
            if (Utils.isCapturing()) {
                Utils.stopCapture();
            } else {
                Utils.startCapture();
            }
            paintCaptureBtn(captureBtn);
        });

        JButton settingsBtn = new JButton(SETTINGS_BUTTON);
        settingsBtn.setToolTipText(SETTINGS_BUTTON_TOOLTIP);
        settingsBtn.setFont(settingsBtn.getFont().deriveFont(Font.BOLD));
        settingsBtn.addActionListener(e -> {
            try {
                SettingsDialog myFrame = new SettingsDialog();
                myFrame.display();
            } catch (Exception exception) {
                Utils.msgBox("error", exception.getMessage());
            }
        });

        JButton dbBtn = new JButton(DB_BUTTON);
        dbBtn.setToolTipText(DB_BUTTON_TOOLTIP);
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

        // ZZZ: Read these from the config later.
        querySidebar = new Sidebar(queryTextArea);
        // List<Query> queries = Handler.getInstance().getQueries();
        // if (queries != null) {
        //     querySidebar.setQueries(queries);
        // }
        querySidebar.setPreferredSize(new Dimension(200, 0));
        // Add the action to the saveBtn because otherwise it will be null.
        saveBtn.addActionListener(e -> this.querySidebar.saveQueryDetails());

        // Set the sidebar to the left component of the tab.
        this.setLeftComponent(querySidebar);
        // Set the resultsSplitPane to the right component of the tab.
        this.setRightComponent(resultsSplitPane);

        // Apply the Burp theme to the UI.
        Utils.applyBurpStyle(this);
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
            btn.setText(CAPTURE_BUTTON_ON);
            btn.setToolTipText("Click to stop capture");
            btn.setBackground(new Color(38, 100, 157));
        } else {
            btn.setText(CAPTURE_BUTTON_OFF);
            btn.setToolTipText("Click to start capture");
            Utils.setBackground(btn);
        }
    }
}
