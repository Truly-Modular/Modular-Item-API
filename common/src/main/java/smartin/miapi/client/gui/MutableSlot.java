package smartin.miapi.client.gui;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;

/**
 * This is a mutable implementation of the vanilla Slot class that allows enabling or disabling the slot.
 * When a slot is disabled, it cannot be interacted with by the player.
 */
public class MutableSlot extends Slot {
    private boolean isEnabled = true;

    /**
     * Constructs a new mutable slot object.
     *
     * @param inventory the inventory that this slot belongs to.
     * @param index     the index of the slot in the inventory.
     * @param x         the X-coordinate of the slot in the GUI.
     * @param y         the Y-coordinate of the slot in the GUI.
     */
    public MutableSlot(Container inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    /**
     * Sets whether this slot is enabled or not.
     *
     * @param isEnabled true if the slot should be enabled, false otherwise.
     */
    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    /**
     * Checks whether this slot is enabled or not.
     *
     * @return true if the slot is enabled, false otherwise.
     */
    @Override
    public boolean isActive() {
        return isEnabled;
    }
}
