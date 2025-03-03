package looking_glass.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import looking_glass.common.Log;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

public class TableEditor extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel label;
    private String columnName;

    public JTable getTable() {
        return table;
    }

    public void setTable(JTable table) {
        this.table = table;
    }

    public DefaultTableModel getTableModel() {
        return tableModel;
    }

    public void setTableModel(DefaultTableModel tableModel) {
        this.tableModel = tableModel;
    }

    public JLabel getLabel() {
        return label;
    }

    public void setLabel(JLabel label) {
        this.label = label;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public Vector<Vector> getData() {
        return tableModel.getDataVector();
    }
    
    public void setData(Vector<Vector> tableData) {
        tableModel.setDataVector(tableData, new Vector<String>() {{
            add(columnName);
        }});
    }

    // TODO:
    //
    // 1. Make this a generic table editor with `Object[]` for the eventual add
    // to the utilities repo. All the `String[]` can be replaced with
    // `Object[]`.

    public TableEditor(String labelValue, String columnName) {

        this.setLayout(new BorderLayout());

        // Set the column name to use in dialogs.
        this.columnName = columnName;

        // Create the label
        label = new JLabel(labelValue);
        this.add(label, BorderLayout.NORTH);

        // Create a new table model and table
        tableModel = new BurpTableModel(columnName);
        table = new JTable(tableModel);

        // Add the table to a scroll pane
        JScrollPane scrollPane = new JScrollPane(table);

        // Add the scroll pane to the panel with some space
        this.add(scrollPane, BorderLayout.CENTER);

        // Create a new panel for the buttons
        JPanel buttonPanel = new JPanel();
        // Grid layout. 5 rows, 1 column, no horizontal space, 5 vertical space.
        buttonPanel.setLayout(new GridLayout(5, 1, 0, 5));

        // Create the buttons
        JButton addBtn = new JButton("Add");
        JButton editBtn = new JButton("Edit");
        JButton removeBtn = new JButton("Remove");
        JButton pasteBtn = new JButton("Paste");
        JButton loadBtn = new JButton("Load from file");

        // Add action listeners to the buttons
        addBtn.addActionListener(e -> addAction());
        editBtn.addActionListener(e -> editAction());
        removeBtn.addActionListener(e -> removeAction());
        pasteBtn.addActionListener(e -> pasteAction());
        loadBtn.addActionListener(e -> loadAction());

        // Add the buttons to the button panel with some space
        buttonPanel.add(addBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(removeBtn);
        buttonPanel.add(pasteBtn);
        buttonPanel.add(loadBtn);

        this.add(buttonPanel, BorderLayout.WEST);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
    }

    private void addAction() {
        String dialogTitle = "Add " + columnName;
        String dialogLabel = "Enter " + columnName + " :";
        String input = JOptionPane.showInputDialog(this, dialogLabel, dialogTitle, JOptionPane.QUESTION_MESSAGE);
        if (input != null && !input.isEmpty()) {
            tableModel.addRow(new String[] { input });
        }
    }

    private void editAction() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            String oldValue = (String) tableModel.getValueAt(selectedRow, 0);
            String newValue = (String) JOptionPane.showInputDialog(this, "Enter new value:", "Edit Item",
                    JOptionPane.QUESTION_MESSAGE, null, null, oldValue);
            if (newValue != null) {
                tableModel.setValueAt(newValue, selectedRow, 0);
            }
        }
    }

    // Remove every selected row.
    private void removeAction() {
        int[] selectedRows = table.getSelectedRows();
        if (selectedRows.length > 0) {
            for (int i = selectedRows.length - 1; i >= 0; i--) {
                tableModel.removeRow(selectedRows[i]);
            }
        }
    }

    private void pasteAction() {
        try {
            String clipboardText = (String) Toolkit.getDefaultToolkit().getSystemClipboard()
                    .getData(DataFlavor.stringFlavor);
            if (clipboardText != null) {
                tableModel.addRow(new String[] { clipboardText });
            }
        } catch (UnsupportedFlavorException | IOException e) {
            // Display the error message.
            JOptionPane.showMessageDialog(null, "Clipboard error " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            Log.toError("Clipboard error " + e.getMessage());
        }
    }

    private void loadAction() {
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try (BufferedReader reader = new BufferedReader(new FileReader(selectedFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    tableModel.addRow(new String[] { line });
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Error reading file: " + e.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
                Log.toError("Error reading file: " + e.getMessage());
            }
        }
    }
}