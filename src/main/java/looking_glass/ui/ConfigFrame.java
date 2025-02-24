package looking_glass.ui;

import javax.swing.*;
import java.awt.*;

public class ConfigFrame extends JFrame {

    public ConfigFrame() {
        super("My Frame");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JPanel groupBoxPanel = new JPanel();
        groupBoxPanel.setBorder(BorderFactory.createTitledBorder("AA"));
        groupBoxPanel.setLayout(new BoxLayout(groupBoxPanel, BoxLayout.Y_AXIS));

        TableEditor tableEditor1 = new TableEditor("Table 1", "Column 1");
        TableEditor tableEditor2 = new TableEditor("Table 2", "Column 2");

        groupBoxPanel.add(tableEditor1);
        groupBoxPanel.add(tableEditor2);

        panel.add(groupBoxPanel, BorderLayout.CENTER);

        add(panel);
        pack();
    }

    public void show() {
        SwingUtilities.invokeLater(() -> {
            setVisible(true);
        });
    }
}