package smartin.miapi.modules.properties.inventory.screen;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import smartin.miapi.modules.properties.inventory.InventoryType;
import smartin.miapi.modules.properties.inventory.ItemInventoryManager;
import smartin.miapi.modules.properties.inventory.SlotInfo;
import smartin.miapi.registries.RegistryInventory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DefaultInventoryScreenHandler extends AbstractContainerMenu {

    private final Player player;

    public static class ManagedInventory {
        SlotInfo slotInfo;
        InventoryType type;
        Container container;

        int firstSlot;
        int lastSlot;
    }

    private final List<ManagedInventory> managedInventories = new ArrayList<>();

    private final int containerSlotCount;

    public DefaultInventoryScreenHandler(int syncId, Inventory playerInventory) {
        super(RegistryInventory.backpackScreenHandler, syncId);

        this.player = playerInventory.player;

        int yOffset = 10; // start below screen top
        int globalIndex = 0;
        int startYSlotInfo = 0;
        int leftPuffer = 22;
        for (SlotInfo slotInfo : ItemInventoryManager.PLAYER_TO_SLOT.values()
                .stream()
                .sorted(Comparator.comparingDouble(SlotInfo::priority))
                .toList()) {
            addSlot(new Slot(new SimpleContainer(slotInfo.getStack(player)), 0, 2, 10 + startYSlotInfo) {
                public boolean allowModification(Player player) {
                    return false;
                }

                public boolean isFake() {
                    return true;
                }

                public boolean mayPlace(ItemStack stack) {
                    return false;
                }

                public boolean mayPickup(Player player) {
                    return false;
                }
            });
            startYSlotInfo += 18;
        }

        for (InventoryType type : ItemInventoryManager.INVENTORY_TYPES.values()) {

            // Draw title spacing
            boolean hasValues = false;

            //yOffset += 10; // space after title before slots

            // Collect all SlotInfos
            List<SlotInfo> infos = ItemInventoryManager.PLAYER_TO_SLOT.values()
                    .stream()
                    .sorted(Comparator.comparingDouble(SlotInfo::priority))
                    .toList();

            int usedSlotsForType = 0; // reset horizontal counter for this type

            for (SlotInfo slotInfo : infos) {
                ItemStack containerStack = slotInfo.getStack(player);
                if (containerStack.isEmpty()) continue;

                Container container = ItemInventoryManager.loadOrCreate(containerStack, type.getId());
                int size = container.getContainerSize();
                if (size <= 0) continue;

                ManagedInventory managed = new ManagedInventory();
                managed.slotInfo = slotInfo;
                managed.type = type;
                managed.container = container;
                managed.firstSlot = slots.size();

                managedInventories.add(managed);

                for (int i = 0; i < size; i++) {
                    if (!hasValues) {
                        hasValues = true;
                        yOffset += 8;
                    }
                    int x = usedSlotsForType % 9; // horizontal position resets per type
                    int y = usedSlotsForType / 9;
                    addSlot(managed.type.createSlot(container, i, leftPuffer + x * 18, yOffset + y * 18, slotInfo, player, containerStack));
                    usedSlotsForType++;
                    globalIndex++;
                }

                managed.lastSlot = slots.size() - 1;
            }

            // Advance Y by number of rows for this InventoryType
            int rows = (int) Math.ceil(usedSlotsForType / 9.0);
            yOffset += rows * 18;

            // minimal spacing between types
            yOffset += 4;
        }

        this.containerSlotCount = globalIndex;

        /*
        ------------------------------------
        PLAYER INVENTORY
        ------------------------------------
         */

        int playerInvY = yOffset + 10; // top of player inventory

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                addSlot(new Slot(playerInventory, x + y * 9 + 9, leftPuffer + x * 18, playerInvY + y * 18));
            }
        }

        /*
        ------------------------------------
        HOTBAR
        ------------------------------------
         */

        int hotbarY = playerInvY + 58;

        for (int x = 0; x < 9; x++) {
            addSlot(new Slot(playerInventory, x, leftPuffer + x * 18, hotbarY));
        }
    }

    /*
    ------------------------------------
    SHIFT CLICK
    ------------------------------------
     */

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int index) {

        ItemStack original = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {

            ItemStack stack = slot.getItem();
            original = stack.copy();

            if (index < containerSlotCount) {

                if (!moveItemStackTo(stack, containerSlotCount, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }

            } else {

                if (!moveItemStackTo(stack, 0, containerSlotCount, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return original;
    }

    public List<ManagedInventory> getManagedInventories() {
        return managedInventories;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);

        for (ManagedInventory managed : managedInventories) {

            ItemStack stack = managed.slotInfo.getStack(player);

            ItemInventoryManager.save(stack, managed.type, managed.container);

            managed.slotInfo.setStack(stack, player);
        }
    }
}