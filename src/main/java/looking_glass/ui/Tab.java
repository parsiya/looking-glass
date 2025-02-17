package looking_glass.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.util.List;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import burp.api.montoya.core.ToolType;
import burp.api.montoya.proxy.Proxy;
import burp.api.montoya.proxy.ProxyHttpRequestResponse;
import looking_glass.Constants;
import looking_glass.DB;
import looking_glass.Log;
import looking_glass.Request;
import looking_glass.Response;
import looking_glass.Utils;

public class Tab extends JSplitPane {

    private JList<String> queryList;
    private JScrollPane queryListScrollPane;
    private JTextArea queryTextArea;
    private JScrollPane queryTextAreaScrollPane;
    private JTable resultsTable;
    private JScrollPane resultsTableScrollPane;
    private JSplitPane resultsSplitPane;

    public Tab(int dividerLocation) {

        this.setOrientation(JSplitPane.HORIZONTAL_SPLIT);

        // ZZZ: Add some test data to querylist.
        String[] queries = { "Query 1", "Query 2", "Query 3" };
        queryList = new JList<String>(queries);
        queryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Add it to the scroll pane
        queryListScrollPane = new JScrollPane(queryList);

        // Create two buttons on top of each other.
        JButton runBtn = new JButton("Run");
        // Color of Burp Repeater's "Send" button.
        runBtn.setBackground(new Color(255, 102, 51));
        runBtn.setForeground(Color.WHITE);
        runBtn.setFont(runBtn.getFont().deriveFont(Font.BOLD));

        JButton saveBtn = new JButton("Save");

        JButton clearBtn = new JButton("Clear");

        JButton configBtn = new JButton("Config");
        configBtn.setFont(runBtn.getFont().deriveFont(Font.BOLD));
        configBtn.addActionListener(e -> selectDBFile());

        JButton proxybtn = new JButton("Store Proxy History");

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        buttonPanel.add(runBtn);
        buttonPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        buttonPanel.add(saveBtn);
        buttonPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        buttonPanel.add(clearBtn);
        buttonPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        buttonPanel.add(proxybtn);
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(configBtn);
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

        this.setLeftComponent(queryListScrollPane);
        this.setRightComponent(resultsSplitPane);

        // Apply the Burp them to the UI.
        Utils.api().userInterface().applyThemeToComponent(this);
    }

    // Opens a file chooser dialog to select a file.
    public static String selectDBFile() {
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
                // Is this thing on? Can you see the class?
                Class.forName("org.sqlite.JDBC");
                // Store the file path in the extension's configuration.
                Utils.api().persistence().preferences().setString(Constants.DB_PATH_KEY, file.getAbsolutePath());
                return file.getAbsolutePath();

            } catch (Exception e) {
                Log.toError("Error creating DB: " + e.getMessage());
                return null;
            }
        }
        return null;
    }

    public static void storeProxyHistory() {
        // Get the proxy history.
        Proxy proxy = Utils.api().proxy();
        List<ProxyHttpRequestResponse> history = proxy.history();

        // Go through the proxy history.
        for (ProxyHttpRequestResponse item : history) {
            Request req = new Request(item.finalRequest(), item.annotations(), ToolType.PROXY);
            Response res = new Response(item.originalResponse(), ToolType.PROXY);

            // Convert the request to JSON.
            String requestJson = Utils.toJson(req);
            String responseJson = Utils.toJson(res);

            // Store the request in the DB and get the primary key for it.

        }

        // ProxyHttpRequestResponse firstItem = history.get();
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
    }
}
