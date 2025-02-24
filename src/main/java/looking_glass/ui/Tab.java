package looking_glass.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.util.List;
import java.util.stream.IntStream;

import javax.swing.*;

import burp.api.montoya.core.ToolType;
import burp.api.montoya.proxy.Proxy;
import burp.api.montoya.proxy.ProxyHttpRequestResponse;
import looking_glass.Handler;
import looking_glass.common.Constants;
import looking_glass.common.Log;
import looking_glass.common.Utils;
import looking_glass.db.DB;
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
        clearBtn.addActionListener(e -> test1());

        JButton configBtn = new JButton("DB Config");
        configBtn.setToolTipText("Configure the database connection, choose a new one, or stop logging.");
        configBtn.setFont(runBtn.getFont().deriveFont(Font.BOLD));
        configBtn.addActionListener(e -> DBModal.show());

        JButton proxyBtn = new JButton("Import Proxy History");
        proxyBtn.setToolTipText("Import the proxy history into the database");
        proxyBtn.addActionListener(e -> storeProxyHistory());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        buttonPanel.add(runBtn);
        buttonPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        buttonPanel.add(saveBtn);
        buttonPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        buttonPanel.add(clearBtn);
        buttonPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        buttonPanel.add(proxyBtn);
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
        Utils.applyBurpStyle(this);
    }

    public static void storeProxyHistory() {
        // Get the proxy history.
        Proxy proxy = Utils.api().proxy();
        List<ProxyHttpRequestResponse> history = proxy.history();

        // Return if history is empty.
        if (history.isEmpty()) {
            // Show a message box.
            Utils.msgBox(Utils.burpFrame(), "Proxy history is empty.", "Error");
            Log.toOutput("Proxy history is empty.");
            return;
        }

        Log.toOutput("Storing the proxy history in the DB.");

        // Go through the proxy history.
        IntStream.range(0, history.size()).forEach(i -> {
            ProxyHttpRequestResponse item = history.get(i);
            Request req = new Request(item.finalRequest(), item.annotations(), ToolType.PROXY);
            Response res = new Response(item.originalResponse(), ToolType.PROXY);

            // Store the request and responses in the DB.

            // Get an instance of Handler, which has the DB connection.
            Handler handler = Handler.getInstance();
            // Now the connection might not be established. If so, we show the
            // DB modal so the user can choose a DB and make a connection.
            while (handler.getConnection() == null) {
                DBModal.show();
            }
            // Hopefully, the user has finally chosen a DB and the connection is
            // populated.
            try {
                int reqId = DB.insertRequest(req, handler.getConnection());
                DB.insertResponse(res, handler.getConnection(), reqId);
            } catch (Exception e) {
                Log.toError(e.getMessage());
                Log.toError(String.format("Stored %d pairs in the DB before the error.", i));
                return;
            }
        });
        Log.toOutput(String.format("Proxy History successfully imported. Stored %d pairs in the DB.", history.size()));
    }

    public static void test1() {
        ConfigFrame myFrame = new ConfigFrame();
        myFrame.setVisible(true);
    }
}
