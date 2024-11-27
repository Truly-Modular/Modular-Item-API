package smartin.miapi.material;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import dev.architectury.event.EventResult;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.Optional;

/**
 * This Property allows you to encode the material Itemstack into the module it was used to craft it
 */
public class MaterialInscribeDataProperty extends CodecProperty<String> {
    public static final ResourceLocation KEY = Miapi.id("inscribe_data_on_craft");
    public static MaterialInscribeDataProperty property;

    public MaterialInscribeDataProperty() {
        super(Codec.STRING);
        property = this;
        MiapiEvents.MATERIAL_CRAFT_EVENT.register((listener) -> {
            listener.crafted = inscribe(listener);
            return EventResult.pass();
        });
    }

    public static ItemStack inscribe(MiapiEvents.MaterialCraftEventData data) {
        ItemStack raw = data.crafted;
        Optional<String> optional = property.getData(data.moduleInstance);
        if (optional.isPresent()) {
            inscribeModuleInstance(data.moduleInstance, data.materialStack.copy(), optional.get());
            data.moduleInstance.getRoot().writeToItem(data.crafted);
        }
        return raw;
    }

    public static void inscribeModuleInstance(ModuleInstance moduleInstance, ItemStack itemStack, String key) {
        JsonElement element = ItemStack.CODEC.encodeStart(JsonOps.INSTANCE, itemStack).getOrThrow();
        moduleInstance.moduleData.put(Miapi.id(key), element);
    }

    public static ItemStack readStackFromModuleInstance(ModuleInstance moduleInstance, String key) {
        JsonElement element = moduleInstance.moduleData.get(Miapi.id(key));
        if (element!= null) {
            try {
                return ItemStack.CODEC.decode(JsonOps.INSTANCE, element).getOrThrow().getFirst();

            } catch (RuntimeException ignored) {
                Miapi.LOGGER.error("failed to read item-data from moduledata " + key, ignored);
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public String merge(String left, String right, MergeType mergeType) {
        return ModuleProperty.decideLeftRight(left, right, mergeType);
    }
}
