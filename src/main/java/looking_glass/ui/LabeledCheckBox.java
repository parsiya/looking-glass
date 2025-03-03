package looking_glass.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LabeledCheckBox extends JPanel {
    private JCheckBox checkBox;
    private JLabel label;

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
                checkBox.setSelected(!checkBox.isSelected());
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

    public void addActionListener(ActionListener actionListener) {
        checkBox.addActionListener(actionListener);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        checkBox.setEnabled(enabled);
        label.setEnabled(enabled);
    }
}