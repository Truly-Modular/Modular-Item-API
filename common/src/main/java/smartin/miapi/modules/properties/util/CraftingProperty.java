package smartin.miapi.modules.properties.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec2f;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.blocks.ModularWorkBenchEntity;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.craft.CraftAction;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.edit_options.EditOption;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This is an abstract Class for Crafting properties.
 * They getRaw called during craft actions.
 */
public interface CraftingProperty {
    /**
     * This class should return a {@link InteractAbleWidget} for the gui
     * this gui is rendered
     *
     * @param x      the x position of the gui
     * @param y      the y position of the gui
     * @param width  the width of the gui
     * @param height the height of the gui
     * @return return null if this property has no gui
     */
    @Nullable
    default InteractAbleWidget createGui(int x, int y, int width, int height, CraftAction action) {
        return null;
    }

    /**
     * Works together with the ui, positions to place the slots in the gui
     * the amount decides how many itemstacks are parsed in as a list in the methods
     *
     * @return a List of positions, return empty List of no guis
     */
    default List<Vec2f> getSlotPositions() {
        return new ArrayList<>();
    }

    /**
     * If the Property should be executed on craft, for most Properties this should only happen when they are involved
     */
    default boolean shouldExecuteOnCraft(@Nullable ItemModule.ModuleInstance module, ItemModule.ModuleInstance root, ItemStack stack) {
        return module != null && module.getProperties().containsKey(this);
    }

    /**
     * Write a buffer from gui to be sent to server
     *
     * @param data       A dataMap to write additional data to, its recommended to use the Properties key to avoid collisions
     * @param createdGui the gui created on the client, return value of {@link #createGui}
     */
    default void writeData(Map<String, String> data, @Nullable InteractAbleWidget createdGui, EditOption.EditContext editContext) {

    }

    /**
     * The priority for the CraftingLogic, lower priority gets executed first
     *
     * @return the priority
     */
    default float getPriority() {
        return 0;
    }

    /**
     * a check if the Crafting can happen
     *
     * @param old       the old Itemstack
     * @param crafting  the newly Crafted Itemstack
     * @param player    the player crafting
     * @param bench     the workbench block entity (null on client)
     * @param newModule the new ModuleInstance
     * @param module    the new Module
     * @param inventory Linked Inventory, length of {@link #getSlotPositions()}
     * @param data      a map including Data send from the Client for additional Craftinginfo
     * @return if the crafting can happen
     */
    default boolean canPerform(ItemStack old, ItemStack crafting, @Nullable ModularWorkBenchEntity bench, PlayerEntity player, @Nullable ItemModule.ModuleInstance newModule, ItemModule module, List<ItemStack> inventory, Map<String, String> data) {
        return true;
    }

    default Text getWarning() {
        return Text.empty();
    }

    /**
     * to create a previewStack even if the conditions are not met
     *
     * @param old       the old Itemstack
     * @param crafting  the newly Crafted Itemstack
     * @param player    the player crafting
     * @param bench     the modular workbench block entity (null on client)
     * @param newModule the new ModuleInstance
     * @param module    the new Module
     * @param inventory Linked Inventory, length of {@link #getSlotPositions()}
     * @param data      a map including Data send from the Client for additional Craftinginfo
     * @return the previewStack Itemstack
     */
    ItemStack preview(ItemStack old, ItemStack crafting, PlayerEntity player, ModularWorkBenchEntity bench, @Nullable ItemModule.ModuleInstance newModule, ItemModule module, List<ItemStack> inventory, Map<String, String> data);

    /**
     * the actual CraftAction
     *
     * @param old       the old Itemstack
     * @param crafting  the newly Crafted Itemstack
     * @param player    the player crafting
     * @param bench     the modular workbench block entity (null on client)
     * @param newModule the new ModuleInstance
     * @param module    the new Module
     * @param inventory Linked Inventory, length of {@link #getSlotPositions()}
     * @param data      a map including Data send from the Client for additional Craftinginfo
     * @return a List of Itemstacks, first is the CraftedItem, followed by a List of Itemstacks to replace Inventory slots registered by {@link #getSlotPositions()}
     */
    default List<ItemStack> performCraftAction(ItemStack old, ItemStack crafting, PlayerEntity player, @Nullable ModularWorkBenchEntity bench, @Nullable ItemModule.ModuleInstance newModule, ItemModule module, List<ItemStack> inventory, Map<String, String> data) {
        List<ItemStack> stacks = new ArrayList<>();
        stacks.add(this.preview(old, crafting, player, bench, newModule, module, inventory, data));
        stacks.addAll(inventory);
        return stacks;
    }
}