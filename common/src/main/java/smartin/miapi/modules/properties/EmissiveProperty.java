package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.item.ItemStack;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

public class EmissiveProperty implements ModuleProperty {
    public static final String KEY = "emissive";
    public static EmissiveProperty property;

    public EmissiveProperty() {
        property = this;
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) {
        return (data instanceof JsonPrimitive prim && prim.isBoolean()) || data instanceof JsonObject;
    }

    public static int[] getLightValues(ItemStack stack) {
        return getLightValues(property.getJsonElement(stack));
    }

    public static int[] getLightValues(ModuleInstance instance) {
        return getLightValues(property.getJsonElement(instance));
    }

    public static int[] getLightValues(JsonElement element) {
        int sky = -1;
        int block = -1;

        if (element instanceof JsonPrimitive primitive && primitive.isBoolean() && primitive.getAsBoolean()) {
            sky = 15;
            block = 15;
        } else if (element instanceof JsonObject object) {
            JsonElement skyRaw = object.get("sky");
            if (skyRaw instanceof JsonPrimitive primitive && primitive.isNumber())
                sky = primitive.getAsInt();

            JsonElement blockRaw = object.get("block");
            if (blockRaw instanceof JsonPrimitive primitive && primitive.isNumber())
                block = primitive.getAsInt();
        }

        return new int[]{sky, block};
    }

    @Override
    public JsonElement merge(JsonElement old, JsonElement toMerge, MergeType type) {
        int[] oldValues = getLightValues(old);
        int oldSky = oldValues[0];
        int oldBlock = oldValues[1];

        int[] newValues = getLightValues(toMerge);
        int newSky = newValues[0];
        int newBlock = newValues[1];

        int sky = Math.max(newSky, oldSky);
        int block = Math.max(newBlock, oldBlock);

        JsonObject obj = new JsonObject();
        obj.addProperty("sky", sky);
        obj.addProperty("block", block);

        return obj;
    }
}
