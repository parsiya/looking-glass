package looking_glass.message;

import java.util.Vector;

import burp.api.montoya.http.message.MimeType;
import looking_glass.ExtensionSettings;

public class Filter {
    // We will use the settings to filter request and responses.
    private ExtensionSettings settings;

    public Filter(ExtensionSettings settings) {
        this.settings = settings;
    }

    public ExtensionSettings getSettings() {
        return settings;
    }

    public void setSettings(ExtensionSettings settings) {
        this.settings = settings;
    }

    // Check if the request host matches the include or exclude list.
    // 1. Check if the include list is empty.
    // 2. If not, check if the host is in the include list.
    // 2.1 If the host is in the include list, return true.
    // 2.2 If the host is not in the include list, return false.
    // 3. If the include list is not empty, check the exclude list.
    // 3.1 If the host is in the exclude list, return false.
    // 3.2 If the host is not in the exclude list, return true.
    // 4. If both lists are empty, return true.
    public boolean hostMatches(Request req) {

        // 1. Check if the include list is empty.
        if (settings.includeTableData != null || settings.includeTableData.size() > 0) {
            // 2. If not, check if the host is in the include list.
            for (Vector<String> row : settings.includeTableData) {
                // This checks for subdomains.
                if (req.host.endsWith(row.get(0))) {
                    return true;
                }
            }
            // 2.2 If the host is not in the include list, return false because
            // if include is not empty, we will exclude everything not in it.
            return false;
        }

        // 3. If the include list is not empty, check the exclude list.
        if (settings.excludeTableData != null || settings.excludeTableData.size() > 0) {
            for (Vector<String> row : settings.excludeTableData) {
                if (req.host.endsWith(row.get(0))) {
                    // 3.1 If the host is in the exclude list, return false.
                    return false;
                }
            }
            // 3.2 If the host is not in the exclude list, return true. This is
            // redundant, because we're returning true after the if.
            // return true;
        }
        // 4. If both lists are empty, return true.
        return true;
    }

    // Returns true if we can store the body of the request.
    // 1. Check if body size checkbox was checked.
    // 1.1 If true, check if the body size is larger than the value in MB.
    // 2. Check if the store file extension checkbox was checked.
    // 2.1 If true, check if the file extension is in the store file.
    // 2.1.1 Return true if the file extension is in the store file.
    // 2.1.2 Otherwise, return false.
    // 3. Check if the skip file extension checkbox was checked.
    // 3.1 If true, check if the file extension is in the skip file.
    // 3.1.1 Return false if the file extension is in the skip file.
    // 3.1.2 Otherwise, return true.
    // 4. Return true if we get here.
    public boolean storeBody(Request req) {

        // 1. Check if body size checkbox was checked.
        if (settings.bodySizeStatus) {
            // 1.1 If true, check if the body size is larger than the value in MB.
            if (req.body.length() > settings.bodySizeValue * 1024 * 1024) {
                return false;
            }
        }

        // 2. Check if the store file extension checkbox was checked.
        if (settings.storeFileExtensionStatus) {
            // 2.1 If true, check if the file extension is in the store file.
            for (String ext : settings.storeFileExtensions.split(",")) {
                if (req.url.endsWith(ext.trim())) {
                    // 2.1.1 Return true if the file extension is in the store file.
                    return true;
                }
            }
            // 2.1.2 Otherwise, return false.
            return false;
        }

        // 3. Check if the skip file extension checkbox was checked.
        // We're checking this after the store file extension checkbox because
        // store has priority over skip. The UI should not allow both of the
        // checkboxes to be true, but we're not assuming.
        if (settings.skipFileExtensionStatus) {
            // 3.1 If true, check if the file extension is in the skip file.
            for (String ext : settings.hideFileExtensions.split(",")) {
                // 3.1.1 Return false if the file extension is in the skip file.
                if (req.url.endsWith(ext.trim())) {
                    return false;
                }
            }
            // 3.1.2 Otherwise, return true.
            // return true; // this is redundant, we're returning true next.
        }
        // 4. Return true if we get here.
        return true;
    }

    public boolean storeBody(Response resp) {
        // 1. Check if body size checkbox was checked.
        if (settings.bodySizeStatus) {
            // 1.1 If true, check if the body size is larger than the value in MB.
            if (resp.body.length() > settings.bodySizeValue * 1024 * 1024) {
                return false;
            }
        }
        // Response doesn't have a URL so we do not need to check the URL in the
        // store file extension checkbox.

        // Check the response Mimetype against the settings.
        return mimeTypeFilter(resp);
    }

    public boolean mimeTypeFilter(Response resp) {
        MimeType mt = resp.burpMimeType;

        // ZZ Add this to the readme
        // By converting the Burp's proxy filter to Bambda mode, I've figure out
        // which MimeTypes are allowed by each checkbox. Instead of checking
        // against specific MimeTypes, it checks whether it's one of the
        // unchecked ones.
        //
        // There are 25 possible types from Burp and if we remove all the
        // checkboxes, we only check if it's not one of the 22.
        //
        // These three are always allowed: MimeType.NONE, MimeType.UNRECOGNIZED,
        // MimeType.AMBIGUOUS.
        //
        // The rest are in this format:
        // [index in settings.mimeTypes]. [name of the checkbox] -> affected mimetype(s)
        //
        // 0. HTML -> MimeType.HTML
        // 1. Other text -> MimeType.PLAIN_TEXT, MimeType.RTF
        // 2. Script -> MimeType.SCRIPT, MimeType.JSON
        // 3. Images -> IMAGE_UNKNOWN, IMAGE_JPEG, IMAGE_GIF, IMAGE_PNG, IMAGE_BMP,
        // IMAGE_TIFF
        // 4. XML -> XML, IMAGE_SVG_XML
        // 5. Flash -> APPLICATION_FLASH, LEGACY_SER_AMF
        // 6. CSS -> CSS
        // 7. Other binary -> UNRECOGNIZED, SOUND, VIDEO, FONT_WOFF, FONT_WOFF2,
        // APPLICATION_UNKNOWN

        // First we check if it's one of the three that should always go through.
        if (mt == MimeType.NONE || mt == MimeType.UNRECOGNIZED
                || mt == MimeType.AMBIGUOUS) {
            return true;
        }

        // Next we will check if settings.mimeTypes is true or not, if true, we
        // will only allow those mimeTypes, otherwise, we will ignore it.
        if (settings.mimeTypes[0] && mt == MimeType.HTML) {
            return true;
        }
        if (settings.mimeTypes[1] && (mt == MimeType.PLAIN_TEXT || mt == MimeType.RTF)) {
            return true;
        }
        if (settings.mimeTypes[2] && (mt == MimeType.SCRIPT || mt == MimeType.JSON)) {
            return true;
        }
        if (settings.mimeTypes[3] && (mt == MimeType.IMAGE_UNKNOWN || mt == MimeType.IMAGE_JPEG
                || mt == MimeType.IMAGE_GIF || mt == MimeType.IMAGE_PNG
                || mt == MimeType.IMAGE_BMP || mt == MimeType.IMAGE_TIFF)) {
            return true;
        }
        if (settings.mimeTypes[4] && (mt == MimeType.XML || mt == MimeType.IMAGE_SVG_XML)) {
            return true;
        }
        if (settings.mimeTypes[5] && (mt == MimeType.APPLICATION_FLASH || mt == MimeType.LEGACY_SER_AMF)) {
            return true;
        }
        if (settings.mimeTypes[6] && mt == MimeType.CSS) {
            return true;
        }
        if (settings.mimeTypes[7] && (mt == MimeType.SOUND || mt == MimeType.VIDEO
                || mt == MimeType.FONT_WOFF || mt == MimeType.FONT_WOFF2
                || mt == MimeType.APPLICATION_UNKNOWN)) {
            return true;
        }

        // If none of the conditions matched, return false
        return false;
    }
}
