package looking_glass.ui.burp_domain_filter;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import looking_glass.common.Log;

public class BurpDomainFilter extends JPanel {

    private TableModel tableModel;
    private JTable table;
    private JLabel label;
    private List<FilterRule> rules;

    private JTextField inputField;
    private JCheckBox includeSubdomainsCheckbox;

    public List<FilterRule> getRules() {
        return rules;
    }

    public void setRules(List<FilterRule> rules) {
        this.rules = rules;
        tableModel.setRowCount(0); // Clear existing rows
        for (FilterRule rule : rules) {
            tableModel.addRow(new Object[] { rule.enabled, rule.prefix, rule.includeSubdomains });
        }
    }

    public BurpDomainFilter(String labelValue, String columnName) {

        this.setLayout(new BorderLayout());

        // The label shown on top of the table.
        label = new JLabel(labelValue);
        this.add(label, BorderLayout.NORTH);

        // The table model and table.
        this.tableModel = new TableModel();
        this.table = new JTable(tableModel);

        // Add a listener to the table to update the rule when a user changes
        // the isEnabled checkbox.
        tableModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (e.getType() == TableModelEvent.UPDATE) {
                    int row = e.getFirstRow();
                    int column = e.getColumn();

                    if (column == 0) { // "Enabled" column
                        boolean isEnabled = (boolean) tableModel.getValueAt(row, 0);
                        FilterRule rule = rules.get(row);
                        rule.enabled = isEnabled;
                        rules.set(row, rule);
                    }
                }
            }
        });

        // Add the table to a scroll pane.
        JScrollPane scrollPane = new JScrollPane(table);
        // Add the scroll pane to the panel with some space.
        this.add(scrollPane, BorderLayout.CENTER);

        // Create a new panel for the buttons.
        JPanel buttonPanel = new JPanel();
        // Grid layout for the buttons.
        // 5 rows, 1 column, no horizontal space, 5 vertical space.
        buttonPanel.setLayout(new GridLayout(5, 1, 0, 5));

        // Create the buttons.
        JButton addBtn = new JButton("Add");
        JButton editBtn = new JButton("Edit");
        JButton removeBtn = new JButton("Remove");
        JButton pasteBtn = new JButton("Paste");
        JButton loadBtn = new JButton("Load from file");

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

        JPanel addDialog = domainDialog(null, false);
        int result = JOptionPane.showConfirmDialog(this, addDialog, "Add domain", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String input = inputField.getText();
            boolean includeSubdomains = includeSubdomainsCheckbox.isSelected();
            if (input != null && !input.isEmpty()) {
                FilterRule rule = new FilterRule(true, input, includeSubdomains);
                rules.add(rule);
                tableModel.addRule(rule);
            }
        }
    }

    private void editAction() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            FilterRule rule = rules.get(selectedRow);

            JPanel editDialog = domainDialog(rule.prefix, rule.includeSubdomains);

            int result = JOptionPane.showConfirmDialog(this, editDialog, "Edit Domain", JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                String input = inputField.getText();
                boolean includeSubdomains = includeSubdomainsCheckbox.isSelected();
                if (input != null && !input.isEmpty()) {
                    rule.prefix = input;
                    rule.includeSubdomains = includeSubdomains;
                    tableModel.setValueAt(rule.prefix, selectedRow, 1);
                    tableModel.setValueAt(rule.includeSubdomains, selectedRow, 2);
                }
            }
        }
    }

    // Remove every selected row.
    private void removeAction() {
        int[] selectedRows = table.getSelectedRows();
        if (selectedRows.length > 0) {
            for (int i = selectedRows.length - 1; i >= 0; i--) {
                tableModel.removeRow(selectedRows[i]);
                rules.remove(selectedRows[i]);
            }
        }
    }

    private void pasteAction() {
        try {
            String clipboardText = (String) Toolkit.getDefaultToolkit().getSystemClipboard()
                    .getData(DataFlavor.stringFlavor);
            if (clipboardText != null) {
                FilterRule rule = new FilterRule(true, clipboardText, false);
                rules.add(rule);
                tableModel.addRule(rule);
            }
        } catch (UnsupportedFlavorException | IOException e) {
            // Display the error message.
            JOptionPane.showMessageDialog(null, "Clipboard error " + e.getMessage(),
                    "Error",
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

                    FilterRule rule = new FilterRule(true, line, false);
                    rules.add(rule);
                    tableModel.addRule(rule);
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Error reading file: " + e.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
                Log.toError("Error reading file: " + e.getMessage());
            }
        }
    }

    // The panel shown when the user wants to add or edit a domain.
    private JPanel domainDialog(String prefix, boolean includeSubdomains) {

        JPanel panel = new JPanel(new GridLayout(3, 1));
        JLabel dialogLabel = new JLabel("Domain: ");
        this.inputField = new JTextField(prefix == null ? "" : prefix);
        this.includeSubdomainsCheckbox = new JCheckBox("Include subdomains", includeSubdomains);

        panel.add(dialogLabel);
        panel.add(inputField);
        panel.add(includeSubdomainsCheckbox);

        return panel;
    }
}