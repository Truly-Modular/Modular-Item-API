package smartin.miapi.editor.syntax;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.modules.conditions.ConditionManager;
import smartin.miapi.modules.properties.util.EditorError;
import smartin.miapi.registries.JsonOpsBooleanPatched;

import java.util.*;

public class ConditionEditor implements EditorInterface {

    public static final ResourceLocation ID = Miapi.id("condition_editor");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public List<EditorError> validateContent(@Nullable JsonElement json, String rawContent, int offset) {
        List<EditorError> errors = new ArrayList<>();

        if (json == null) {
            errors.add(new EditorError(
                    offset,
                    "Invalid JSON",
                    EditorError.ErrorSeverity.ERROR
            ));
            return errors;
        }

        if (!(json instanceof JsonObject obj)) {
            errors.add(new EditorError(
                    offset,
                    "Condition must be a JSON object",
                    EditorError.ErrorSeverity.ERROR
            ));
            return errors;
        }

        // --- validate "type" early for better UX ---
        if (!obj.has("type")) {
            errors.add(new EditorError(
                    findLine(rawContent, "type") + offset,
                    "Missing required field: type",
                    EditorError.ErrorSeverity.ERROR
            ));
            return errors;
        }

        String type = obj.get("type").getAsString();

        if (!ConditionManager.CONDITION_REGISTRY.containsKey(Miapi.id(type))) {
            errors.add(new EditorError(
                    findLine(rawContent, "type") + offset,
                    "Unknown condition type: " + type,
                    EditorError.ErrorSeverity.ERROR
            ));
            return errors;
        }

        // --- full codec validation ---
        DataResult<?> result = ConditionManager.CONDITION_CODEC.parse(JsonOpsBooleanPatched.INSTANCE, json);

        if (result.isError()) {
            String message = result.error().get().message();

            errors.add(new EditorError(
                    findLine(rawContent, "type") + offset,
                    "Condition decode failed: " + message,
                    EditorError.ErrorSeverity.ERROR
            ));
        }

        return errors;
    }

    @Override
    public Map<TextRange, Integer> getSyntaxHighlighting(String content) {
        return new HashMap<>();
    }

    // ----------------------------
    // helpers
    // ----------------------------

    private int findLine(String content, String key) {
        String target = "\"" + key + "\"";
        int index = content.indexOf(target);
        if (index == -1) return 1;
        return content.substring(0, index).split("\n").length;
    }
}