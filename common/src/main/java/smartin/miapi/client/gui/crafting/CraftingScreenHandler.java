package smartin.miapi.client.gui.crafting;

import com.mojang.datafixers.util.Pair;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.*;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.blocks.ModularWorkBenchEntity;
import smartin.miapi.client.gui.MutableSlot;
import smartin.miapi.craft.CraftAction;
import smartin.miapi.item.ModularItemStackConverter;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.edit_options.EditOption;
import smartin.miapi.modules.properties.AllowedSlots;
import smartin.miapi.modules.properties.SlotProperty;
import smartin.miapi.network.Networking;
import smartin.miapi.registries.RegistryInventory;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.screen.PlayerScreenHandler.BLOCK_ATLAS_TEXTURE;
import static net.minecraft.screen.PlayerScreenHandler.EMPTY_OFFHAND_ARMOR_SLOT;

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
    public CraftingScreenHandler craftingScreenHandler;
    private List<Slot> mutableSlots = new ArrayList<>();

    static final Identifier[] EMPTY_ARMOR_SLOT_TEXTURES = new Identifier[]{PlayerScreenHandler.EMPTY_BOOTS_SLOT_TEXTURE, PlayerScreenHandler.EMPTY_LEGGINGS_SLOT_TEXTURE, PlayerScreenHandler.EMPTY_CHESTPLATE_SLOT_TEXTURE, PlayerScreenHandler.EMPTY_HELMET_SLOT_TEXTURE};
    private static final EquipmentSlot[] EQUIPMENT_SLOT_ORDER = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    /**
     * Constructs a new CraftingScreenHandler instance with the specified sync ID and player inventory.
     *
     * @param syncId          the sync ID
     * @param playerInventory the player inventory
     */
    public CraftingScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, null, ScreenHandlerContext.EMPTY, new ArrayPropertyDelegate(7));
        craftingScreenHandler = this;
    }

    public CraftingScreenHandler(int syncId, PlayerInventory playerInventory, ModularWorkBenchEntity benchEntity, PropertyDelegate delegate) {
        this(syncId, playerInventory, benchEntity, ScreenHandlerContext.EMPTY, delegate);
        craftingScreenHandler = this;
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
        craftingScreenHandler = this;
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
                mutableSlots.add(slot);
                this.addSlot(slot);
                slot.id = slotId;
            });
            Networking.registerC2SPacket(packetIDSlotRemove, (buffer, player) -> {
                int slotId = buffer.readInt();
                Slot slot = this.getSlot(slotId);
                mutableSlots.remove(slot);
                quickMove(playerInventory.player, slotId);
            });
            Networking.registerC2SPacket(editPacketID, (buffer, player) -> {
                EditOption option = RegistryInventory.editOptions.get(buffer.readString());
                int[] intArray = buffer.readIntArray();
                ItemStack stack = ModularItemStackConverter.getModularVersion(inventory.getStack(0));
                ItemModule.ModuleInstance root = ItemModule.getModules(stack);
                List<Integer> position = new ArrayList<>();
                for (int value : intArray) {
                    position.add(value);
                }
                ItemModule.ModuleInstance current = root.getPosition(position).copy();

                SlotProperty.ModuleSlot slot = SlotProperty.getSlotIn(current);
                if (slot == null && current != null && current.module != null) {
                    slot = new SlotProperty.ModuleSlot(AllowedSlots.getAllowedSlots(current.module));
                }

                assert option != null;
                SlotProperty.ModuleSlot finalSlot = slot;
                EditOption.EditContext editContext = new EditOption.EditContext() {
                    @Override
                    public void craft(PacketByteBuf craftBuffer) {

                    }

                    @Override
                    public void preview(PacketByteBuf preview) {

                    }

                    @Override
                    public SlotProperty.ModuleSlot getSlot() {
                        return finalSlot;
                    }

                    @Override
                    public ItemStack getItemstack() {
                        return stack;
                    }

                    @Override
                    public @Nullable ItemModule.ModuleInstance getInstance() {
                        return current;
                    }

                    @Override
                    public @Nullable PlayerEntity getPlayer() {
                        return player;
                    }

                    @Override
                    public @Nullable ModularWorkBenchEntity getWorkbench() {
                        return blockEntity;
                    }

                    @Override
                    public Inventory getLinkedInventory() {
                        return inventory;
                    }

                    @Override
                    public CraftingScreenHandler getScreenHandler() {
                        return craftingScreenHandler;
                    }
                };
                if (option.isVisible(editContext)) {
                    ItemStack editedStack = option.execute(buffer, editContext);
                    inventory.setStack(0, editedStack);
                    if (blockEntity != null) {
                        blockEntity.setItem(editedStack);
                        blockEntity.saveAndSync();
                    }
                    inventory.markDirty();
                    this.onContentChanged(inventory);
                } else {
                    Miapi.LOGGER.warn("ERROR - Couldn`t verify craft action from client " + player.getUuidAsString() + " " + player.getDisplayName().getString() + " This might be a bug or somebody is trying to exploit");
                    Miapi.DEBUG_LOGGER.warn(String.valueOf(current));
                    Miapi.DEBUG_LOGGER.warn(position.toString());
                }
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
        int yOffset = 2 * 18 + 2 + 12 - 20 + 102 - 1;
        int xOffset = 30 + 2 * 18 + 138 + 7 + 2;
        for (int j = 0; j < 3; ++j) {
            for (int k = 0; k < 9; ++k) {
                this.addSlot(new PlayerInventorySlot(playerInventory, k + j * 9 + 9, k * 18 + xOffset, j * 18 + yOffset));
            }
        }

        for (int j = 0; j < 9; ++j) {
            this.addSlot(new PlayerInventorySlot(playerInventory, j, j * 18 + xOffset, 3 * 18 + 4 + yOffset));
        }

        this.addSlot(new ModifyingSlot(inventory, 0, 112 - 61, 118 + 71, blockEntity));
        for (int i = 0; i < 4; ++i) {
            final EquipmentSlot equipmentSlot = EQUIPMENT_SLOT_ORDER[i];
            int offset = i < 2 ? 0 : 1;
            this.addSlot(new Slot(playerInventory, 39 - i, 69 + i * 18 - offset, 118 + 71) {

                @Override
                public void setStack(ItemStack stack) {
                    //PlayerScreenHandler.onEquipStack(owner, equipmentSlot, stack, this.getStack());
                    super.setStack(stack);
                }

                @Override
                public int getMaxItemCount() {
                    return 1;
                }

                @Override
                public boolean canInsert(ItemStack stack) {
                    return equipmentSlot == MobEntity.getPreferredEquipmentSlot(stack);
                }

                @Override
                public boolean canTakeItems(PlayerEntity playerEntity) {
                    ItemStack itemStack = this.getStack();
                    if (!itemStack.isEmpty() && !playerEntity.isCreative() && EnchantmentHelper.hasBindingCurse(itemStack)) {
                        return false;
                    }
                    return super.canTakeItems(playerEntity);
                }

                @Override
                public Pair<Identifier, Identifier> getBackgroundSprite() {
                    return Pair.of(BLOCK_ATLAS_TEXTURE, EMPTY_ARMOR_SLOT_TEXTURES[equipmentSlot.getEntitySlotId()]);
                }
            });
        }
        this.addSlot(new Slot(playerInventory, 40, 111 - 61 + 5 * 18, 118 + 71) {

            @Override
            public void setStack(ItemStack stack) {
                //PlayerScreenHandler.onEquipStack(owner, EquipmentSlot.OFFHAND, stack, this.getStack());
                super.setStack(stack);
            }

            @Override
            public Pair<Identifier, Identifier> getBackgroundSprite() {
                return Pair.of(BLOCK_ATLAS_TEXTURE, EMPTY_OFFHAND_ARMOR_SLOT);
            }
        });
        this.addProperties(delegate);
    }

    public boolean notClient() {
        return !playerInventory.player.getWorld().isClient;
    }

    @Override
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
        mutableSlots.remove(slot);
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
        mutableSlots.add(slot);
        Networking.sendC2S(packetIDSlotAdd, buf);

        slot.markDirty();
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    @Override
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
            if (index >= 36) {
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
                    for (Slot slot1 : mutableSlots) {
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
        if (!player.isAlive() || player instanceof ServerPlayerEntity serverPlayerEntity && serverPlayerEntity.isDisconnected()) {
            for (int i = 0; i < inventory.size(); ++i) {
                if (i == 0) continue;
                player.dropItem(inventory.removeStack(i), false);
            }
            return;
        }
        for (int i = 0; i < inventory.size(); ++i) {
            if (i == 0) continue;
            PlayerInventory currentPlayerInv = player.getInventory();
            if (!(currentPlayerInv.player instanceof ServerPlayerEntity)) continue;
            currentPlayerInv.offerOrDrop(inventory.removeStack(i));
        }
    }

    @Override
    public void onContentChanged(Inventory inventory) {
        this.sendContentUpdates();
    }

    public static class ModifyingSlot extends Slot {
        protected final ModularWorkBenchEntity blockEntity;

        public ModifyingSlot(Inventory inventory, int index, int x, int y, ModularWorkBenchEntity blockEntity) {
            super(inventory, index, x, y);
            this.blockEntity = blockEntity;
        }

        public boolean notClient() {
            return blockEntity != null && blockEntity.hasWorld() && !blockEntity.getWorld().isClient;
        }

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
            if (notClient()) {
                blockEntity.setItem(stack);
                blockEntity.saveAndSync();
            }
            this.markDirty();
        }

        @Override
        public ItemStack getStack() {
            ItemStack stack = super.getStack();
            if (notClient() && !stack.isEmpty()) return blockEntity.getItem();
            return stack;
        }
    }

    public static class PlayerInventorySlot extends Slot {
        public PlayerInventorySlot(PlayerInventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }
    }
}
