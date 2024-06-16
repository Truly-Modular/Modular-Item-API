package smartin.miapi.modules.material;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import dev.architectury.event.EventResult;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import smartin.miapi.Environment;
import smartin.miapi.Miapi;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.Optional;

import static smartin.miapi.modules.material.MaterialProperty.materials;

public class NBTMaterial extends JsonMaterial {
    public static String NBTKEY = "miapi_material";
    public static String KEY = "nbt_runtime_material";
    public JsonObject overWrite;
    public Material parent;
    public double cost = 1.0;

    public NBTMaterial(Material parent, JsonObject overwrite, boolean isClient) {
        super(parent.getDebugJson().deepCopy(), isClient);
        this.parent = parent;
        this.overWrite = overwrite;
        this.mergeJson(overwrite, isClient);
    }

    public void mergeJson(JsonElement rootElement, boolean isClient) {
        if (rootElement.isJsonObject()) {
            JsonObject object = rootElement.getAsJsonObject();
            if (object.has("cost")) {
                cost = ModuleProperty.getDouble(object, "cost", null, cost);
            }
        }
        super.mergeJson(rootElement, isClient);
    }

    @Override
    public String getKey() {
        return KEY;
    }

    public static void setup() {
        ReloadEvents.MAIN.subscribe(isClient -> {
            JsonObject object = new JsonObject();
            object.addProperty("key", KEY);
            materials.put(
                    KEY,
                    new NBTMaterial(
                            new JsonMaterial(object, isClient),
                            new JsonObject(), isClient));
        }, -1);
        MiapiEvents.MATERIAL_CRAFT_EVENT.register(data -> {
            if (data.material instanceof NBTMaterial nbtMaterial) {
                nbtMaterial.writeMaterial(data.moduleInstance);
            }
            return EventResult.pass();
        });
    }

    public Material getMaterial(ModuleInstance moduleInstance) {
        String data = moduleInstance.moduleData.get("miapi:nbt_material_data");
        try {
            JsonObject object = Miapi.gson.fromJson(data, JsonObject.class);
            Optional<Material> material = decode(object);
            return material.orElse(this);
        } catch (Exception e) {
            Miapi.LOGGER.error("Could not find Material", e);
        }
        return this;
    }

    public void writeMaterial(ModuleInstance moduleInstance) {
        JsonObject object1 = this.overWrite.deepCopy();
        object1.addProperty("parent", this.parent.getKey());

        moduleInstance.moduleData.put("miapi:nbt_material_data", Miapi.gson.toJson(object1));
    }

    public Material getMaterialFromIngredient(ItemStack ingredient) {
        if (ingredient.hasNbt() && ingredient.getNbt().contains(NBTKEY)) {
            NbtCompound compound = ingredient.getNbt().getCompound(NBTKEY);
            Optional<JsonElement> element = NbtCompound.CODEC.encodeStart(JsonOps.INSTANCE, compound).result();
            if (element.isPresent()) {
                JsonObject object = element.get().getAsJsonObject();
                Optional<Material> material = decode(object);
                if (material.isPresent()) {
                    return material.get();
                }
            }
        }
        return null;
    }

    public Optional<Material> decode(JsonObject object) {
        try {
            String parentID = object.get("parent").getAsString();
            Material parent = MaterialProperty.materials.get(parentID);
            if (parent == null) {
                Miapi.LOGGER.error("Could not find Material:" + parentID);
            }
            return Optional.of(new NBTMaterial(parent, object, Environment.isClient()));
        } catch (Exception e) {
            Miapi.LOGGER.error("Could not find Material", e);
        }
        return Optional.empty();
    }

    @Override
    public double getValueOfItem(ItemStack itemStack) {
        if (itemStack.hasNbt() && itemStack.getNbt().contains(NBTKEY)) {
            return 1.0;
        }
        return 0.0;
    }

    @Override
    public Double getPriorityOfIngredientItem(ItemStack itemStack) {
        if (itemStack.hasNbt() && itemStack.getNbt().contains(NBTKEY)) {
            return -5.0;
        }
        return null;
    }
}
