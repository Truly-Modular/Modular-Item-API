package smartin.miapi.client.gui.crafting;

import dev.architectury.event.events.client.ClientTooltipEvent;
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

/**
 * This is the screen handler class for miapis default Crafting Screen.
 */
public class CraftingScreenHandler extends ScreenHandler {
    private final ScreenHandlerContext context;
    private static final String PACKET_ID = ":crafting_packet_";
    public Inventory inventory;
    public PlayerInventory playerInventory;
    public final String packetID;
    public final String packetIDSlotAdd;
    public final String packetIDSlotRemove;

    /**
     * Constructs a new CraftingScreenHandler instance with the specified sync ID and player inventory.
     *
     * @param syncId          the sync ID
     * @param playerInventory the player inventory
     */
    public CraftingScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, ScreenHandlerContext.EMPTY);
    }

    /**
     * This is a custom container for the CraftingScreen that extends ScreenHandler.
     * It contains the necessary inventory references and packet IDs for syncing.
     * This class also provides a ScreenHandlerContext for context-dependent operations
     * in recipes or the inventory.
     *
     * @param syncId          the ID for syncing between client and server
     * @param playerInventory the player's inventory
     * @param context         the context of the screen
     */
    public CraftingScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(Miapi.CRAFTING_SCREEN_HANDLER, syncId);
        packetID = Miapi.MOD_ID + PACKET_ID + playerInventory.player.getUuidAsString() + "_" + syncId;
        packetIDSlotAdd = Miapi.MOD_ID + PACKET_ID + "_" + playerInventory.player.getUuidAsString() + "_" + syncId + "_slotAdd";
        packetIDSlotRemove = Miapi.MOD_ID + PACKET_ID + "_" + playerInventory.player.getUuidAsString() + "_" + syncId + "_slotRemove";
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
            });
            Networking.registerC2SPacket(packetIDSlotRemove, (buffer, player) -> {
                int slotId = buffer.readInt();
                transferSlot(playerInventory.player, slotId);
            });
        }
        this.context = context;
        this.inventory = new SimpleInventory(54) {
            @Override
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

        this.addSlot(new Slot(inventory, 0, 112, 118) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return true;
            }

            @Override
            public int getMaxItemCount() {
                return 64;
            }
        });
    }

    /**
     * Removes the given {@link Slot} from the screen and sends a packet to the server to remove it as well.
     *
     * @param slot the slot to be removed
     */
    public void removeSlotByClient(Slot slot) {
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

    /**
     * Adds the given {@link Slot} to the screen and sends a packet to the server to add it as well.
     *
     * @param slot the slot to be added
     */
    public void addSlotByClient(Slot slot) {
        if (slots.contains(slot)) return;
        this.addSlot(slot);
        PacketByteBuf buf = Networking.createBuffer();
        buf.writeInt(slot.getIndex());
        buf.writeInt(slot.id);
        Networking.sendC2S(packetIDSlotAdd, buf);

        slot.markDirty();
    }

    /**
     * Transfers an {@link ItemStack} from the slot with the given index to the player's inventory.
     *
     * @param player the player who is performing the transfer
     * @param index  the index of the slot to transfer from
     * @return the ItemStack that was transferred, or {@link ItemStack#EMPTY} if the transfer was unsuccessful
     */
    public ItemStack transferSlot(PlayerEntity player, int index) {
        Slot slot = this.slots.get(index);
        player.getInventory().offerOrDrop(slot.getStack());
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    public void close(PlayerEntity player) {
        //TODO:figure out how to react on close
        //super.close(player);
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
        Networking.unRegisterC2SPacket(packetID);
        Networking.unRegisterC2SPacket(packetIDSlotAdd);
        Networking.unRegisterC2SPacket(packetIDSlotRemove);
    }

    public void setItem(ItemStack stack) {
        inventory.setStack(0, stack);
        inventory.markDirty();
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return null;
    }

    @Override
    public void onContentChanged(Inventory inventory) {
        this.sendContentUpdates();
    }
}
