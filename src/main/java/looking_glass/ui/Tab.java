package looking_glass.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.util.List;
import javax.swing.*;

import burp.api.montoya.core.ToolType;
import burp.api.montoya.proxy.Proxy;
import burp.api.montoya.proxy.ProxyHttpRequestResponse;

import looking_glass.common.Constants;
import looking_glass.common.Log;
import looking_glass.common.Utils;
import looking_glass.message.Request;
import looking_glass.message.Response;

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
        configBtn.addActionListener(e -> DBModal.show());

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
