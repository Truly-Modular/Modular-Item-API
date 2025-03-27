package smartin.miapi.editor.syntax;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.editor.MiapiEditor;
import smartin.miapi.editor.registry.PropertyRegistryViewer;
import smartin.miapi.modules.properties.util.EditorError;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.modules.properties.util.Validator;
import smartin.miapi.registries.RegistryInventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PropertyMapHighlighter implements EditorInterface {
    public static final ResourceLocation id = Miapi.id("miapi:property_map");
    private ResourceLocation modulePath;

    public PropertyMapHighlighter() {
        this(Miapi.id("runtime_editor_property_checker"));
    }

    public PropertyMapHighlighter(ResourceLocation modulePath) {
        this.modulePath = modulePath;
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

        return getEditorErrors(rawContent, moduleJson);
    }

    public Map<String, Runnable> toolbarButtons() {
        return Map.of("Property Registry", () -> {
            PropertyRegistryViewer viewer = new PropertyRegistryViewer();
            MiapiEditor.editors.add(viewer);
        });
    }

    public static List<EditorError> getEditorErrors(String rawContent, JsonObject moduleJson) {
        List<EditorError> errors = new ArrayList<>();
        Map<String, JsonElement> rawProperties = moduleJson.asMap();
        rawProperties.forEach((key, data) -> {
            int line = getLineNumber(rawContent, key);
            ResourceLocation id = Miapi.id(key);
            ModuleProperty<?> property = RegistryInventory.moduleProperties.get(id);

            if (property == null) {
                errors.add(new EditorError(
                        line,
                        "Invalid property '" + key + "'. This indicates either a broken Module, Outdated API version or missing dependency!",
                        EditorError.ErrorSeverity.ERROR
                ));
                return;
            }

            try {
                boolean valid = property.load(Miapi.id(key), data, true);
                if (!valid) {
                    errors.add(new EditorError(
                            line,
                            "Property was not loaded '" + key + "' this is usually due to the property deactivating itself when requirements arent met like for compat properties.",
                            EditorError.ErrorSeverity.WARNING
                    ));
                } else {
                    if (property instanceof Validator validator) {
                        errors.addAll(validator.validate(line, property.decode(data), true));
                    }
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

    private static int getLineNumber(String content, String searchText) {
        String[] lines = content.split("\n");
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].contains("\"" + searchText + "\"")) {
                return i + 1;
            }
        }
        return 1;
    }
} 