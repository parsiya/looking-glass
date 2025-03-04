package looking_glass.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import looking_glass.ExtensionSettings;
import looking_glass.common.Constants;
import looking_glass.common.Log;
import looking_glass.common.Utils;

public class SettingsDialog extends JDialog {

    // Labels
    // ---------
    // Settings dialog name.
    private static final String SETTINGS_FRAME_NAME = "Looking Glass Settings"; // ZZZ change this.
    // Include and exclude table names.
    private static final String INCLUDE_TABLE = "Include";
    private static final String EXCLUDE_TABLE = "Exclude";
    // Include and exclude table column names.
    private static final String COLUMN_NAME = "Host";
    // Scope panel name
    private static final String SCOPE_PANEL = "Target Scope";

    // Filter panel name.
    private static final String FILTER_PANEL = "Filter request or response body";
    // MIME type filter panel name
    private static final String MIME_TYPE_FILTER_PANEL = "MIME type";
    // MIME type filter names. Based on the Burp proxy filter.
    private static final String[] MIME_LABELS = { "HTML", "Other text", "Script", "Images",
            "XML", "Flash", "CSS", "Other binary" };

    // Size filter panel name
    private static final String SIZE_FILTER_PANEL = "Skip body size";
    private static final String SIZE_LABEL = "Larger than (MB)";

    // File extension filter panel name
    private static final String FILE_EXTENSION_FILTER_PANEL = "File extension";
    // Store only and skip labels
    private static final String SHOW_ONLY_LABEL = "Store:";
    private static final String HIDE_LABEL = "Skip:";

    // Buttons
    private static final String CANCEL_BUTTON = "Cancel";
    private static final String SAVE_BUTTON = "Apply & Close";

    private TableEditor include, exclude;
    private JTextField showTextField, hideTextField, sizeTextField;
    private LabeledCheckBox sizeCheckBox, showCheckBox, hideCheckBox;
    private LabeledCheckBox[] mimeTypeCheckBoxes;

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
        this.include = new TableEditor(INCLUDE_TABLE, COLUMN_NAME);
        this.exclude = new TableEditor(EXCLUDE_TABLE, COLUMN_NAME);
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
        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.PAGE_AXIS));
        filterPanel.setBorder(BorderFactory.createTitledBorder(FILTER_PANEL));

        // To prevent the GridLayout from stretching the components, we wrap the
        // elements in a flow layout.
        JPanel mimeTypeFilter = new JPanel();
        mimeTypeFilter.setBorder(BorderFactory.createTitledBorder(MIME_TYPE_FILTER_PANEL));
        mimeTypeFilter.setLayout(new BorderLayout());

        // Create a wrapper panel with FlowLayout.
        JPanel flowWrapper = new JPanel();
        // flowWrapper.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 10));

        // Create a panel with GridLayout.
        JPanel gridPanel = new JPanel();
        gridPanel.setLayout(new GridLayout(4, 2, 10, 10));

        // Add the 8 MIME Type checks to the grid panel.
        mimeTypeCheckBoxes = new LabeledCheckBox[MIME_LABELS.length];
        for (int i = 0; i < MIME_LABELS.length; i++) {
            mimeTypeCheckBoxes[i] = new LabeledCheckBox(MIME_LABELS[i]);
            // Add the checkbox to your form.
            gridPanel.add(mimeTypeCheckBoxes[i]);
        }

        // Add the grid panel to the flow wrapper.
        flowWrapper.add(gridPanel);

        // Use the flow wrapper as the main panel.
        mimeTypeFilter.add(flowWrapper, BorderLayout.WEST);

        // `Skip body by size` group box.
        JPanel sizeFilterPanel = new JPanel();
        sizeFilterPanel.setBorder(BorderFactory.createTitledBorder(SIZE_FILTER_PANEL));
        sizeFilterPanel.setLayout(new GridLayout(1, 1));
        sizeFilterPanel.setBorder(BorderFactory.createCompoundBorder(
                sizeFilterPanel.getBorder(),
                BorderFactory.createEmptyBorder(5, 0, 5, 5)));
        this.sizeCheckBox = new LabeledCheckBox(SIZE_LABEL);
        sizeTextField = new JTextField();
        sizeFilterPanel.add(sizeCheckBox);
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
        this.showCheckBox = new LabeledCheckBox(SHOW_ONLY_LABEL);
        this.showTextField = new JTextField();

        // Hide file extensions.
        this.hideCheckBox = new LabeledCheckBox(HIDE_LABEL);
        this.hideTextField = new JTextField();

        // Like the proxy filter, if one checkbox is selected, the other is disabled.
        showCheckBox.getCheckBox().addActionListener(e -> {
            if (showCheckBox.isSelected()) {
                hideCheckBox.setSelected(false);
                hideCheckBox.setEnabled(false);
                hideTextField.setEnabled(false);
            } else {
                hideCheckBox.setEnabled(true);
                hideTextField.setEnabled(true);
            }
        });
        // This is to make clicking the label do the same as above.
        showCheckBox.getLabel().addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (showCheckBox.isSelected()) {
                    hideCheckBox.setSelected(false);
                    hideCheckBox.setEnabled(false);
                    hideTextField.setEnabled(false);
                } else {
                    hideCheckBox.setEnabled(true);
                    hideTextField.setEnabled(true);
                }
            }
        });

        hideCheckBox.getCheckBox().addActionListener(e -> {
            if (hideCheckBox.isSelected()) {
                showCheckBox.setSelected(false);
                showCheckBox.setEnabled(false);
                showTextField.setEnabled(false);
            } else {
                showCheckBox.setEnabled(true);
                showTextField.setEnabled(true);
            }
        });

        hideCheckBox.getLabel().addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (hideCheckBox.isSelected()) {
                    showCheckBox.setSelected(false);
                    showCheckBox.setEnabled(false);
                    showTextField.setEnabled(false);
                } else {
                    showCheckBox.setEnabled(true);
                    showTextField.setEnabled(true);
                }
            }
        });

        // Add the two checkboxes to the panel.
        extensionFilterPanel.add(showCheckBox);
        extensionFilterPanel.add(showTextField);
        extensionFilterPanel.add(hideCheckBox);
        extensionFilterPanel.add(hideTextField);

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
            this.save();
            this.dispose();
        });

        // Add buttons to the save panel
        savePanel.add(cancelButton);
        savePanel.add(applyBtn);

        // Add all the buttons to the filter panel that will be the right part.
        filterPanel.add(mimeTypeFilter);
        filterPanel.add(sizeFilterPanel);
        filterPanel.add(extensionFilterPanel);
        // Add vertical space before the save panel.
        filterPanel.add(Box.createVerticalStrut(10));
        filterPanel.add(savePanel);

        // Add rightPanel to the EAST position.
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.1;
        gbc.weighty = 0.1;
        this.add(filterPanel, gbc);

        this.setPreferredSize(new Dimension(900, 450));

        // Read the value of SETTINGS_KEY and load any data (if exists).
        String json = Utils.getKey(Constants.SETTINGS_KEY);
        if (json != null) {
            try {
                ExtensionSettings extensionSettings = ExtensionSettings.fromJson(json);
                if (extensionSettings != null) {
                    this.loadSettings(extensionSettings);
                }
            } catch (Exception e) {
                Log.toError("Extension settings was corrupted: " + e.getMessage());
                // ZZZ: Load the default setting.
            }
        } else {
            // ZZZ: Add default settings like Burp.
            Log.toOutput("No extension settings found. Using default values.");
        }

        // Pack it all up.
        this.pack();
        // Issue: The frame shows big tables and the size becomes correct after
        // resizing it. Adding these two lines to fix it?
        this.invalidate();
        this.repaint();
    }

    public void display() {
        this.setLocationRelativeTo(Utils.burpFrame());
        this.setVisible(true);
    }

    public void save() {
        ExtensionSettings settings = new ExtensionSettings();

        // Store include and exclude table data.
        settings.setIncludeTableData(this.include.getData());
        settings.setExcludeTableData(this.exclude.getData());

        // Store MIME type filter states.
        boolean[] mimeTypeStates = new boolean[MIME_LABELS.length];
        for (int i = 0; i < MIME_LABELS.length; i++) {
            mimeTypeStates[i] = this.mimeTypeCheckBoxes[i].isSelected();
        }
        settings.setMimeTypes(mimeTypeStates);

        // Store filters size, show/hide file extensions.
        settings.setSizeStatus(this.sizeCheckBox.isSelected());
        settings.setSizeValue(this.sizeTextField.getText());
        settings.setShowStatus(this.showCheckBox.isSelected());
        settings.setShowValue(this.showTextField.getText());
        settings.setHideStatus(this.hideCheckBox.isSelected());
        settings.setHideValue(this.hideTextField.getText());

        // Convert it to json.
        String json = "";
        try {
            json = settings.toJson();
        } catch (Exception e) {
            Log.toError("Error converting extension settings to JSON: " + e.getMessage());
            return;
        }
        // Store it in the settings key.
        Utils.setKey(Constants.SETTINGS_KEY, json);
    }

    // Loads the settings data into the Settings Dialog.
    private void loadSettings(ExtensionSettings settings) {
        // Load the include and exclude table data.
        this.include.setData(settings.getIncludeTableData());
        this.exclude.setData(settings.getExcludeTableData());
        // Load the MIME type filter states.
        boolean[] mimeTypeStates = settings.getMimeTypes();
        for (int i = 0; i < MIME_LABELS.length; i++) {
            this.mimeTypeCheckBoxes[i].setSelected(mimeTypeStates[i]);
        }
        // Load the filters size, show/hide file extensions.
        this.sizeCheckBox.setSelected(settings.isSizeStatus());
        this.sizeTextField.setText(settings.getSizeValue());
        this.showCheckBox.setSelected(settings.isShowStatus());
        this.showTextField.setText(settings.getShowValue());
        this.hideCheckBox.setSelected(settings.isHideStatus());
        this.hideTextField.setText(settings.getHideValue());
    }
}