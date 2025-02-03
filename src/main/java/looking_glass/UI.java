package looking_glass;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.*;

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
        JButton button1 = new JButton("Run");
        button1.setBackground(new Color(255, 102, 51));
        button1.setForeground(Color.WHITE);
        button1.setFont(button1.getFont().deriveFont(Font.BOLD));

        JButton button2 = new JButton("Save");
        button2.setFont(button1.getFont().deriveFont(Font.BOLD));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(button1);
        buttonPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        buttonPanel.add(button2);
        buttonPanel.add(Box.createHorizontalGlue());

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
    }

}
