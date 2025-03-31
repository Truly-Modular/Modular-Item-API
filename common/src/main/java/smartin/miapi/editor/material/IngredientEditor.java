package smartin.miapi.editor.material;

import com.google.gson.JsonArray;
import imgui.ImGui;
import smartin.miapi.blueprint.IngredientWithCount;
import smartin.miapi.editor.util.JsonEditor;
import smartin.miapi.material.CodecMaterial;
import smartin.miapi.modules.properties.util.CodecProperty;

import java.util.ArrayList;
import java.util.List;

public class IngredientEditor {
    private final CodecMaterial material;
    private final JsonEditor jsonEditor;

    public IngredientEditor(CodecMaterial material) {
        this.material = material;
        JsonArray ingredients = new JsonArray();
        material.items.forEach(item -> {
            ingredients.add(IngredientWithCount.CODEC.encodeStart(CodecProperty.getOps(), item).getOrThrow());
        });
        this.jsonEditor = new JsonEditor("Ingredients", ingredients);
    }

    public void render() {
        jsonEditor.render();
        List<IngredientWithCount> ingredient = new ArrayList<>(material.items);
        try {
            JsonArray array = jsonEditor.getValue().getAsJsonArray();
            ingredient.clear();
            array.forEach(element -> {
                ingredient.add(IngredientWithCount.CODEC.decode(CodecProperty.getOps(), element).getOrThrow().getFirst());
            });
            material.items = ingredient;
        } catch (Exception e) {
            ImGui.text("Failed to parse ingredients: " + e.getMessage());
        }
    }
} 