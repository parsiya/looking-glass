package looking_glass.ui.burp_domain_filter;

import javax.swing.table.DefaultTableModel;

class TableModel extends DefaultTableModel {
    private static final String[] COLUMN_NAMES = {"Enabled", "Prefix", "Include Subdomains"};

    public TableModel() {
        super(COLUMN_NAMES, 0);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
            case 2:
                return Boolean.class;
            case 1:
                return String.class;
            default:
                return Object.class;
        }
    }

    public void addRule(FilterRule rule) {
        Object[] row = {rule.enabled, rule.prefix, rule.includeSubdomains};
        addRow(row);
    }

    // Make only the "Enabled" column editable.
    @Override
    public boolean isCellEditable(int row, int column) {
        return column == 0; // Only the first column is editable
    }
}

