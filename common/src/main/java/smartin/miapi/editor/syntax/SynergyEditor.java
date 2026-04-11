package smartin.miapi.editor.syntax;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.tag.ModuleTagProperty;
import smartin.miapi.modules.synergies.SynergyManager;
import smartin.miapi.modules.properties.util.EditorError;
import smartin.miapi.registries.RegistryInventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SynergyEditor implements EditorInterface {

    private final CompositeEditor base = new CompositeEditor();

    public SynergyEditor() {
        base.addEditor("condition", false, new ConditionEditor());
        base.addEditor("holder", false, new PropertyHolderEditor());
    }

    @Override
    public ResourceLocation getId() {
        return Miapi.id("synergy_editor");
    }

    @Override
    public List<EditorError> validateContent(@Nullable JsonElement json, String rawContent, int offset) {
        List<EditorError> errors = new ArrayList<>();

        if (!(json instanceof JsonObject obj)) {
            errors.add(new EditorError(offset, "Synergy must be a JSON object", EditorError.ErrorSeverity.ERROR));
            return errors;
        }

        // --- type validation ---
        if (!obj.has("type")) {
            errors.add(new EditorError(
                    findLine(rawContent, "type") + offset,
                    "Missing required field: type",
                    EditorError.ErrorSeverity.ERROR
            ));
            return errors;
        }

        String typeStr = obj.get("type").getAsString();
        ResourceLocation type = Miapi.id(typeStr);

        if (!SynergyManager.SYNERGY_TYPE_REGISTRY.containsKey(type)) {
            errors.add(new EditorError(
                    findLine(rawContent, "type") + offset,
                    "Unknown synergy type: " + typeStr,
                    EditorError.ErrorSeverity.ERROR
            ));
            return errors;
        }

        // --- shared validation ---
        errors.addAll(base.validateContent(json, rawContent, offset));

        // --- type-specific validation ---
        switch (typeStr) {
            case "miapi:module" -> validateModule(obj, rawContent, offset, errors);
            case "miapi:material" -> validateMaterial(obj, rawContent, offset, errors);
            case "miapi:tag" -> validateTag(obj, rawContent, offset, errors);
        }

        return errors;
    }

    @Override
    public Map<TextRange, Integer> getSyntaxHighlighting(String content) {
        return base.getSyntaxHighlighting(content);
    }

    // ----------------------------
    // type-specific validators
    // ----------------------------

    private void validateModule(JsonObject obj, String raw, int offset, List<EditorError> errors) {
        if (!obj.has("module")) {
            errors.add(new EditorError(
                    findLine(raw, "module") + offset,
                    "Missing 'module' field",
                    EditorError.ErrorSeverity.ERROR
            ));
            return;
        }

        String id = obj.get("module").getAsString();

        // ⚠ only warning if missing
        if (!existsModule(id)) {
            errors.add(new EditorError(
                    findLine(raw, "module") + offset,
                    "Unknown module: " + id,
                    EditorError.ErrorSeverity.WARNING
            ));
        }
    }

    private void validateMaterial(JsonObject obj, String raw, int offset, List<EditorError> errors) {
        if (!obj.has("material")) {
            errors.add(new EditorError(
                    findLine(raw, "material") + offset,
                    "Missing 'material' field",
                    EditorError.ErrorSeverity.ERROR
            ));
            return;
        }

        String id = obj.get("material").getAsString();

        if (!existsMaterial(id)) {
            errors.add(new EditorError(
                    findLine(raw, "material") + offset,
                    "Unknown material: " + id,
                    EditorError.ErrorSeverity.WARNING
            ));
        }
    }

    private void validateTag(JsonObject obj, String raw, int offset, List<EditorError> errors) {
        if (!obj.has("tag")) {
            errors.add(new EditorError(
                    findLine(raw, "tag") + offset,
                    "Missing 'tag' field",
                    EditorError.ErrorSeverity.ERROR
            ));
            return;
        }

        String tag = obj.get("tag").getAsString();

        if (!existsTag(tag)) {
            errors.add(new EditorError(
                    findLine(raw, "tag") + offset,
                    "Unknown tag: " + tag,
                    EditorError.ErrorSeverity.WARNING
            ));
        }
    }

    // ----------------------------
    // existence checks (stub)
    // ----------------------------

    private boolean existsModule(String id) {
        return RegistryInventory.ITEM_MODULE_MIAPI_REGISTRY.containsKey(Miapi.id(id));
    }

    private boolean existsMaterial(String id) {
        return RegistryInventory.MATERIAL_REGISTRY.containsKey(Miapi.id(id));
    }

    private boolean existsTag(String tag) {
        return !ModuleTagProperty.getModulesWithTag(tag).isEmpty();
    }

    // ----------------------------
    // utils
    // ----------------------------

    private int findLine(String content, String key) {
        String target = "\"" + key + "\"";
        int index = content.indexOf(target);
        if (index == -1) return 1;
        return content.substring(0, index).split("\n").length;
    }
}