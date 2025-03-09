package looking_glass;

import java.util.Arrays;
import java.util.Vector;

import looking_glass.common.Utils;

public class ExtensionSettings {

    public boolean bodySizeStatus;
    public boolean storeFileExtensionStatus;
    public boolean skipFileExtensionStatus;
    public int bodySizeValue;
    public String storeFileExtensions;
    public String hideFileExtensions;
    public boolean[] mimeTypes;
    public Vector<Vector> includeTableData;
    public Vector<Vector> excludeTableData;

    public String toJson() throws Exception {
        return Utils.toJson(this);
    }

    public ExtensionSettings(String json) throws Exception {
        ExtensionSettings settings = Utils.fromJson(json, ExtensionSettings.class);
        this.bodySizeStatus = settings.bodySizeStatus;
        this.storeFileExtensionStatus = settings.storeFileExtensionStatus;
        this.skipFileExtensionStatus = settings.skipFileExtensionStatus;
        this.bodySizeValue = settings.bodySizeValue;
        this.storeFileExtensions = settings.storeFileExtensions;
        this.hideFileExtensions = settings.hideFileExtensions;
        this.mimeTypes = settings.mimeTypes;
        this.includeTableData = settings.includeTableData;
        this.excludeTableData = settings.excludeTableData;
    }

    public ExtensionSettings() {
    }

    // Returns a default settings object.
    public static ExtensionSettings getDefault() {
        ExtensionSettings settings = new ExtensionSettings();
        settings.bodySizeStatus = false;
        settings.bodySizeValue = 5;

        settings.storeFileExtensionStatus = false;
        settings.storeFileExtensions = "";

        settings.skipFileExtensionStatus = true;
        settings.hideFileExtensions = "js,gif,jpg,png,css,woff,woff2,mp3,wav,ogg,aac,flac,mp4,avi,mov,wmv,mkv,bmp,tiff,svg,ico,ttf,otf,eot,zip,rar,7z,gz,bz2,exe,dll,msi";;

        settings.mimeTypes = new boolean[8];
        Arrays.fill(settings.mimeTypes, true); // Set everything to true.

        settings.includeTableData = new Vector<>();
        settings.excludeTableData = new Vector<>();
        return settings;
    }
}