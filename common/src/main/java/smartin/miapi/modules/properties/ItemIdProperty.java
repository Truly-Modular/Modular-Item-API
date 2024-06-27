package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import smartin.miapi.blocks.ModularWorkBenchEntity;
import smartin.miapi.craft.CraftAction;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.properties.util.CraftingProperty;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.registries.RegistryInventory;

import java.util.List;
import java.util.Map;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * This Property changes the ItemIdentifier of an ModularItem on Craft
 * it only supports preregisterd ids in {@link RegistryInventory#modularItems}
 */
public class ItemIdProperty implements CraftingProperty, ModuleProperty {
    public static final String KEY = "itemId";
    public static ItemIdProperty property;

    public ItemIdProperty() {
        property = this;
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        data.getAsString();
        assert RegistryInventory.modularItems.get(data.getAsString()) != null;
        return true;
    }

    @Override
    public boolean shouldExecuteOnCraft(ModuleInstance module, ModuleInstance root, ItemStack stack) {
        return true;
    }

    public static ItemStack changeId(ItemStack itemStack){
        ModuleInstance root = ItemModule.getModules(itemStack);
        JsonElement data = ItemModule.getMergedProperty(root, property);
        if (data != null) {
            String translationKey = data.getAsString();
            Item item = RegistryInventory.modularItems.get(translationKey);
            if (item != null) {
                ModularItemCache.clearUUIDFor(itemStack);
                ItemStack newStack = new ItemStack(item);
                newStack.setNbt(itemStack.getNbt());
                newStack.setCount(itemStack.getCount());
                root.writeToItem(newStack);
                ModularItemCache.clearUUIDFor(newStack);
                return newStack;
            }
        }
        ModularItemCache.clearUUIDFor(itemStack);
        return itemStack;
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
    public ItemStack preview(ItemStack old, ItemStack crafting, Player player, ModularWorkBenchEntity bench, CraftAction craftAction, ItemModule module, List<ItemStack> inventory, Map<String,String> dataMap) {
        ModuleInstance root = ItemModule.getModules(crafting);
        JsonElement data = ItemModule.getMergedProperty(root, property);
        if (data != null) {
            String translationKey = data.getAsString();
            Item item = RegistryInventory.modularItems.get(translationKey);
            if (item != null) {
                ModularItemCache.clearUUIDFor(old);
                ItemStack newStack = new ItemStack(item);
                newStack.setNbt(crafting.getNbt());
                newStack.setCount(crafting.getCount());
                root.writeToItem(newStack);
                ModularItemCache.clearUUIDFor(newStack);
                return newStack;
            }
        }
        return crafting;
    }
}
