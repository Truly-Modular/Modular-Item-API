package smartin.miapi.client.gui.crafting;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.blocks.ModularWorkBenchEntity;
import smartin.miapi.client.gui.MutableSlot;
import smartin.miapi.craft.CraftAction;
import smartin.miapi.item.ModularItemStackConverter;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.edit_options.EditOption;
import smartin.miapi.network.Networking;
import smartin.miapi.registries.RegistryInventory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * This is the screen handler class for miapis default Crafting Screen.
 */
public class CraftingScreenHandler extends ScreenHandler {
    private final ScreenHandlerContext context;
    private static final String PACKET_ID = ":crafting_packet_";
    public Inventory inventory;
    public PlayerInventory playerInventory;
    public @Nullable ModularWorkBenchEntity blockEntity;
    public final PropertyDelegate delegate;
    public final String packetID;
    public final String editPacketID;
    public final String packetIDSlotAdd;
    public final String packetIDSlotRemove;

    /**
     * Constructs a new CraftingScreenHandler instance with the specified sync ID and player inventory.
     *
     * @param syncId          the sync ID
     * @param playerInventory the player inventory
     */
    public CraftingScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, null, ScreenHandlerContext.EMPTY, new ArrayPropertyDelegate(7));
    }
    public CraftingScreenHandler(int syncId, PlayerInventory playerInventory, ModularWorkBenchEntity benchEntity, PropertyDelegate delegate) {
        this(syncId, playerInventory, benchEntity, ScreenHandlerContext.EMPTY, delegate);
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
    public CraftingScreenHandler(int syncId, PlayerInventory playerInventory, @Nullable ModularWorkBenchEntity benchEntity, ScreenHandlerContext context, PropertyDelegate delegate) {
        super(RegistryInventory.craftingScreenHandler, syncId);
        packetID = Miapi.MOD_ID + PACKET_ID + playerInventory.player.getUuidAsString() + "_" + syncId;
        editPacketID = Miapi.MOD_ID + PACKET_ID + "_edit_" + playerInventory.player.getUuidAsString() + "_" + syncId;
        packetIDSlotAdd = Miapi.MOD_ID + PACKET_ID + "_" + playerInventory.player.getUuidAsString() + "_" + syncId + "_slotAdd";
        packetIDSlotRemove = Miapi.MOD_ID + PACKET_ID + "_" + playerInventory.player.getUuidAsString() + "_" + syncId + "_slotRemove";
        this.delegate = delegate;
        this.playerInventory = playerInventory;
        this.blockEntity = benchEntity;
        if (playerInventory.player instanceof ServerPlayerEntity) {
            Networking.registerC2SPacket(packetID, (buffer, player) -> {
                CraftAction action = new CraftAction(buffer, blockEntity);
                action.setItem(inventory.getStack(0));
                action.linkInventory(inventory, 1);
                if (action.canPerform()) {
                    ItemStack stack = action.perform();
                    inventory.setStack(0, stack);
                    if (blockEntity != null) {
                        blockEntity.setItem(stack);
                        blockEntity.saveAndSync();
                    }
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
                quickMove(playerInventory.player, slotId);
            });
            Networking.registerC2SPacket(editPacketID, (buffer, player) -> {
                EditOption option = RegistryInventory.editOptions.get(buffer.readString());
                int[] intArray = buffer.readIntArray();
                ItemStack stack = ModularItemStackConverter.getModularVersion(inventory.getStack(0));
                ItemModule.ModuleInstance root = ItemModule.getModules(stack);
                List<Integer> position = new ArrayList<>();
                for (int value : intArray) {
                    Miapi.LOGGER.error("value " + value);
                    position.add(value);
                }
                Miapi.LOGGER.warn(root.toString());
                stack = option.execute(buffer, stack, root.getPosition(position).copy());
                inventory.setStack(0, stack);
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
        if (blockEntity != null) {
            this.setItem(blockEntity.getItem());
        }
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

            @Override
            public void setStack(ItemStack stack) {
                super.setStack(stack);
                if (notClient() && blockEntity != null) {
                    blockEntity.setItem(stack);
                    blockEntity.saveAndSync();
                }
                this.markDirty();
            }

            @Override
            public void onTakeItem(PlayerEntity player, ItemStack stack) {
                super.onTakeItem(player, stack);
            }

            @Override
            public ItemStack getStack() {
                ItemStack stack = super.getStack();
                if (notClient() && blockEntity != null && !stack.isEmpty()) return blockEntity.getItem();
                return stack;
            }
        });

        this.addProperties(delegate);
    }

    public boolean notClient() {
        return !playerInventory.player.getWorld().isClient;
    }

    public void sendContentUpdates() {
        super.sendContentUpdates();
        if (blockEntity == null && delegate.get(0) == 1) {
            short xsh = (short) delegate.get(1);
            short xsl = (short) delegate.get(2);
            int x = (xsh << 16) | (xsl & 0xFFFF);

            short ysh = (short) delegate.get(3);
            short ysl = (short) delegate.get(4);
            int y = (ysh << 16) | (ysl & 0xFFFF);

            short zsh = (short) delegate.get(5);
            short zsl = (short) delegate.get(6);
            int z = (zsh << 16) | (zsl & 0xFFFF);

            BlockEntity be = playerInventory.player.getWorld().getBlockEntity(new BlockPos(x, y, z));
            if (be instanceof ModularWorkBenchEntity casted) blockEntity = casted;
        }
    }

    /**
     * Removes the given {@link Slot} from the screen and sends a packet to the server to remove it as well.
     *
     * @param slot the slot to be removed
     */
    public void removeSlotByClient(Slot slot) {
        if (!slots.contains(slot))
            return;
        quickMove(playerInventory.player, slot.id);
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

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        this.context.run((world, pos) -> {
            this.dropInventory(player, this.inventory);
        });

        // Transfer the items in the inventory to the player's inventory
        for (int i = 0; i < this.inventory.size(); i++) {
            if (i == 0) continue;
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
        Networking.unRegisterC2SPacket(editPacketID);
        if (notClient() && blockEntity != null) blockEntity.saveAndSync();
    }

    public void setItem(ItemStack stack) {
        inventory.setStack(0, stack);
        inventory.markDirty();
        if (blockEntity != null) {
            blockEntity.setItem(stack);
            if (notClient()) blockEntity.saveAndSync();
        }
    }

    public ItemStack quickMove(PlayerEntity player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasStack()) {
            ItemStack itemStack2 = slot.getStack();
            itemStack = itemStack2.copy();
            if (index >= 36 ) {
                //case 1: tool slot to player
                slot.onTakeItem(player, itemStack2);
                if (!this.insertItem(itemStack2, 0, 36, true)) {
                    return ItemStack.EMPTY;
                }

                if (index == 36 && blockEntity != null) {
                    blockEntity.setItem(itemStack2);
                    if (notClient()) blockEntity.saveAndSync();
                }
                slot.markDirty();
            } else {
                //PlayerInv
                if ((slots.get(36).getStack().isEmpty() || slots.get(36).getStack().getItem().equals(itemStack2.getItem())) && !this.insertItem(itemStack2, 36, 37, true)) {
                    return ItemStack.EMPTY;
                } else {
                    for (Slot slot1 : slots) {
                        if (slot1.id >= 36) {
                            if (!this.insertItem(itemStack2, slot1.id, slot1.id + 1, true)) {
                                return ItemStack.EMPTY;
                            }
                        }
                    }
                }
                slot.markDirty();
            }
        }
        return itemStack;
    }

    @Override
    protected void dropInventory(PlayerEntity player, Inventory inventory) {
        if (!player.isAlive() || player instanceof ServerPlayerEntity && ((ServerPlayerEntity)player).isDisconnected()) {
            for (int i = 0; i < inventory.size(); ++i) {
                if (i == 0) continue;
                player.dropItem(inventory.removeStack(i), false);
            }
            return;
        }
        for (int i = 0; i < inventory.size(); ++i) {
            if (i == 0) continue;
            PlayerInventory playerInventory = player.getInventory();
            if (!(playerInventory.player instanceof ServerPlayerEntity)) continue;
            playerInventory.offerOrDrop(inventory.removeStack(i));
        }
    }

    @Override
    public void onContentChanged(Inventory inventory) {
        this.sendContentUpdates();
    }
}
