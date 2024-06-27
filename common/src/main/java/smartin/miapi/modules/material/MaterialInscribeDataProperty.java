package smartin.miapi.modules.material;

import com.google.gson.JsonElement;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.architectury.event.EventResult;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
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

    public static void inscribeModuleInstance(ModuleInstance moduleInstance, ItemStack itemStack, String key) {
        Tag nbtElement = itemStack.writeNbt(new CompoundTag());
        moduleInstance.moduleData.put(key, nbtElement.getAsString());
    }

    public static ItemStack readStackFromModuleInstance(ModuleInstance moduleInstance, String key) {
        String itemStackString = moduleInstance.moduleData.get(key);
        if (itemStackString != null) {
            try {
                return ItemStack.parse(TagParser.parseTag(itemStackString));

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
