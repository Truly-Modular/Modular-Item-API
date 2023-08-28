package smartin.miapi.modules.properties.material;

import com.google.gson.JsonElement;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.util.ModuleProperty;

/**
 * This Property allows to designate the Item as Tool or Weapon,
 * mainly affecting durability calculations
 */
public class MaterialInscribeProperty implements ModuleProperty {
    public static final String KEY = "inscribe_on_craft";
    public static MaterialInscribeProperty property;

    public MaterialInscribeProperty() {
        property = this;
    }

    public static ItemStack inscribe(ItemStack raw, ItemStack materialStack) {
        JsonElement element = ItemModule.getMergedProperty(raw, property);
        if (element != null) {
            NbtCompound compound = raw.getOrCreateNbt();
            materialStack = materialStack.copy();
            materialStack.setCount(1);
            compound.put(element.getAsString(), materialStack.writeNbt(new NbtCompound()));
            compound.copyFrom(materialStack.getOrCreateNbt());
            raw.setNbt(compound);
        }
        return raw;
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        data.getAsString();
        return true;
    }
}
