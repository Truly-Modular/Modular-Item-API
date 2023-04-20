package smartin.miapi.client.gui;

import net.minecraft.inventory.Inventory;
import net.minecraft.screen.slot.Slot;
import smartin.miapi.Miapi;

public class MutableSlot extends Slot {
    private boolean isEnabled = true;

    public MutableSlot(Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }
}
