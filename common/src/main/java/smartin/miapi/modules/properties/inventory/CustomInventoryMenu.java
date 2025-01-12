package smartin.miapi.modules.properties.inventory;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;

public class CustomInventoryMenu extends AbstractContainerMenu {

    private final Map<ItemInventoryManager.SlotInfo, List<ItemInventoryManager.InventoryType>> inventoryMap;

    protected CustomInventoryMenu(int id, Inventory playerInventory, Map<ItemInventoryManager.SlotInfo, List<ItemInventoryManager.InventoryType>> inventoryMap) {
        super(null, id); // Replace `null` with the appropriate MenuType if needed
        this.inventoryMap = inventoryMap;

        // Initialize player inventory slots
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 140 + i * 18));
            }
        }

        // Initialize player hotbar slots
        for (int i = 0; i < 9; i++) {
            addSlot(new Slot(playerInventory, i, 8 + i * 18, 198));
        }
    }

    public Map<ItemInventoryManager.SlotInfo, List<ItemInventoryManager.InventoryType>> getInventoryMap() {
        return inventoryMap;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return null;
    }

    @Override
    public boolean stillValid(Player player) {
        return false;
    }
}
