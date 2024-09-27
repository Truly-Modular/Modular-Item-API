package smartin.miapi.material;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Environment;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

public class MaterialOverwriteProperty implements ModuleProperty<JsonElement> {
    public static final ResourceLocation KEY = Miapi.id("material_overwrite");
    public static MaterialOverwriteProperty property;

    public MaterialOverwriteProperty() {
        property = this;
    }

    public Material adjustMaterial(ModuleInstance moduleInstance, Material old) {
        if (getData(moduleInstance).isPresent()) {
            JsonMaterial merged = new JsonMaterial(old.getID(), old.getDebugJson(), Environment.isClient());
            merged.mergeJson(getData(moduleInstance).get(), Environment.isClient());
            return merged;
        }
        return old;
    }

    @Override
    public JsonElement decode(JsonElement element) {
        return element;
    }

    @Override
    public JsonElement encode(JsonElement property) {
        return property;
    }

    @Override
    public JsonElement merge(JsonElement left, JsonElement right, MergeType mergeType) {
        if (left instanceof JsonObject objectLeft && right instanceof JsonObject objectRight) {
            return (JsonElement) ModuleProperty.mergedJsonObjects(objectLeft, objectRight, mergeType);
        }
        return ModuleProperty.decideLeftRight(left, right, mergeType);
    }
}
