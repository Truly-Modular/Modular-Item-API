package smartin.miapi.editor.syntax;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.modules.properties.util.EditorError;

import java.util.List;
import java.util.Map;

public interface EditorInterface {
    /**
     * Called when the content of the editor changes
     *
     * @param json       The current JSON content, null if invalid
     * @param rawContent The raw text content
     * @return List of errors, empty if none
     */
    List<EditorError> validateContent(@Nullable JsonElement json, String rawContent);

    /**
     * Get syntax highlighting for specific parts of the text
     *
     * @param content The current text content
     * @return Map of character ranges to colors (RGBA format)
     */
    Map<TextRange, Integer> getSyntaxHighlighting(String content);

    /**
     * Get the identifier for this interface
     */
    ResourceLocation getId();

    /**
     * A List of Clickable Toolbar Buttons
     */
    default Map<String, Runnable> toolbarButtons() {
        return Map.of();
    }

    /**
     * Record for representing a range of text
     */
    record TextRange(int start, int end) {
    }

}