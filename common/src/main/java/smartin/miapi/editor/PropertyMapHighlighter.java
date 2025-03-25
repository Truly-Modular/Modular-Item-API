package smartin.miapi.editor;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.registries.RegistryInventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PropertyMapHighlighter implements EditorInterface {
    public static final ResourceLocation id = Miapi.id("miapi:property_map");
    private final ResourceLocation modulePath;
    private final boolean isClient;

    public PropertyMapHighlighter() {
        this(Miapi.id("runtime_editor_property_checker"), true);
    }

    public PropertyMapHighlighter(ResourceLocation modulePath, boolean isClient) {
        this.modulePath = modulePath;
        this.isClient = isClient;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public List<EditorError> validateContent(JsonElement json, String rawContent) {
        List<EditorError> errors = new ArrayList<>();

        if (!(json instanceof JsonObject moduleJson)) {
            errors.add(new EditorError(1, "Expected a JSON object", EditorError.ErrorSeverity.ERROR));
            return errors;
        }

        Map<String, JsonElement> rawProperties = moduleJson.asMap();
        rawProperties.forEach((key, data) -> {
            int line = getLineNumber(rawContent, key);
            ModuleProperty<?> property = RegistryInventory.moduleProperties.get(Miapi.id(key));

            if (property == null) {
                errors.add(new EditorError(
                        line,
                        "Invalid property '" + key + "'. This indicates either a broken Module, Outdated API version or missing dependency!",
                        EditorError.ErrorSeverity.ERROR
                ));
                return;
            }

            try {
                boolean valid = property.load(Miapi.id(key), data, isClient);
                if (!valid) {
                    errors.add(new EditorError(
                            line,
                            "Property was not loaded '" + key + "' this is usually due to the property deactivating itself when requirements arent met like for compat properties.",
                            EditorError.ErrorSeverity.WARNING
                    ));
                }
            } catch (Exception e) {
                errors.add(new EditorError(
                        line,
                        "Failed to load property '" + key + "': " + e.getLocalizedMessage(),
                        EditorError.ErrorSeverity.ERROR
                ));
            }
        });

        return errors;
    }

    @Override
    public Map<TextRange, Integer> getSyntaxHighlighting(String content) {
        // We'll use the same highlighting as JsonSyntaxHighlighter for now
        return new HashMap<>();
    }

    private int getLineNumber(String content, String searchText) {
        String[] lines = content.split("\n");
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].contains("\"" + searchText + "\"")) {
                return i + 1;
            }
        }
        return 1;
    }
} 