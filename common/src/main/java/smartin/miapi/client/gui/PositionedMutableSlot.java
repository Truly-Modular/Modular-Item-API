package smartin.miapi.client.gui;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import smartin.miapi.mixin.SlotAccessor;

/**
 * A mutable slot that also supports runtime position changes via mixin accessors.
 */
public class PositionedMutableSlot extends MutableSlot {
    public boolean isHighlightable = true;

    public PositionedMutableSlot(Container inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    /**
     * Sets the X position of this slot.
     */
    public void setX(int x) {
        ((SlotAccessor) (Slot) this).setXMiapi(x);
    }

    /**
     * Sets the Y position of this slot.
     */
    public void setY(int y) {
        ((SlotAccessor) (Slot) this).setYMiapi(y);
    }

    /**
     * Sets both X and Y position.
     */
    public void setPos(int x, int y) {
        SlotAccessor accessor = (SlotAccessor) (Slot) this;
        accessor.setXMiapi(x);
        accessor.setYMiapi(y);
    }

    public void setHighlightable(boolean isHighlightable) {
        this.isHighlightable = isHighlightable;
    }

    public boolean isHighlightable() {
        return isHighlightable;
    }

    public boolean mayPickup(Player player) {
        return isHighlightable;
    }

    /**
     * Convenience: offset position.
     */
    public void offset(int dx, int dy) {
        setPos(this.x + dx, this.y + dy);
    }
}