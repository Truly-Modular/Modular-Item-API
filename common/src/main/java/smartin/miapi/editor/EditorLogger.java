package smartin.miapi.editor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logger for Miapi Editor module.
 * Use this logger throughout the editor codebase.
 */
public final class EditorLogger {
    private static final Logger LOGGER = LoggerFactory.getLogger("smartin.miapi.editor");
    
    private EditorLogger() {
        // Prevent instantiation
    }
    
    /**
     * Get the logger instance.
     * @return The logger instance
     */
    public static Logger getLogger() {
        return LOGGER;
    }
}
