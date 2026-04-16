package smartin.miapi.modules.properties.inventory.screen;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import smartin.miapi.client.gui.PositionedMutableSlot;
import smartin.miapi.modules.properties.inventory.InventoryType;
import smartin.miapi.modules.properties.inventory.ItemInventoryManager;
import smartin.miapi.modules.properties.inventory.SlotInfo;
import smartin.miapi.registries.RegistryInventory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DefaultInventoryScreenHandler extends AbstractContainerMenu {

    private final Player player;
    private List<ManagedSlot> slots = new ArrayList<>();
    private List<Slot> coreItemSlots = new ArrayList<>();

    public static class ManagedInventory {
        public SlotInfo slotInfo;
        public InventoryType type;
        public Container container;

        public int firstSlot;
        public int lastSlot;
    }

    /**
     * Slot that is aware of its ManagedInventory and supports dynamic positioning + enable/disable.
     */
    public static class ManagedSlot extends PositionedMutableSlot {
        public final ManagedInventory managed;

        public ManagedSlot(ManagedInventory managed, Container container, int index) {
            super(container, index, 0, 0);
            this.managed = managed;
        }
    }

    private final List<ManagedInventory> managedInventories = new ArrayList<>();
    private final List<SlotInfo> slotInfos;

    private final int containerSlotCount;

    public DefaultInventoryScreenHandler(int syncId, Inventory playerInventory) {
        super(RegistryInventory.backpackScreenHandler, syncId);

        this.player = playerInventory.player;

        this.slotInfos = ItemInventoryManager.PLAYER_TO_SLOT.values()
                .stream()
                .sorted(Comparator.comparingDouble(SlotInfo::priority))
                .toList();

        int globalIndex = 0;

        for (SlotInfo slotInfo : slotInfos) {
            this.coreItemSlots.add(new Slot(new SimpleContainer(slotInfo.getStack(player)), 0, 0, 0) {
                @Override
                public boolean allowModification(Player player) {
                    return false;
                }

                @Override
                public boolean isFake() {
                    return true;
                }

                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }

                @Override
                public boolean mayPickup(Player player) {
                    return false;
                }
            });
        }

        /*
        ------------------------------------
        MANAGED INVENTORIES (NO LAYOUT HERE)
        ------------------------------------
         */
        for (InventoryType type : ItemInventoryManager.INVENTORY_TYPES.values()) {

            for (SlotInfo slotInfo : slotInfos) {
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
                    ManagedSlot slot = new ManagedSlot(managed, container, i);
                    addSlot(slot);
                    slots.add(slot);
                    globalIndex++;
                }

                managed.lastSlot = slots.size() - 1;
            }

        }

        int leftPuffer = 22;
        int yOffset = 6*18+6;
        this.containerSlotCount = globalIndex;
        int playerInvY = yOffset + 10;
        // top of player inventory
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                addSlot(new Slot(playerInventory, x + y * 9 + 9, leftPuffer + x * 18, playerInvY + y * 18));
            }
        } /* ------------------------------------ HOTBAR ------------------------------------ */
        int hotbarY = playerInvY + 58;
        for (int x = 0; x < 9; x++) {
            addSlot(new Slot(playerInventory, x, leftPuffer + x * 18, hotbarY));
        }
    }

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

    public List<ManagedSlot> getManagedSlots() {
        return slots;
    }

    public List<Slot> getSourceItems() {
        return coreItemSlots;
    }

    public List<SlotInfo> getSlotInfos() {
        return slotInfos;
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