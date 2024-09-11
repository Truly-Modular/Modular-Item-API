package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
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
 * @header Item ID Property
 * @path /data_types/properties/item_id
 * @description_start
 * The ItemIdProperty allows for the modification of an item's identifier upon crafting. When this property is applied,
 * it changes the `ItemStack`'s item to a different one based on a in java registered Modular Item.
 * This is useful for dynamically altering items during the crafting process, giving you the ability to switch item types
 * according to predefined configurations.
 * @description_end
 * @data item_id: the desired Item ID.
 */

public class ItemIdProperty extends CodecProperty<ResourceLocation> implements CraftingProperty {
    public static final ResourceLocation KEY = Miapi.id("item_id");
    public static ItemIdProperty property;

    public ItemIdProperty() {
        super(ResourceLocation.CODEC);
        property = this;
    }

    @Override
    public boolean shouldExecuteOnCraft(ModuleInstance module, ModuleInstance root, ItemStack stack, CraftAction action) {
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
    public ItemStack preview(ItemStack old, ItemStack crafting, Player player, ModularWorkBenchEntity bench, CraftAction craftAction, ItemModule module, List<ItemStack> inventory, Map<ResourceLocation, JsonElement> dataMap) {
        return changeId(crafting);
    }

    @Override
    public ResourceLocation merge(ResourceLocation left, ResourceLocation right, MergeType mergeType) {
        return right;
    }
}
