package looking_glass.ui;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

public class LabeledCheckBox extends JPanel {
    private JCheckBox checkBox;
    private JLabel label;

    public JCheckBox getCheckBox() {
        return checkBox;
    }

    public void setCheckBox(JCheckBox checkBox) {
        this.checkBox = checkBox;
    }

    public JLabel getLabel() {
        return label;
    }

    public void setLabel(JLabel label) {
        this.label = label;
    }

    public LabeledCheckBox(String text) {
        setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));

        checkBox = new JCheckBox();
        checkBox.addItemListener(new CheckBoxListener());

        label = new JLabel(text);

        add(checkBox);
        add(label);

        // Toggling the checkbox by clicking the label.
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (checkBox.isEnabled()) {
                    checkBox.setSelected(!checkBox.isSelected());
                }
            }
        });
    }

    private class CheckBoxListener implements ItemListener {
        @Override
        public void itemStateChanged(ItemEvent e) {
            // No changes needed here
        }
    }

    // Returns the value of the checkbox.
    public boolean isSelected() {
        return checkBox.isSelected();
    }

    public void setSelected(boolean selected) {
        checkBox.setSelected(selected);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        checkBox.setEnabled(enabled);
        label.setEnabled(enabled);
    }

    @Override
    public void setToolTipText(String text) {
        // super.setToolTipText(text);
        checkBox.setToolTipText(text);
        label.setToolTipText(text);
    }
}