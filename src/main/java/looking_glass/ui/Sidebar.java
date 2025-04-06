package looking_glass.ui;

import javax.swing.*;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.ArrayList;
import java.util.List;

import looking_glass.Handler;
import looking_glass.common.Utils;
import looking_glass.query.Query;

public class Sidebar extends JScrollPane {
    private DefaultListModel<Query> queryListModel;
    private JList<Query> queryList;
    private JTextArea queryDetailsArea;

    // The JTextArea is used to show the details of the selected query.
    public Sidebar(JTextArea textArea) {
        this.queryDetailsArea = textArea;

        // Get the queries from the handler.
        this.queryListModel = Handler.getInstance().getQueries();
        if (this.queryListModel == null) {
            this.queryListModel = new DefaultListModel<>();
        }

        // Add a listener to detect changes in the query list model.
        this.queryListModel.addListDataListener(new javax.swing.event.ListDataListener() {
            // Commenting for future use.
            // The internalAdded and intervalRemoved methods are correctly called when
            // new items are added or removed. The contentsChanged method is not called when
            // an item that exists is modified.
            @Override
            public void intervalAdded(javax.swing.event.ListDataEvent e) {
                Handler.getInstance().setQueries(queryListModel);
            }

            @Override
            public void intervalRemoved(javax.swing.event.ListDataEvent e) {
                Handler.getInstance().setQueries(queryListModel);
            }

            // This is not automatically called when an item is modified.
            @Override
            public void contentsChanged(javax.swing.event.ListDataEvent e) {
                Handler.getInstance().setQueries(queryListModel);
            }
        });

        this.queryList = new JList<Query>(queryListModel);
        this.setViewportView(this.queryList);

        // ==================== Popup menu ====================
        JPopupMenu popupMenu = new JPopupMenu();

        // ==================== New Query =====================
        JMenuItem newQueryItem = new JMenuItem("New Query");
        newQueryItem.addActionListener(e -> this.newQuery());
        popupMenu.add(newQueryItem);

        // ==================== Rename Query =====================
        JMenuItem editQueryItem = new JMenuItem("Rename Query");
        editQueryItem.setEnabled(false);
        editQueryItem.addActionListener(e -> {
            this.editQuery();
        });
        popupMenu.add(editQueryItem);

        // Enable/disable `Rename Query` when a query is selected.
        this.queryList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                editQueryItem.setEnabled(this.queryList.getSelectedValue() != null);
            }
        });

        // ==================== Delete Query =====================

        JMenuItem deleteQueryItem = new JMenuItem("Delete Query");
        deleteQueryItem.setEnabled(false);
        popupMenu.add(deleteQueryItem);
        deleteQueryItem.addActionListener(e -> {
            this.deleteQuery();
        });

        // Add the popup menu to the JList.
        this.queryList.setComponentPopupMenu(popupMenu);

        // ==================== JList ====================

        // 1. Update the JTextArea when a query is selected.
        // 2. Only enable `Delete Query` and 'Rename Query' when an item is selected.
        this.queryList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Query selectedQuery = this.queryList.getSelectedValue();
                if (selectedQuery != null) {
                    this.queryDetailsArea.setText(selectedQuery.text);
                    deleteQueryItem.setEnabled(true); // Enable `Delete Query`
                } else {
                    this.queryDetailsArea.setText("");
                    deleteQueryItem.setEnabled(false); // Disable `Delete Query`
                }
            }
        });

        // Double-click to edit the selected query.
        this.queryList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                // Detect left double-click.
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                    editQuery();
                }
                // Detect middle-click.
                if (e.getButton() == MouseEvent.BUTTON2) {
                    int index = queryList.locationToIndex(e.getPoint());
                    if (index != -1 && queryList.getCellBounds(index, index).contains(e.getPoint())) {
                        queryList.setSelectedIndex(index); // Ensure the item is selected
                        deleteQuery();
                    }
                }
            }
        });
    }

    // Constructor that takes a JTextArea and queries.
    public Sidebar(JTextArea queryDetailsArea, List<Query> queries) {
        this(queryDetailsArea);
        this.setQueries(queries);
    }

    public List<Query> getQueries() {
        List<Query> queries = new ArrayList<>();
        for (int i = 0; i < queryListModel.size(); i++) {
            queries.add(this.queryListModel.get(i));
        }
        return queries;
    }

    public void setQueries(List<Query> queries) {
        this.queryListModel.clear();
        for (Query q : queries) {
            this.queryListModel.addElement(q);
        }
    }

    // Helper method to add a new query. The user will enter the title in a
    // dialog box and the text of the query from the JTextArea.
    private void newQuery() {
        String initialText = this.queryDetailsArea.getText();

        String title = JOptionPane.showInputDialog(
                Utils.burpFrame(),
                "Enter Query Title:",
                "New Query",
                JOptionPane.PLAIN_MESSAGE).toString();
        if (title != null && !title.trim().isEmpty()) {
            Query newQuery = new Query(title, initialText != null ? initialText : "");
            this.queryListModel.addElement(newQuery);
            // Select the new query in the list to update the JList.
            this.queryList.setSelectedValue(newQuery, true);
        }
        // Refresh the list to show the new query (if any)
        this.queryList.repaint();
    }

    // Edit the selected query.
    public void editQuery() {
        Query selectedQuery = this.queryList.getSelectedValue();
        if (selectedQuery != null) {
            String newTitle = JOptionPane.showInputDialog(
                    Utils.burpFrame(),
                    "Rename Query:",
                    selectedQuery.title);
            if (newTitle != null && !newTitle.trim().isEmpty()) {
                selectedQuery.title = newTitle;
                // Manually notify the model that the item has changed so
                // `DefaultListModel.contentsChanged` can fire.
                int selectedIndex = this.queryList.getSelectedIndex();
                queryListModel.setElementAt(selectedQuery, selectedIndex);
            }
        }
    }

    // Delete the selected query.
    public void deleteQuery() {
        Query selectedQuery = queryList.getSelectedValue();
        if (selectedQuery != null) {
            int confirm = JOptionPane.showConfirmDialog(
                    Utils.burpFrame(),
                    "Are you sure you want to delete `" + selectedQuery.title + "`?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                queryListModel.removeElement(selectedQuery);
            }
        }
    }

    // Save a query based on the text in the JTextArea. If a query is selected,
    // update it. Otherwise, add a new query.
    public void saveQueryDetails() {
        Query selectedQuery = this.queryList.getSelectedValue();
        if (selectedQuery != null) {
            selectedQuery.text = this.queryDetailsArea.getText();
            // Manually notify the model that the item has changed so
            // `DefaultListModel.contentsChanged` can fire.
            int selectedIndex = this.queryList.getSelectedIndex();
            queryListModel.setElementAt(selectedQuery, selectedIndex);
        } else {
            // Add a new query with the JTextArea content.
            this.newQuery();
        }
    }
}
