package smartin.miapi.editor.material;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import imgui.ImGui;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.editor.util.JsonEditor;
import smartin.miapi.material.codec.CodecMaterial;

import java.util.Optional;

public class IconEditor {
    private final CodecMaterial material;
    private final JsonEditor jsonEditor;

    public IconEditor(CodecMaterial material) {
        this.material = material;
        JsonElement initialValue = material.iconJson.orElse(new JsonPrimitive(""));
        this.jsonEditor = new JsonEditor("Icon", initialValue);
    }

    public void render() {
        jsonEditor.render();
        try {
            JsonElement element = jsonEditor.getValue();
            if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
                String value = element.getAsString();
                if (!value.isEmpty()) {
                    try {
                        ResourceLocation.parse(value);
                        material.iconJson = Optional.of(element);
                    } catch (Exception e) {
                        ImGui.text("Invalid resource location!");
                    }
                } else {
                    material.iconJson = Optional.empty();
                }
            } else {
                material.iconJson = Optional.of(element);
            }
        } catch (Exception e) {
            ImGui.text("Failed to parse icon: " + e.getMessage());
        }
    }
} 