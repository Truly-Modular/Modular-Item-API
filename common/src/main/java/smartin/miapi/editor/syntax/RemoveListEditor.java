package smartin.miapi.editor.syntax;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.util.EditorError;
import smartin.miapi.registries.RegistryInventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RemoveListEditor implements EditorInterface {

    public static final ResourceLocation ID = Miapi.id("remove_list_editor");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public List<EditorError> validateContent(@Nullable JsonElement json, String rawContent, int offset) {
        List<EditorError> errors = new ArrayList<>();

        if (json == null) {
            return errors;
        }

        if (!(json instanceof JsonArray array)) {
            errors.add(new EditorError(
                    offset,
                    "'remove' must be an array",
                    EditorError.ErrorSeverity.ERROR
            ));
            return errors;
        }

        for (JsonElement element : array) {
            if (!element.isJsonPrimitive()) {
                errors.add(new EditorError(
                        offset,
                        "Entries in 'remove' must be strings",
                        EditorError.ErrorSeverity.ERROR
                ));
                continue;
            }

            String key = element.getAsString();
            ResourceLocation id = Miapi.id(key);

            if (!RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY.containsKey(id)) {
                errors.add(new EditorError(
                        offset,
                        "Unknown property in remove: " + key,
                        EditorError.ErrorSeverity.ERROR
                ));
            }
        }

        return errors;
    }

    @Override
    public Map<TextRange, Integer> getSyntaxHighlighting(String content) {
        return Map.of();
    }
}