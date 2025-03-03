package looking_glass.ui;

import javax.swing.table.DefaultTableModel;

// This is a table model with only one column of type String.
// TODO: Make a class that creates one based on the `Target > Scope` tab with
// `enabled` checkbox, `prefix` text and `include subdomains` checkbox.
public class BurpTableModel extends DefaultTableModel {
    public BurpTableModel(String columnName) {
        addColumn(columnName);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }


    // Make the table readonly.
    @Override
    public boolean isCellEditable(int row, int column) {
        return false; // Make all cells non-editable
    }
}