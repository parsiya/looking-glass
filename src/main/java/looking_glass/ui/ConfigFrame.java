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

import looking_glass.ConfigData;
import looking_glass.common.Constants;
import looking_glass.common.Log;
import looking_glass.common.Utils;

public class ConfigFrame extends JDialog {

    // Labels
    // ---------
    // Config frame name.
    private static final String CONFIG_FRAME_NAME = "Looking Glass Configuration"; // ZZZ change this.
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

    private TableEditor include, exclude;
    private JTextField showTextField, hideTextField, sizeTextField;
    private LabeledCheckBox sizeCheckBox, showCheckBox, hideCheckBox;
    private LabeledCheckBox[] mimeTypeCheckBoxes;

    // We want the extension to only create one instance of this frame.
    private static ConfigFrame instance;

    public static ConfigFrame getInstance() {
        if (instance == null) {
            instance = new ConfigFrame();
        }
        return instance;
    }

    private ConfigFrame() {
        super((Frame) Utils.burpFrame(), CONFIG_FRAME_NAME);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        // Use GridBagLayout for better control.
        this.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // This is simulating a group box to hold the scope. This is designed to
        // look like the Burp `Target > Scope` tab.
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
        // Add scopePanel to the WEST position
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

        // Create a wrapper panel with FlowLayout
        JPanel flowWrapper = new JPanel();
        // flowWrapper.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 10));

        // Create a panel with GridLayout
        JPanel gridPanel = new JPanel();
        gridPanel.setLayout(new GridLayout(4, 2, 10, 10));

        // Add 8 elements to the grid panel
        mimeTypeCheckBoxes = new LabeledCheckBox[MIME_LABELS.length];
        for (int i = 0; i < MIME_LABELS.length; i++) {
            mimeTypeCheckBoxes[i] = new LabeledCheckBox(MIME_LABELS[i]);
            // Add the checkbox to your form
            gridPanel.add(mimeTypeCheckBoxes[i]);
        }

        // Add the grid panel to the flow wrapper
        flowWrapper.add(gridPanel);

        // Use the flow wrapper as the main panel
        mimeTypeFilter.add(flowWrapper, BorderLayout.WEST);

        // Skip body by size.
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
        // This is to make the label do the same.
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

        // Add them to the panel.
        extensionFilterPanel.add(showCheckBox);
        extensionFilterPanel.add(showTextField);
        extensionFilterPanel.add(hideCheckBox);
        extensionFilterPanel.add(hideTextField);

        // Save panel
        JPanel savePanel = new JPanel();
        savePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));

        // Cancel button
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> {
            // Close the form.
            this.dispose();
        });

        // Save and close button
        JButton applyBtn = new JButton("Apply & Close");
        applyBtn.setBackground(new Color(255, 102, 51));
        applyBtn.setForeground(Color.WHITE);
        applyBtn.setFont(applyBtn.getFont().deriveFont(Font.BOLD));
        applyBtn.addActionListener(e -> {
            Log.toOutput("Apply button clicked.");
            // Save the config and hide the form.
            this.save();
            this.dispose();
        });

        // Add buttons to the save panel
        savePanel.add(cancelButton);
        savePanel.add(applyBtn);

        filterPanel.add(mimeTypeFilter);
        filterPanel.add(sizeFilterPanel);
        filterPanel.add(extensionFilterPanel);
        // Add vertical space before the save panel
        filterPanel.add(Box.createVerticalStrut(10));
        filterPanel.add(savePanel);

        // Add rightPanel to the EAST position
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.1;
        gbc.weighty = 0.1;
        this.add(filterPanel, gbc);

        this.setPreferredSize(new Dimension(900, 450));

        // Read the value of CONFIG_KEY from the config and if it exists, load it.
        String json = Utils.getKey(Constants.CONFIG_KEY);
        if (json != null) {
            try {
                ConfigData configData = ConfigData.fromJson(json);
                if (configData != null) {
                    this.loadConfigData(configData);
                }
            } catch (Exception e) {
                Log.toError("Config was corrupted: " + e.getMessage());
                // ZZZ: Load the default config.
            }
        } else {
            // ZZZ: Add default config like Burp.
            Log.toOutput("No config found. Using default values.");
        }

        // Pack it all up.
        this.pack();
        // Issue: The frame shows big tables and the size becomes correct after
        // resizing it. Adding these two lines to fix it?
        this.invalidate();
        this.repaint();
    }

    public void display() {
        this.setVisible(true);
    }

    public void save() {
        Log.toOutput("Inside save");
        ConfigData configData = new ConfigData();

        // Store include and exclude table data.
        configData.setIncludeTableData(this.include.getData());
        configData.setExcludeTableData(this.exclude.getData());

        // Store MIME type filter states.
        boolean[] mimeTypeStates = new boolean[MIME_LABELS.length];
        for (int i = 0; i < MIME_LABELS.length; i++) {
            mimeTypeStates[i] = this.mimeTypeCheckBoxes[i].isSelected();
        }
        configData.setMimeTypes(mimeTypeStates);

        // Store filters size, show/hide file extensions.
        configData.setSizeStatus(this.sizeCheckBox.isSelected());
        configData.setSizeValue(this.sizeTextField.getText());
        configData.setShowStatus(this.showCheckBox.isSelected());
        configData.setShowValue(this.showTextField.getText());
        configData.setHideStatus(this.hideCheckBox.isSelected());
        configData.setHideValue(this.hideTextField.getText());

        Log.toOutput("before toJson");
        // Convert configData to json.
        String json = "";
        try {
            json = configData.toJson();
        } catch (Exception e) {
            Log.toError("Error converting config data to JSON: " + e.getMessage());
        }
        Log.toOutput("after toJson");
        // Store it in the config key.
        Utils.setKey(Constants.CONFIG_KEY, json);
        Log.toOutput(json);
    }

    private void loadConfigData(ConfigData configData) {
        // Load the include and exclude table data.
        this.include.setData(configData.getIncludeTableData());
        this.exclude.setData(configData.getExcludeTableData());
        // Load the MIME type filter states.
        boolean[] mimeTypeStates = configData.getMimeTypes();
        for (int i = 0; i < MIME_LABELS.length; i++) {
            this.mimeTypeCheckBoxes[i].setSelected(mimeTypeStates[i]);
        }
        // Load the filters size, show/hide file extensions.
        this.sizeCheckBox.setSelected(configData.isSizeStatus());
        this.sizeTextField.setText(configData.getSizeValue());
        this.showCheckBox.setSelected(configData.isShowStatus());
        this.showTextField.setText(configData.getShowValue());
        this.hideCheckBox.setSelected(configData.isHideStatus());
        this.hideTextField.setText(configData.getHideValue());
    }
}