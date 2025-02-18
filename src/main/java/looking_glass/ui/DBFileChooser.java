package looking_glass.ui;

import java.awt.Component;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

class DBFileChooser {

    private JFileChooser fileChooser;
    
    // Creates the file chooser for DB files.
    public DBFileChooser() {
        fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select the database");
        fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
        FileNameExtensionFilter filter = new FileNameExtensionFilter("DB files", "db");
        fileChooser.setFileFilter(filter);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setFileHidingEnabled(true);
    }

    // Show the file chooser dialog.
    public int show(Component parent) {
        return fileChooser.showSaveDialog(parent);
    }

    // Return the selected file.
    public File getSelectedFile() {
        return fileChooser.getSelectedFile();
    }
}
