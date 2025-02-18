package looking_glass.common;

// Helper functions that make it easier to log. Log.toOutput and Log.toError
// look nicer than Utils.logToOutput and Utils.logToError.
public class Log {
    
    public static void toOutput(String message) {
        Utils.api().logging().logToOutput(message);
    }

    // Log to Error.
    public static void toError(String message) {
        Utils.api().logging().logToError(message);
    }
}
