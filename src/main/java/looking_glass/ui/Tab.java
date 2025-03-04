package looking_glass.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import java.util.List;
import java.util.stream.IntStream;

import javax.swing.*;

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
        JButton runBtn = new JButton(RUN_BUTTON);
        // Color of Burp Repeater's "Send" button.
        runBtn.setBackground(new Color(255, 102, 51));
        runBtn.setForeground(Color.WHITE);
        runBtn.setFont(runBtn.getFont().deriveFont(Font.BOLD));

        JButton saveBtn = new JButton(SAVE_BUTTON);

        JButton clearBtn = new JButton(CLEAR_BUTTON);
        clearBtn.addActionListener(e -> clearBtnAction());

        JButton importProxyBtn = new JButton(PROXY_BUTTON_TEXT);
        importProxyBtn.setToolTipText(PROXY_BUTTON_TOOLTIP);
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
            SettingsDialog myFrame = new SettingsDialog();
            myFrame.display();
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

        this.setLeftComponent(queryListScrollPane);
        this.setRightComponent(resultsSplitPane);

        // Apply the Burp them to the UI.
        Utils.applyBurpStyle(this);
    }

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


    private static void paintCaptureBtn(JButton btn) {
        if (Utils.isCapturing()) {
            btn.setText(CAPTURE_BUTTON_ON);
            btn.setToolTipText("Click to stop capture");
            btn.setBackground(new Color(38, 100, 157));
        } else {
            btn.setText(CAPTURE_BUTTON_OFF);
            btn.setToolTipText("Click to start capture");
            // Utils.applyBurpStyle(btn); // This doesn't work for to change the background.
            Utils.setBackground(btn);
        }
    }

    public static void clearBtnAction() {
        // ZZZ: TODO
        // Do nothing for now.
    }
}
