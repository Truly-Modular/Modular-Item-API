package smartin.miapi.editor.syntax;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.util.EditorError;

import java.util.*;

public class CompositeEditor implements EditorInterface {

    private final List<Entry> editors = new ArrayList<>();

    private record Entry(String path, boolean required, EditorInterface editor) {}

    public void addEditor(String path, boolean required, EditorInterface editor) {
        editors.add(new Entry(path, required, editor));
    }

    @Override
    public ResourceLocation getId() {
        return Miapi.id("composite_editor");
    }

    @Override
    public List<EditorError> validateContent(@Nullable JsonElement json, String rawContent, int offset) {
        List<EditorError> errors = new ArrayList<>();

        if (!(json instanceof JsonObject root)) {
            errors.add(new EditorError(
                    offset,
                    "Expected root JSON object",
                    EditorError.ErrorSeverity.ERROR
            ));
            return errors;
        }

        for (Entry entry : editors) {
            JsonElement sub = resolvePath(root, entry.path);

            if (sub == null) {
                if (entry.required) {
                    int line = findLine(rawContent, entry.path);
                    errors.add(new EditorError(
                            line + offset,
                            "Missing required field: " + entry.path,
                            EditorError.ErrorSeverity.ERROR
                    ));
                }
                continue;
            }

            int subOffset = findLine(rawContent, entry.path);

            errors.addAll(entry.editor.validateContent(
                    sub,
                    rawContent,
                    offset + subOffset
            ));
        }

        return errors;
    }

    @Override
    public Map<TextRange, Integer> getSyntaxHighlighting(String content) {
        Map<TextRange, Integer> result = new HashMap<>();

        for (Entry entry : editors) {
            result.putAll(entry.editor.getSyntaxHighlighting(content));
        }

        return result;
    }

    @Override
    public Map<String, Runnable> toolbarButtons() {
        Map<String, Runnable> buttons = new LinkedHashMap<>();
        for (Entry entry : editors) {
            buttons.putAll(entry.editor.toolbarButtons());
        }
        return buttons;
    }

    // ----------------------------
    // helpers
    // ----------------------------

    private JsonElement resolvePath(JsonObject root, String path) {
        String[] parts = path.split("\\.");
        JsonElement current = root;

        for (String part : parts) {
            if (!(current instanceof JsonObject obj)) return null;
            current = obj.get(part);
            if (current == null) return null;
        }

        return current;
    }

    private int findLine(String content, String key) {
        String[] lines = content.split("\n");
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].contains("\"" + key + "\"")) {
                return i + 1;
            }
        }
        return 1;
    }
}