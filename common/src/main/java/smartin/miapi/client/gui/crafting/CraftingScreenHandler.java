package smartin.miapi.client.gui.crafting;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.MutableSlot;
import smartin.miapi.craft.CraftAction;
import smartin.miapi.network.Networking;

public class CraftingScreenHandler extends ScreenHandler {
    private ScreenHandlerContext context;
    public Inventory inventory;
    public PlayerInventory playerInventory;
    public final int syncId;
    public final String packetID;
    public final String packetIDSlotAdd;
    public final String packetIDSlotRemove;

    public CraftingScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, ScreenHandlerContext.EMPTY);
    }

    public CraftingScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(Miapi.CRAFTING_SCREEN_HANDLER, syncId);
        packetID = Miapi.MOD_ID + ":crafting_packet_" + syncId;
        packetIDSlotAdd = Miapi.MOD_ID + ":crafting_packet_" + syncId + "slotAdd";
        packetIDSlotRemove = Miapi.MOD_ID + ":crafting_packet_" + syncId + "slotRemove";
        this.playerInventory = playerInventory;
        if (playerInventory.player instanceof ServerPlayerEntity) {
            Networking.registerC2SPacket(packetID, (buffer, player) -> {
                CraftAction action = new CraftAction(buffer);
                action.setItem(inventory.getStack(0));
                action.linkInventory(inventory, 1);
                if (action.canPerform()) {
                    inventory.setStack(0, action.perform());
                    this.onContentChanged(inventory);
                }
            });
            Networking.registerC2SPacket(packetIDSlotAdd, (buffer, player) -> {
                int invId = buffer.readInt();
                int slotId = buffer.readInt();
                Slot slot = new Slot(inventory, invId, 0, 0);
                slot.id = slotId;
                this.addSlot(slot);
                slot.id = slotId;
                Miapi.LOGGER.error("adding Slot handler 2" + slot.id + "  " + invId);
            });
            Networking.registerC2SPacket(packetIDSlotRemove, (buffer, player) -> {
                int slotId = buffer.readInt();
                transferSlot(playerInventory.player, slotId);
            });
        }

        this.syncId = syncId;
        this.context = context;
        this.inventory = new SimpleInventory(54) {
            public void markDirty() {
                super.markDirty();
                CraftingScreenHandler.this.onContentChanged(this);
            }
        };
        int i = 18 * 2 + 1;
        int offset = 30 + 4 * 18;
        for (int j = 0; j < 3; ++j) {
            for (int k = 0; k < 9; ++k) {
                this.addSlot(new Slot(playerInventory, k + j * 9 + 9, 8 + k * 18 + offset, 103 + j * 18 + i));
            }
        }

        for (int j = 0; j < 9; ++j) {
            this.addSlot(new Slot(playerInventory, j, 8 + j * 18 + offset, 161 + i));
        }

        this.addSlot(new Slot(inventory, 0, 8 + 3 * 18 + 25, 103 + 18 + 48 + 26) {
            public boolean canInsert(ItemStack stack) {
                return true;
            }

            public int getMaxItemCount() {
                return 64;
            }
        });
    }

    public void removeSlot2(Slot slot) {
        if (!slots.contains(slot))
            return;
        transferSlot(playerInventory.player, slot.id);
        slot.markDirty();
        if (slot instanceof MutableSlot mutableSlot) {
            mutableSlot.setEnabled(false);
        }
        playerInventory.markDirty();
        inventory.markDirty();
        PacketByteBuf buf = Networking.createBuffer();
        buf.writeInt(slot.id);
        Networking.sendC2S(packetIDSlotRemove, buf);

    }

    public void addSlot2(Slot slot) {
        if (slots.contains(slot)) return;
        this.addSlot(slot);
        PacketByteBuf buf = Networking.createBuffer();
        buf.writeInt(slot.getIndex());
        buf.writeInt(slot.id);
        Networking.sendC2S(packetIDSlotAdd, buf);

        slot.markDirty();
    }

    /**
     * This is copied from EnchantingTable, adjust later
     *
     * @param player
     * @param index
     * @return
     */
    public ItemStack transferSlot(PlayerEntity player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasStack()) {
            ItemStack slotStack = slot.getStack();
            itemStack = slotStack.copy();
            if (index == 0) { // Transfer from custom slot to player inventory
                if (!this.insertItem(slotStack, 1, 37, true)) {
                    return ItemStack.EMPTY;
                }
                //slot.onStackChanged(slotStack, itemStack);
                slot.onQuickTransfer(slotStack, itemStack);
            } else { // Transfer from player inventory to custom slot
                if (this.insertItem(slotStack, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            }
            if (slotStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
            if (slotStack.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTakeItem(player, slotStack);
            this.sendContentUpdates();
        }
        return itemStack;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        //Miapi.LOGGER.warn("can Use True");
        return true;
    }

    @Override
    public void close(PlayerEntity player) {
        super.close(player);
        this.context.run((world, pos) -> {
            this.dropInventory(player, this.inventory);
        });

        // Transfer the items in the inventory to the player's inventory
        for (int i = 0; i < this.inventory.size(); i++) {
            ItemStack stack = this.inventory.getStack(i);
            if (!stack.isEmpty()) {
                if (!player.getInventory().insertStack(stack)) {
                    player.dropItem(stack, false);
                }
                this.inventory.setStack(i, ItemStack.EMPTY);
            }
        }
        Networking.unRegisterC2CPacket(packetID);
        Networking.unRegisterC2CPacket(packetIDSlotAdd);
        Networking.unRegisterC2CPacket(packetIDSlotRemove);
    }

    public void onContentChanged(Inventory inventory) {
        this.sendContentUpdates();
    }
}
