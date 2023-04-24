package smartin.miapi.item.modular.properties;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.Vec2f;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.item.modular.ItemModule;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * This is an abstract Class for Crafting properties.
 * They get called during craft actions.
 */
public abstract class CraftingProperty implements ModuleProperty {
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
    public InteractAbleWidget createGui(int x, int y, int width, int height) {
        return null;
    }

    /**
     * Works together with the ui, positions to place the slots in the gui
     * the amount decides how many itemstacks are parsed in as a list in the methods
     *
     * @return a List of positions, return empty List of no guis
     */
    public List<Vec2f> getSlotPositions() {
        return new ArrayList<>();
    }

    /**
     * Write a buffer from gui to be sent to server
     *
     * @param buf        the buffer to write to
     * @param createdGui the gui created on the client, return value of {@link #createGui}
     */
    public void writeCraftingBuffer(PacketByteBuf buf, InteractAbleWidget createdGui) {

    }

    /**
     * The priority for the CraftingLogic, lower priority gets executed first
     *
     * @return the priority
     */
    public abstract float getPriority();

    /**
     * a check if the Crafting can happen
     *
     * @param old       the old Itemstack
     * @param crafting  the newly Crafted Itemstack
     * @param player    the player crafting
     * @param newModule the new ModuleInstance
     * @param module    the new Module
     * @param inventory Linked Inventory, length of {@link #getSlotPositions()}
     * @param buf       the writen buffer from {@link #writeCraftingBuffer(PacketByteBuf, InteractAbleWidget)}
     * @return if the crafting can happen
     */
    public boolean canPerform(ItemStack old, ItemStack crafting, PlayerEntity player, ItemModule.ModuleInstance newModule, ItemModule module, List<ItemStack> inventory, PacketByteBuf buf) {
        return true;
    }

    /**
     * to create a preview even if the conditions are not met
     *
     * @param old       the old Itemstack
     * @param crafting  the newly Crafted Itemstack
     * @param player    the player crafting
     * @param newModule the new ModuleInstance
     * @param module    the new Module
     * @param inventory Linked Inventory, length of {@link #getSlotPositions()}
     * @param buf       the writen buffer from {@link #writeCraftingBuffer(PacketByteBuf, InteractAbleWidget)}
     * @return the preview Itemstack
     */
    public abstract ItemStack preview(ItemStack old, ItemStack crafting, PlayerEntity player, ItemModule.ModuleInstance newModule, ItemModule module, List<ItemStack> inventory, PacketByteBuf buf);

    /**
     * the actual CraftAction
     *
     * @param old       the old Itemstack
     * @param crafting  the newly Crafted Itemstack
     * @param player    the player crafting
     * @param newModule the new ModuleInstance
     * @param module    the new Module
     * @param inventory Linked Inventory, length of {@link #getSlotPositions()}
     * @param buf       the writen buffer from {@link #writeCraftingBuffer(PacketByteBuf, InteractAbleWidget)}
     * @return a List of Itemstacks, first is the CraftedItem, followed by a List of Itemstacks to replace Inventory slots registered by {@link #getSlotPositions()}
     */
    public List<ItemStack> performCraftAction(ItemStack old, ItemStack crafting, PlayerEntity player, ItemModule.ModuleInstance newModule, ItemModule module, List<ItemStack> inventory, PacketByteBuf buf) {
        List<ItemStack> stacks = new ArrayList<>();
        stacks.add(this.preview(old, crafting, player, newModule, module, inventory, buf));
        stacks.addAll(inventory);
        return stacks;
    }
}