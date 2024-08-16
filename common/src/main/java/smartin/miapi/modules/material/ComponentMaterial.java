package smartin.miapi.modules.material;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.architectury.event.EventResult;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Environment;
import smartin.miapi.Miapi;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.modules.ModuleInstance;

import java.util.Optional;

import static smartin.miapi.modules.material.MaterialProperty.materials;

public class ComponentMaterial extends JsonMaterial {
    public static ResourceLocation KEY = Miapi.id("component_runtime_material");
    public JsonObject overWrite;
    public Material parent;
    public double cost = 1.0;
    public static Codec<ComponentMaterial> CODEC = RecordCodecBuilder.create((instance) ->
            instance.group(
                    Codec.DOUBLE
                            .optionalFieldOf("cost", 1.0)
                            .forGetter((material) -> material.cost),
                    StatResolver.Codecs.JSONELEMENT_CODEC
                            .optionalFieldOf("overwrite", new JsonObject())
                            .forGetter((material) -> material.overWrite),
                    ResourceLocation.CODEC
                            .fieldOf("parent")
                            .forGetter((material) -> material.parent.getID())
            ).apply(instance, (cost, json, materialKey) -> {
                Material material = materials.get(materialKey);
                return new ComponentMaterial(material, json, cost, Environment.isClient());
            }));

    public static DataComponentType<ComponentMaterial> NBT_MATERIAL_COMPONENT = DataComponentType.<ComponentMaterial>builder()
            .persistent(CODEC)
            .networkSynchronized(ByteBufCodecs.fromCodec(CODEC)).build();

    public ComponentMaterial(Material parent, JsonElement overwrite, double cost, boolean isClient) {
        super(KEY, parent.getDebugJson().deepCopy(), isClient);
        this.parent = parent;
        this.overWrite = overwrite.getAsJsonObject();
        this.mergeJson(overwrite, isClient);
        this.cost = cost;

    }

    public ComponentMaterial(Material parent, JsonObject overwrite, boolean isClient) {
        super(KEY, parent.getDebugJson().deepCopy(), isClient);
        this.parent = parent;
        this.overWrite = overwrite;
        this.mergeJson(overwrite, isClient);
    }

    public void mergeJson(JsonElement rootElement, boolean isClient) {
        if (rootElement.isJsonObject()) {
            JsonObject object = rootElement.getAsJsonObject();
            if (object.has("cost")) {
                cost = object.get("cost").getAsDouble();
            }
        }
        super.mergeJson(rootElement, isClient);
    }

    @Override
    public ResourceLocation getID() {
        return KEY;
    }

    public static void setup() {
        ReloadEvents.MAIN.subscribe(isClient -> {
            JsonObject object = new JsonObject();
            materials.put(
                    KEY,
                    new ComponentMaterial(
                            new JsonMaterial(KEY, object, isClient),
                            new JsonObject(), isClient));
        }, -1);
        MiapiEvents.MATERIAL_CRAFT_EVENT.register(data -> {
            if (data.material instanceof ComponentMaterial componentMaterial) {
                componentMaterial.writeMaterial(data.moduleInstance);
            }
            return EventResult.pass();
        });
    }

    public Material getMaterial(ModuleInstance moduleInstance) {
        JsonElement data = moduleInstance.moduleData.get("miapi:nbt_material_data");
        try {
            Optional<Material> material = decode(data.getAsJsonObject());
            return material.orElse(this);
        } catch (Exception e) {
            Miapi.LOGGER.error("Could not find Material", e);
        }
        return this;
    }

    public void writeMaterial(ModuleInstance moduleInstance) {
        JsonObject object1 = this.overWrite.deepCopy();
        object1.addProperty("parent", this.parent.getID().toString());

        moduleInstance.moduleData.put("miapi:nbt_material_data", object1);
    }

    @Nullable
    public Material getMaterialFromIngredient(ItemStack ingredient) {
        return ingredient.getComponents().get(NBT_MATERIAL_COMPONENT);
    }

    public Optional<Material> decode(JsonObject object) {
        try {
            String parentID = object.get("parent").getAsString();
            Material parentMaterial = MaterialProperty.materials.get(parentID);
            if (parentMaterial == null) {
                Miapi.LOGGER.error("Could not find Material:" + parentID);
                return Optional.empty();
            }
            return Optional.of(new ComponentMaterial(parentMaterial, object, Environment.isClient()));
        } catch (Exception e) {
            Miapi.LOGGER.error("Could not find Material", e);
        }
        return Optional.empty();
    }

    @Override
    public double getValueOfItem(ItemStack itemStack) {
        ComponentMaterial material = itemStack.getComponents().get(NBT_MATERIAL_COMPONENT);
        if (material != null) {
            return material.cost;
        }
        return 0.0;
    }

    @Override
    public Double getPriorityOfIngredientItem(ItemStack itemStack) {
        if (itemStack.getComponents().has(NBT_MATERIAL_COMPONENT)) {
            return -5.0;
        }
        return null;
    }
}
