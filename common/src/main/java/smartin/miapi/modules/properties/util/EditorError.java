package smartin.miapi.modules.properties.util;

/**
 * Record for representing an error in the editor
 */
public record EditorError(int line, String message, ErrorSeverity severity) {
    public enum ErrorSeverity {
        ERROR,
        WARNING,
        INFO
    }
}
