package looking_glass;

import java.util.Vector;

import javax.swing.table.DefaultTableModel;

import looking_glass.common.Utils;

public class ExtensionSettings {

    private boolean sizeStatus;
    private boolean showStatus;
    private boolean hideStatus;
    private String sizeValue;
    private String showValue;
    private String hideValue;
    private boolean[] mimeTypes;
    private Vector<Vector> includeTableData;
    private Vector<Vector> excludeTableData;

    public boolean isSizeStatus() {
        return sizeStatus;
    }

    public void setSizeStatus(boolean sizeStatus) {
        this.sizeStatus = sizeStatus;
    }

    public boolean isShowStatus() {
        return showStatus;
    }

    public void setShowStatus(boolean showStatus) {
        this.showStatus = showStatus;
    }

    public boolean isHideStatus() {
        return hideStatus;
    }

    public void setHideStatus(boolean hideStatus) {
        this.hideStatus = hideStatus;
    }

    public String getSizeValue() {
        return sizeValue;
    }

    public void setSizeValue(String sizeValue) {
        this.sizeValue = sizeValue;
    }

    public String getShowValue() {
        return showValue;
    }

    public void setShowValue(String showValue) {
        this.showValue = showValue;
    }

    public String getHideValue() {
        return hideValue;
    }

    public void setHideValue(String hideValue) {
        this.hideValue = hideValue;
    }

    public boolean[] getMimeTypes() {
        return mimeTypes;
    }

    public void setMimeTypes(boolean[] mimeTypes) {
        this.mimeTypes = mimeTypes;
    }

    public Vector<Vector> getIncludeTableData() {
        return includeTableData;
    }

    public void setIncludeTableData(Vector<Vector> includeTableData) {
        this.includeTableData = includeTableData;
    }

    public Vector<Vector> getExcludeTableData() {
        return excludeTableData;
    }

    public void setExcludeTableData(Vector<Vector> excludeTableData) {
        this.excludeTableData = excludeTableData;
    }

    public String toJson() throws Exception {
        return Utils.toJson(this);
    }

    public static ExtensionSettings fromJson(String json) throws Exception {
        return Utils.fromJson(json, ExtensionSettings.class);
    }
}