package looking_glass.ui;

import java.awt.*;
import java.io.File;
import java.nio.file.Files;

import javax.swing.*;
import javax.swing.text.NumberFormatter;

import java.text.NumberFormat;

import looking_glass.ExtensionSettings;
import looking_glass.Handler;
import looking_glass.common.Log;
import looking_glass.common.Utils;
import looking_glass.ui.burp_domain_filter.BurpDomainFilter;

public class SettingsDialog extends JDialog {

    // ==================== UI Text ====================
    // Settings dialog name.
    private static final String SETTINGS_FRAME_NAME = "Looking Glass Settings";
    // Include and exclude table names.
    private static final String INCLUDE_TABLE = "Include";
    private static final String EXCLUDE_TABLE = "Exclude";
    // Include and exclude table column names.
    private static final String COLUMN_NAME = "Host";
    // Scope panel name
    private static final String SCOPE_PANEL = "Target Scope";

    // Filter panel name.
    private static final String FILTER_PANEL = "Filter request or response body";
    // // MIME type filter panel name
    // private static final String MIME_TYPE_FILTER_PANEL = "MIME type";
    // // MIME type filter names. Based on the Burp proxy filter.
    // private static final String[] MIME_LABELS = { "HTML", "Other text", "Script",
    // "Images",
    // "XML", "Flash", "CSS", "Other binary" };

    // Import/Export Settings
    private static final String IMPORT_EXPORT_SETTINGS = "Import/Export settings";

    // Size filter panel name
    private static final String SIZE_FILTER_PANEL = "Skip body size";
    private static final String SIZE_TOOLTIP = "Enter the size in MB";
    private static final String SIZE_LABEL = "Larger than (MB)";

    // File extension filter panel name
    private static final String FILE_EXTENSION_FILTER_PANEL = "File extension";
    // Store only and skip labels
    private static final String STORE_LABEL = "Store:";
    private static final String STORE_TOOLTIP = "File extensions to store. Comma separated";
    private static final String SKIP_LABEL = "Skip:";
    private static final String SKIP_TOOLTIP = "File extensions to skip. Comma separated";

    // Checkbox Labels
    private static final String CAPTURE_LABEL = "Capture on startup";
    private static final String CAPTURE_TOOLTIP = "If checked, the extension will start capturing requests and responses on startup";
    // private static final String CHECKDB_LABEL = "Don't check DB on startup";
    // private static final String CHECKDB_TOOLTIP = "If checked, the extension will not ask to choose a DB on startup";
    private static final String STARTUP_LABEL = "Startup Settings"; // ZZZ modify if you add more settings in it.

    // Buttons
    private static final String CANCEL_BUTTON = "Cancel";
    private static final String SAVE_BUTTON = "Apply & Close";
    private static final String IMPORT_LABEL = "Import";
    private static final String IMPORT_TOOLTIP = "Import settings from a file";
    private static final String EXPORT_LABEL = "Export";
    private static final String EXPORT_TOOLTIP = "Export settings to a file";

    private BurpDomainFilter include, exclude;
    private JTextField storeTextField, skipTextField;
    private LabeledCheckBox sizeCheckBox, storeCheckBox, skipCheckBox;
    private LabeledCheckBox captureOnStartup;
    // private LabeledCheckBox checkDBOnStartup;
    // private LabeledCheckBox[] mimeTypeCheckBoxes;
    private JFormattedTextField sizeTextField;

    // Issue: I used a singleton instance for the settings dialog, but it is not
    // working because it kept the changes even after closing the frame so I am
    // creating a new instance every time instead. This might lead to another
    // issue that clicking the setting button multiple times will create
    // multiple instances of the settings dialog.

    public SettingsDialog() {
        super((Frame) Utils.burpFrame(), SETTINGS_FRAME_NAME);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        // Make it a modal so users cannot click Burp until they're done here.
        this.setModalityType(ModalityType.APPLICATION_MODAL);

        this.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // This is simulating a group box to hold the scope. Designed to look
        // like the Burp `Target > Scope` tab.
        JPanel scopePanel = new JPanel();
        scopePanel.setBorder(BorderFactory.createTitledBorder(SCOPE_PANEL));
        scopePanel.setLayout(new BoxLayout(scopePanel, BoxLayout.Y_AXIS));
        // Create the include and exclude host tablemodels.
        this.include = new BurpDomainFilter(INCLUDE_TABLE, COLUMN_NAME);
        this.exclude = new BurpDomainFilter(EXCLUDE_TABLE, COLUMN_NAME);
        // Add everything.
        scopePanel.add(include);
        // Add a vertical space between the tables.
        scopePanel.add(Box.createVerticalStrut(10));
        scopePanel.add(exclude);
        // Add scopePanel to the left position.
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 10;
        gbc.weighty = 10;
        this.add(scopePanel, gbc);

        // Right panel side.
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.PAGE_AXIS));

        // Right panel side.
        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.PAGE_AXIS));
        filterPanel.setBorder(BorderFactory.createTitledBorder(FILTER_PANEL));

        // To prevent the GridLayout from stretching the components, we wrap the
        // elements in a flow layout.
        // JPanel mimeTypeFilter = new JPanel();
        // mimeTypeFilter.setBorder(BorderFactory.createTitledBorder(MIME_TYPE_FILTER_PANEL));
        // mimeTypeFilter.setLayout(new BorderLayout());

        // // Create a wrapper panel with FlowLayout.
        // JPanel flowWrapper = new JPanel();

        // // Create a panel with GridLayout.
        // JPanel gridPanel = new JPanel();
        // gridPanel.setLayout(new GridLayout(4, 2, 10, 10));

        // // Add the 8 MIME Type checks to the grid panel.
        // mimeTypeCheckBoxes = new LabeledCheckBox[MIME_LABELS.length];
        // for (int i = 0; i < MIME_LABELS.length; i++) {
        // mimeTypeCheckBoxes[i] = new LabeledCheckBox(MIME_LABELS[i]);
        // // Add the checkbox to your form.
        // gridPanel.add(mimeTypeCheckBoxes[i]);
        // }

        // // Add the grid panel to the flow wrapper.
        // flowWrapper.add(gridPanel);

        // // Use the flow wrapper as the main panel.
        // mimeTypeFilter.add(flowWrapper, BorderLayout.WEST);

        // `Skip body by size` group box.
        JPanel sizeFilterPanel = new JPanel();
        sizeFilterPanel.setBorder(BorderFactory.createTitledBorder(SIZE_FILTER_PANEL));
        sizeFilterPanel.setLayout(new GridLayout(1, 1));
        sizeFilterPanel.setBorder(BorderFactory.createCompoundBorder(
                sizeFilterPanel.getBorder(),
                BorderFactory.createEmptyBorder(5, 0, 5, 5)));
        this.sizeCheckBox = new LabeledCheckBox(SIZE_LABEL);
        sizeFilterPanel.add(sizeCheckBox);
        // Set up the size text field to only accept numbers.
        NumberFormat format = NumberFormat.getInstance();
        NumberFormatter formatter = new NumberFormatter(format);
        formatter.setValueClass(Integer.class);
        formatter.setAllowsInvalid(false);
        this.sizeTextField = new JFormattedTextField(formatter);
        this.sizeTextField.setColumns(10);
        this.sizeTextField.setToolTipText(SIZE_TOOLTIP);
        sizeFilterPanel.add(sizeTextField);

        // File extensions.
        //
        // This is designed to look like Burp's `Proxy History > Filter` (the
        // frame after clicking on the filter bar in proxy history).
        JPanel extensionFilterPanel = new JPanel();
        extensionFilterPanel.setBorder(BorderFactory.createTitledBorder(FILE_EXTENSION_FILTER_PANEL));
        extensionFilterPanel.setBorder(BorderFactory.createCompoundBorder(
                extensionFilterPanel.getBorder(),
                BorderFactory.createEmptyBorder(5, 0, 5, 5)));

        // 2 rows, 2 columns, 0 horizontal space, 10 vertical space.
        extensionFilterPanel.setLayout(new GridLayout(2, 2, 0, 10));

        // Show only file extensions.
        this.storeCheckBox = new LabeledCheckBox(STORE_LABEL);
        this.storeCheckBox.setToolTipText(STORE_TOOLTIP);
        this.storeTextField = new JTextField();

        // Hide file extensions.
        this.skipCheckBox = new LabeledCheckBox(SKIP_LABEL);
        this.skipCheckBox.setToolTipText(SKIP_TOOLTIP);
        this.skipTextField = new JTextField();

        // Like the proxy filter, if one checkbox is selected, the other is disabled.
        this.storeCheckBox.getCheckBox().addActionListener(e -> {
            if (storeCheckBox.isSelected()) {
                skipCheckBox.setSelected(false);
                skipCheckBox.setEnabled(false);
                skipTextField.setEnabled(false);
            } else {
                skipCheckBox.setEnabled(true);
                skipTextField.setEnabled(true);
            }
        });
        // This is to make clicking the label do the same as above.
        this.storeCheckBox.getLabel().addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (storeCheckBox.isSelected()) {
                    skipCheckBox.setSelected(false);
                    skipCheckBox.setEnabled(false);
                    skipTextField.setEnabled(false);
                } else {
                    skipCheckBox.setEnabled(true);
                    skipTextField.setEnabled(true);
                }
            }
        });

        this.skipCheckBox.getCheckBox().addActionListener(e -> {
            if (skipCheckBox.isSelected()) {
                storeCheckBox.setSelected(false);
                storeCheckBox.setEnabled(false);
                storeTextField.setEnabled(false);
            } else {
                storeCheckBox.setEnabled(true);
                storeTextField.setEnabled(true);
            }
        });

        this.skipCheckBox.getLabel().addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (skipCheckBox.isSelected()) {
                    storeCheckBox.setSelected(false);
                    storeCheckBox.setEnabled(false);
                    storeTextField.setEnabled(false);
                } else {
                    storeCheckBox.setEnabled(true);
                    storeTextField.setEnabled(true);
                }
            }
        });

        // Add the two checkboxes to the panel.
        extensionFilterPanel.add(storeCheckBox);
        extensionFilterPanel.add(storeTextField);
        extensionFilterPanel.add(skipCheckBox);
        extensionFilterPanel.add(skipTextField);

        // Create a panel for checkbox settings.
        JPanel checkboxPanel = new JPanel();
        checkboxPanel.setLayout(new GridLayout(1, 1, 10, 10));

        this.captureOnStartup = new LabeledCheckBox(CAPTURE_LABEL);
        this.captureOnStartup.setToolTipText(CAPTURE_TOOLTIP);
        checkboxPanel.add(captureOnStartup);

        // this.checkDBOnStartup = new LabeledCheckBox(CHECKDB_LABEL);
        // this.checkDBOnStartup.setToolTipText(CHECKDB_TOOLTIP);
        // checkboxPanel.add(checkDBOnStartup);
        checkboxPanel.setBorder(BorderFactory.createTitledBorder(STARTUP_LABEL));

        // Group box to import and export settings.
        JPanel importExportPanel = new JPanel();
        importExportPanel.setBorder(BorderFactory.createTitledBorder(IMPORT_EXPORT_SETTINGS));
        importExportPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));

        // Add two buttons to it.
        JButton importBtn = new JButton(IMPORT_LABEL);
        importBtn.setToolTipText(IMPORT_TOOLTIP);
        importBtn.addActionListener(e -> importSettings());

        JButton exportBtn = new JButton(EXPORT_LABEL);
        exportBtn.setToolTipText(EXPORT_TOOLTIP);
        exportBtn.addActionListener(e -> exportSettings());

        // Add the buttons to the panel.
        importExportPanel.add(importBtn);
        importExportPanel.add(exportBtn);

        // `Save panel` section.
        JPanel savePanel = new JPanel();
        savePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));

        // Cancel button.
        JButton cancelButton = new JButton(CANCEL_BUTTON);
        cancelButton.addActionListener(e -> {
            this.dispose();
        });

        // Save and close button.
        JButton applyBtn = new JButton(SAVE_BUTTON);
        applyBtn.setBackground(new Color(255, 102, 51));
        applyBtn.setForeground(Color.WHITE);
        applyBtn.setFont(applyBtn.getFont().deriveFont(Font.BOLD));
        applyBtn.addActionListener(e -> {
            // Save the settings and close the form.
            this.saveSettings();
            this.dispose();
        });

        // Add buttons to the save panel
        savePanel.add(cancelButton);
        savePanel.add(applyBtn);

        // Add all the buttons to the filter panel that will be the right part.
        // filterPanel.add(mimeTypeFilter);
        filterPanel.add(sizeFilterPanel);
        filterPanel.add(extensionFilterPanel);
        rightPanel.add(filterPanel);
        // Add vertical space before the save panel.
        rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(checkboxPanel);
        rightPanel.add(importExportPanel);
        rightPanel.add(savePanel);

        // Add rightPanel to the EAST position.
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.1;
        gbc.weighty = 0.1;
        this.add(rightPanel, gbc);

        this.setPreferredSize(new Dimension(900, 450));

        // Read the settings from the handler.
        this.loadSettings(Handler.getInstance().getSettings());

        // Pack it all up.
        this.pack();
        // Issue: The frame shows big tables and the size becomes correct after
        // resizing it. Adding these two lines to fix it?
        this.invalidate();
        this.repaint();
    }

    public void display() {
        try {
            this.setLocationRelativeTo(Utils.burpFrame());
            this.setVisible(true);
        } catch (Exception e) {
            Log.toError("Error displaying settings dialog: " + e.getMessage());
        }
    }

    // Import settings from a file.
    private void importSettings() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("*.json", "json"));
            int returnValue = fileChooser.showOpenDialog(this);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();

                String json = Files.readString(selectedFile.toPath());
                ExtensionSettings settings = new ExtensionSettings(json);
                this.loadSettings(settings);
            }
        } catch (Exception e) {
            Utils.msgBox("Error", "Error importing settings: " + e.getMessage());
            Log.toError("Error importing settings: " + e.getMessage());
        }
    }

    // Export settings to a file.
    private void exportSettings() {
        try {
            // Save current settings which might have been modified.
            this.saveSettings();
            JFileChooser fileChooser = new JFileChooser();
            int returnValue = fileChooser.showSaveDialog(this);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                if (selectedFile.exists()) {
                    int overwrite = JOptionPane.showConfirmDialog(null,
                            "File already exists. Do you want to overwrite it?", "Overwrite file",
                            JOptionPane.YES_NO_OPTION);
                    if (overwrite != JOptionPane.YES_OPTION) {
                        return;
                    }
                }
                String json = Handler.getInstance().getSettings().toJson();
                Files.writeString(selectedFile.toPath(), json);

            }
        } catch (Exception e) {
            Utils.msgBox("Error", "Error exporting settings: " + e.getMessage());
            Log.toError("Error exporting settings: " + e.getMessage());
        }
    }

    public void saveSettings() {
        ExtensionSettings settings = new ExtensionSettings();

        // Store include and exclude table data.
        settings.includeTableData = this.include.getRules();
        settings.excludeTableData = this.exclude.getRules();

        // // Store MIME type filter states.
        // boolean[] mimeTypeStates = new boolean[MIME_LABELS.length];
        // for (int i = 0; i < MIME_LABELS.length; i++) {
        // mimeTypeStates[i] = this.mimeTypeCheckBoxes[i].isSelected();
        // }
        // settings.mimeTypes = mimeTypeStates;

        // Store filters size, show/hide file extensions.
        settings.storeFileExtensionStatus = this.storeCheckBox.isSelected();
        settings.storeFileExtensions = this.storeTextField.getText();
        settings.skipFileExtensionStatus = this.skipCheckBox.isSelected();
        settings.hideFileExtensions = this.skipTextField.getText();

        // Try to convert the size value to an integer.
        settings.bodySizeStatus = this.sizeCheckBox.isSelected();
        settings.bodySizeValue = (Long) this.sizeTextField.getValue();
        settings.captureOnStartup = this.captureOnStartup.isSelected();

        // Store it in the handler. This will also update the filter and store
        // the settings in the extension settings in Burp.
        Handler.getInstance().setSettings(settings);
    }

    // Loads the settings data into the Settings Dialog.
    private void loadSettings(ExtensionSettings settings) {
        // Load the include and exclude table data.
        this.include.setRules(settings.includeTableData);
        this.exclude.setRules(settings.excludeTableData);
        // // Load the MIME type filter states.
        // boolean[] mimeTypeStates = settings.mimeTypes;
        // for (int i = 0; i < MIME_LABELS.length; i++) {
        // this.mimeTypeCheckBoxes[i].setSelected(mimeTypeStates[i]);
        // }
        // Load the filters size, show/hide file extensions.
        this.sizeCheckBox.setSelected(settings.bodySizeStatus);
        this.sizeTextField.setValue(settings.bodySizeValue);
        this.storeCheckBox.setSelected(settings.storeFileExtensionStatus);
        this.storeTextField.setText(settings.storeFileExtensions);
        this.skipCheckBox.setSelected(settings.skipFileExtensionStatus);
        this.skipTextField.setText(settings.hideFileExtensions);
        this.captureOnStartup.setSelected(settings.captureOnStartup);
    }
}