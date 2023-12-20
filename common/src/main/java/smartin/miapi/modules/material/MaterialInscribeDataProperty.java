package smartin.miapi.modules.material;

import com.google.gson.JsonElement;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.architectury.event.EventResult;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.StringNbtReader;
import smartin.miapi.Miapi;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.util.ModuleProperty;

/**
 * This Property allows to designate the Item as Tool or Weapon,
 * mainly affecting durability calculations
 */
public class MaterialInscribeDataProperty implements ModuleProperty {
    public static final String KEY = "inscribe_data_on_craft";
    public static MaterialInscribeDataProperty property;

    public MaterialInscribeDataProperty() {
        property = this;
        MiapiEvents.MATERIAL_CRAFT_EVENT.register((listener) -> {
            listener.crafted = inscribe(listener);
            return EventResult.pass();
        });
    }

    public static ItemStack inscribe(MiapiEvents.MaterialCraftEventData data) {
        ItemStack raw = data.crafted;
        JsonElement element = ItemModule.getMergedProperty(raw, property);
        if (element != null) {
            inscribeModuleInstance(data.moduleInstance, data.materialStack.copy(), element.getAsString());
            data.moduleInstance.getRoot().writeToItem(data.crafted);
        }
        return raw;
    }

    public static void inscribeModuleInstance(ItemModule.ModuleInstance moduleInstance, ItemStack itemStack, String key) {
        NbtElement nbtElement = itemStack.writeNbt(new NbtCompound());
        moduleInstance.moduleData.put(key, nbtElement.asString());
    }

    public static ItemStack readStackFromModuleInstance(ItemModule.ModuleInstance moduleInstance, String key) {
        String itemStackString = moduleInstance.moduleData.get(key);
        Miapi.LOGGER.info("reading data from ModuleInstance : " + itemStackString);
        if (itemStackString != null) {
            try {
                return ItemStack.fromNbt(StringNbtReader.parse(itemStackString));

            } catch (CommandSyntaxException ignored) {
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        data.getAsString();
        return true;
    }
}
