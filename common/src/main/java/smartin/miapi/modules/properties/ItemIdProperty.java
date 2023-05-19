package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.util.CraftingProperty;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.List;

/**
 * This Property changes the ItemIdentifier of an ModularItem on Craft
 * it only supports preregisterd ids in {@link Miapi#itemRegistry}
 */
public class ItemIdProperty implements CraftingProperty,ModuleProperty {
    public static final String KEY = "itemId";
    public static ModuleProperty property;

    public ItemIdProperty() {
        property = this;
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        data.getAsString();
        assert Miapi.itemRegistry.get(data.getAsString()) != null;
        return true;
    }

    @Override
    public JsonElement merge(JsonElement old, JsonElement toMerge, MergeType type) {
        switch (type) {
            case EXTEND -> {
                return old;
            }
            case SMART, OVERWRITE -> {
                return toMerge;
            }
        }
        return old;
    }

    @Override
    public ItemStack preview(ItemStack old, ItemStack crafting, PlayerEntity player, ItemModule.ModuleInstance newModule, ItemModule module, List<ItemStack> inventory, PacketByteBuf buf) {
        ItemModule.ModuleInstance root = ItemModule.getModules(crafting);
        JsonElement data =  ItemModule.getMergedProperty(root, property);
        String translationKey = "";
        if (data != null) {
            translationKey = data.getAsString();
        }
        Item item = Miapi.itemRegistry.get(translationKey);
        if (item != null) {
            ItemStack newStack = new ItemStack(item);
            newStack.setNbt(crafting.getNbt());
            crafting.getNbt().putString("modules", newModule.getRoot().toString());
            return newStack;
        }
        return crafting;
    }
}
