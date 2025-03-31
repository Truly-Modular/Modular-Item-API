package smartin.miapi.editor.syntax;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.material.CodecMaterial;
import smartin.miapi.modules.properties.util.EditorError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CodecMaterialEditorInterface implements EditorInterface {
    private final ResourceLocation id;

    public CodecMaterialEditorInterface() {
        this.id = Miapi.id("material_editor");
    }

    @Override
    public List<EditorError> validateContent(@Nullable JsonElement json, String rawContent) {
        List<EditorError> errors = new ArrayList<>();

        if (json == null) {
            errors.add(new EditorError(1, "Invalid JSON", EditorError.ErrorSeverity.ERROR));
            return errors;
        }

        try {
            var result = CodecMaterial.CODEC.decode(
                    com.mojang.serialization.JsonOps.INSTANCE,
                    CodecMaterial.CODEC.encodeStart(com.mojang.serialization.JsonOps.INSTANCE, new CodecMaterial(
                            Optional.empty(),
                            Optional.empty(),
                            Optional.empty(),
                            new ArrayList<>(),
                            new ArrayList<>(),
                            new ArrayList<>(),
                            new HashMap<>(),
                            new HashMap<>(),
                            new HashMap<>(),
                            List.of("default"),
                            Optional.empty(),
                            Optional.empty(),
                            new ArrayList<>(),
                            Optional.empty()
                    )).getOrThrow()).result();

            if (result.isEmpty()) {
                errors.add(new EditorError(1, "Failed to decode material: Invalid format", EditorError.ErrorSeverity.ERROR));
            }
        } catch (Exception e) {
            errors.add(new EditorError(1, "Failed to validate material: " + e.getMessage(), EditorError.ErrorSeverity.ERROR));
        }

        return errors;
    }

    @Override
    public Map<TextRange, Integer> getSyntaxHighlighting(String content) {
        Map<TextRange, Integer> highlights = new HashMap<>();

        // Basic JSON syntax highlighting
        // Key highlighting (blue)
        int keyStart = -1;
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '"' && (i == 0 || content.charAt(i - 1) != '\\')) {
                if (keyStart == -1) {
                    keyStart = i;
                } else {
                    highlights.put(new TextRange(keyStart, i + 1), 0xFF0000FF); // Blue
                    keyStart = -1;
                }
            }
        }

        // Value highlighting (green for strings, yellow for numbers)
        int valueStart = -1;
        boolean isString = false;
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '"' && (i == 0 || content.charAt(i - 1) != '\\')) {
                if (valueStart == -1) {
                    valueStart = i;
                    isString = true;
                } else {
                    highlights.put(new TextRange(valueStart, i + 1), 0xFF00FF00); // Green
                    valueStart = -1;
                }
            } else if (Character.isDigit(c) && valueStart == -1) {
                valueStart = i;
                isString = false;
            } else if (!Character.isDigit(c) && valueStart != -1 && !isString) {
                highlights.put(new TextRange(valueStart, i), 0xFFFFFF00); // Yellow
                valueStart = -1;
            }
        }

        return highlights;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }
} 