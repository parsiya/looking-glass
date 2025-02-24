package looking_glass.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import looking_glass.common.Log;
import looking_glass.common.Utils;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class TableEditor extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel label;
    private String columnName;
    private Component parent;

    // Things to do:
    //
    // 1. parent component in the joptionpane should be set to the burp ui. I
    // know how to do this, just look at the code that already does it.
    //
    // 2. Change the constructor so we can customize this for other uses, for
    // example we can set the text in the dialogs, the tablemodel and other
    // things
    //
    // 3.

    public TableEditor(String labelValue, String columnName) {

        // Set parent to Burp frame.
        parent = this;

        this.setLayout(new BorderLayout());

        // Set the column name to use in dialogs.
        this.columnName = columnName;

        // Create the label
        label = new JLabel(labelValue);
        this.add(label, BorderLayout.NORTH);

        // Create a new table model and table
        tableModel = new StringTableModel(columnName);
        table = new JTable(tableModel);

        // Add the table to a scroll pane
        JScrollPane scrollPane = new JScrollPane(table);

        // Add the scroll pane to the panel
        this.add(scrollPane, BorderLayout.CENTER);

        // Create a new panel for the buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(5, 1));

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

        // Add the buttons to the button panel
        buttonPanel.add(addBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(removeBtn);
        buttonPanel.add(pasteBtn);
        buttonPanel.add(loadBtn);

        this.add(buttonPanel, BorderLayout.WEST);
    }

    private void addAction() {
        String dialogTitle = "Add " + columnName;
        String dialogLabel = "Enter " + columnName + " :";
        String input = JOptionPane.showInputDialog(parent, dialogLabel, dialogTitle, JOptionPane.QUESTION_MESSAGE);
        if (input != null && !input.isEmpty()) {
            tableModel.addRow(new String[] { input });
        }
    }

    private void editAction() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            String oldValue = (String) tableModel.getValueAt(selectedRow, 0);
            String newValue = (String) JOptionPane.showInputDialog(parent, "Enter new value:", "Edit Item",
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
                tableModel.addRow(new String[]{clipboardText});
            }
        } catch (UnsupportedFlavorException | IOException e) {
            // Display the error message.
            JOptionPane.showMessageDialog(null, "Clipboard error " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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