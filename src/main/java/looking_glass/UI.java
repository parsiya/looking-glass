package looking_glass;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class UI extends JSplitPane {

    private JList<String> queryList;
    private JScrollPane queryListScrollPane;
    private JTextArea queryTextArea;
    private JScrollPane queryTextAreaScrollPane;
    private JTable resultsTable;
    private JScrollPane resultsTableScrollPane;
    private JSplitPane resultsSplitPane;

    public UI(int dividerLocation) {

        this.setOrientation(JSplitPane.HORIZONTAL_SPLIT);

        // Add some test data to querylist.
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

        JButton button3 = new JButton("Clear");

        JButton configBtn = new JButton("Config");
        configBtn.setFont(runBtn.getFont().deriveFont(Font.BOLD));
        configBtn.addActionListener(e -> selectDBFile());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(runBtn);
        buttonPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        buttonPanel.add(saveBtn);
        buttonPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        buttonPanel.add(button3);
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(configBtn);

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
    public static File selectDBFile() {
        // Create a file chooser and set the file extension filter.
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("DB files", "db");
        fileChooser.setFileFilter(filter);

        // Show the file chooser dialog
        int returnVal = fileChooser.showSaveDialog(null);

        // Check if a file was selected
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            // Get the selected file
            File file = fileChooser.getSelectedFile();

            // If the file doesn't exist, get the path.
            if (!file.exists()) {
                Log.toOutput("DB doesn't exist, creating a new DB.");
            }
            try {
                // Is this thing on? Can you see the class?
                Class.forName("org.sqlite.JDBC");
                DB.connect(file.getAbsolutePath());
            } catch (Exception e) {
                Log.toError("Error creating DB: " + e.getMessage());
                return null;
            }
        }

        return null;
    }
}
