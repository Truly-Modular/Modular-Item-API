package smartin.miapi.modules.properties;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.blocks.ModularWorkBenchEntity;
import smartin.miapi.craft.CraftAction;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.CraftingProperty;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.registries.RegistryInventory;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This Property changes the ItemIdentifier of an ModularItem on Craft
 * it only supports preregisterd ids in {@link RegistryInventory#modularItems}
 */
public class ItemIdProperty extends CodecProperty<ResourceLocation> implements CraftingProperty {
    public static final String KEY = "itemId";
    public static ItemIdProperty property;

    public ItemIdProperty() {
        super(ResourceLocation.CODEC);
        property = this;
    }

    @Override
    public boolean shouldExecuteOnCraft(ModuleInstance module, ModuleInstance root, ItemStack stack) {
        return true;
    }

    public static ItemStack changeId(ItemStack itemStack) {
        ModuleInstance root = ItemModule.getModules(itemStack);
        Optional<ResourceLocation> optional = property.getData(itemStack);
        if (optional.isPresent()) {
            Item item = RegistryInventory.modularItems.get(optional.get().toString());
            if (item != null) {
                root.clearCaches();
                ItemStack newStack = new ItemStack(item);
                newStack.applyComponents(itemStack.getComponents());
                newStack.setCount(itemStack.getCount());
                ModuleInstance newRoot = root.copy();
                newRoot.writeToItem(newStack);
                newRoot.clearCaches();
                return newStack;
            }
        }
        return itemStack.copy();
    }

    @Override
    public ItemStack preview(ItemStack old, ItemStack crafting, Player player, ModularWorkBenchEntity bench, CraftAction craftAction, ItemModule module, List<ItemStack> inventory, Map<String, String> dataMap) {
        return changeId(old);
    }

    @Override
    public ResourceLocation merge(ResourceLocation left, ResourceLocation right, MergeType mergeType) {
        return right;
    }
}
