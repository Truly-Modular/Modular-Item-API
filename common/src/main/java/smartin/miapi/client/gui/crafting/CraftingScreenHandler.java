package smartin.miapi.client.gui.crafting;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.blocks.ModularWorkBenchEntity;
import smartin.miapi.client.gui.MutableSlot;
import smartin.miapi.craft.CraftAction;
import smartin.miapi.item.ModularItemStackConverter;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.edit_options.EditOption;
import smartin.miapi.modules.properties.slot.AllowedSlots;
import smartin.miapi.modules.properties.slot.SlotProperty;
import smartin.miapi.network.Networking;
import smartin.miapi.registries.RegistryInventory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static net.minecraft.world.inventory.InventoryMenu.BLOCK_ATLAS;
import static net.minecraft.world.inventory.InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD;

/**
 * This is the screen handler class for miapis default Crafting Screen.
 */
public class CraftingScreenHandler extends AbstractContainerMenu {
    private final ContainerLevelAccess context;
    private static final String PACKET_ID = ":crafting_packet_";
    public Container inventory;
    public Inventory playerInventory;
    public @Nullable ModularWorkBenchEntity blockEntity;
    public final ContainerData delegate;
    public final String packetID;
    public final String editPacketID;
    public final String packetIDSlotAdd;
    public final String packetIDSlotRemove;
    public CraftingScreenHandler craftingScreenHandler;
    private final List<Slot> mutableSlots = new ArrayList<>();

    static final ResourceLocation[] EMPTY_ARMOR_SLOT_TEXTURES = new ResourceLocation[]{InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS, InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS, InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE, InventoryMenu.EMPTY_ARMOR_SLOT_HELMET};
    private static final EquipmentSlot[] EQUIPMENT_SLOT_ORDER = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    /**
     * Constructs a new CraftingScreenHandler instance with the specified sync ID and player inventory.
     *
     * @param syncId          the sync ID
     * @param playerInventory the player inventory
     */
    public CraftingScreenHandler(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, null, ContainerLevelAccess.NULL, new SimpleContainerData(7));
        craftingScreenHandler = this;
    }

    public CraftingScreenHandler(int syncId, Inventory playerInventory, ModularWorkBenchEntity benchEntity, ContainerData delegate) {
        this(syncId, playerInventory, benchEntity, ContainerLevelAccess.NULL, delegate);
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
    public CraftingScreenHandler(int syncId, Inventory playerInventory, @Nullable ModularWorkBenchEntity benchEntity, ContainerLevelAccess context, ContainerData delegate) {
        super(RegistryInventory.craftingScreenHandler, syncId);
        craftingScreenHandler = this;
        packetID = Miapi.MOD_ID + PACKET_ID + playerInventory.player.getStringUUID() + "_" + syncId;
        editPacketID = Miapi.MOD_ID + PACKET_ID + "_edit_" + playerInventory.player.getStringUUID() + "_" + syncId;
        packetIDSlotAdd = Miapi.MOD_ID + PACKET_ID + "_" + playerInventory.player.getStringUUID() + "_" + syncId + "_slot_add";
        packetIDSlotRemove = Miapi.MOD_ID + PACKET_ID + "_" + playerInventory.player.getStringUUID() + "_" + syncId + "_slot_remove";
        this.delegate = delegate;
        this.playerInventory = playerInventory;
        this.blockEntity = benchEntity;
        if (playerInventory.player instanceof ServerPlayer) {
            Networking.registerC2SPacket(packetID, (buffer, player) -> {
                CraftAction action = new CraftAction(buffer, blockEntity);
                Miapi.server.execute(() -> {
                    action.setItem(inventory.getItem(0));
                    action.linkInventory(inventory, 1);
                    if (action.canPerform()) {
                        ItemStack stack = action.perform();
                        inventory.setItem(0, stack);
                        if (blockEntity != null) {
                            blockEntity.setItem(stack);
                            blockEntity.saveAndSync();
                        }
                        this.slotsChanged(inventory);
                    }
                });
            });
            Networking.registerC2SPacket(packetIDSlotAdd, (buffer, player) -> {
                int invId = buffer.readInt();
                int slotId = buffer.readInt();
                Miapi.server.execute(() -> {
                    Slot slot = new Slot(inventory, invId, 0, 0);
                    slot.index = slotId;
                    mutableSlots.add(slot);
                    this.addSlot(slot);
                    slot.index = slotId;
                });
            });
            Networking.registerC2SPacket(packetIDSlotRemove, (buffer, player) -> {
                int slotId = buffer.readInt();
                Miapi.server.execute(() -> {
                    Slot slot = this.getSlot(slotId);
                    mutableSlots.remove(slot);
                    quickMoveStack(playerInventory.player, slotId);
                });
            });
            Networking.registerC2SPacket(editPacketID, (buffer, player) -> {
                EditOption option = RegistryInventory.editOptions.get(buffer.readUtf());
                String[] array = buffer.readUtf().split("\n");
                //TODO: do i need execute on server?
                //Miapi.server.execute(()->{
                ItemStack stack = ModularItemStackConverter.getModularVersion(inventory.getItem(0));
                ModuleInstance root = ItemModule.getModules(stack);
                List<String> position = new ArrayList<>();
                Collections.addAll(position, array);
                ModuleInstance current = root.getPosition(position).copy();

                SlotProperty.ModuleSlot slot = SlotProperty.getSlotIn(current);
                if (slot == null && current != null && current.module != null) {
                    slot = new SlotProperty.ModuleSlot(AllowedSlots.getAllowedSlots(current.module));
                }

                assert option != null;
                SlotProperty.ModuleSlot finalSlot = slot;
                EditOption.EditContext editContext = new EditOption.EditContext() {
                    @Override
                    public void craft(FriendlyByteBuf craftBuffer) {

                    }

                    @Override
                    public void preview(FriendlyByteBuf preview) {

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
                    public @Nullable ModuleInstance getInstance() {
                        return current;
                    }

                    @Override
                    public @Nullable Player getPlayer() {
                        return player;
                    }

                    @Override
                    public @Nullable ModularWorkBenchEntity getWorkbench() {
                        return blockEntity;
                    }

                    @Override
                    public Container getLinkedInventory() {
                        return inventory;
                    }

                    @Override
                    public CraftingScreenHandler getScreenHandler() {
                        return craftingScreenHandler;
                    }
                };
                if (option.isVisible(editContext)) {
                    ItemStack editedStack = option.execute(buffer, editContext);
                    Miapi.server.execute(() -> {
                        inventory.setItem(0, editedStack);
                        if (blockEntity != null) {
                            blockEntity.setItem(editedStack);
                            blockEntity.saveAndSync();
                        }
                        inventory.setChanged();
                        this.slotsChanged(inventory);
                    });
                } else {
                    Miapi.LOGGER.warn("ERROR - Couldn`t verify craft action from client " + player.getStringUUID() + " " + player.getDisplayName().getString() + " This might be a bug or somebody is trying to exploit");
                    Miapi.LOGGER.warn(String.valueOf(current));
                    Miapi.LOGGER.warn(position.toString());
                }
                //});
            });
        }
        this.context = context;
        this.inventory = new SimpleContainer(54) {
            @Override
            public void setChanged() {
                super.setChanged();
                CraftingScreenHandler.this.slotsChanged(this);
            }
        };
        if (blockEntity != null) {
            this.setItem(blockEntity.getItem());
        }
        int yOffset = 2 * 18 + 2 + 12 - 20 + 102 - 1;
        int xOffset = 30 + 2 * 18 + 138 + 7 + 2 - 3;
        for (int j = 0; j < 3; ++j) {
            for (int k = 0; k < 9; ++k) {
                this.addSlot(new PlayerInventorySlot(playerInventory, k + j * 9 + 9, k * 18 + xOffset - 15, j * 18 + yOffset - 14));
            }
        }

        for (int j = 0; j < 9; ++j) {
            this.addSlot(new PlayerInventorySlot(playerInventory, j, j * 18 + xOffset - 15, 3 * 18 + 4 + yOffset - 14));
        }

        this.addSlot(new ModifyingSlot(inventory, 0, 112 - 60 - 15 - 3, 118 + 72 - 14, blockEntity));
        for (int i = 0; i < 4; ++i) {
            final EquipmentSlot equipmentSlot = EQUIPMENT_SLOT_ORDER[i];
            int offset = i < 2 ? 0 : 1;
            this.addSlot(new Slot(playerInventory, 39 - i, 87 - 3 + i * 18 - offset - 15, 118 + 71 - 14) {
                @Override
                public int getMaxStackSize() {
                    return 1;
                }

                public boolean mayPlace(ItemStack itemStack) {
                    return equipmentSlot == playerInventory.player.getEquipmentSlotForItem(itemStack);
                }

                @Override
                public boolean mayPickup(Player playerEntity) {
                    ItemStack itemStack = this.getItem();
                    return (itemStack.isEmpty() || playerEntity.isCreative() || !EnchantmentHelper.has(itemStack, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE)) && super.mayPickup(playerEntity);
                }

                @Override
                public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                    return Pair.of(BLOCK_ATLAS, EMPTY_ARMOR_SLOT_TEXTURES[equipmentSlot.getIndex()]);
                }
            });
        }
        this.addSlot(new Slot(playerInventory, 40, 111 - 61 + 5 * 18 + 18 - 15 - 3, 118 + 71 - 14) {
            @Override
            public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                return Pair.of(BLOCK_ATLAS, EMPTY_ARMOR_SLOT_SHIELD);
            }
        });
        this.addDataSlots(delegate);
    }

    public boolean notClient() {
        return !playerInventory.player.level().isClientSide;
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (notClient()) {
            blockEntity.setItem(inventory.getItem(0));
            blockEntity.saveAndSync();
        }
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

            BlockEntity be = playerInventory.player.level().getBlockEntity(new BlockPos(x, y, z));
            if (be instanceof ModularWorkBenchEntity casted) blockEntity = casted;
        }
        updateBE();
    }

    /**
     * Removes the given {@link Slot} from the screen and sends a packet to the server to remove it as well.
     *
     * @param slot the slot to be removed
     */
    public void removeSlotByClient(Slot slot) {
        if (!slots.contains(slot))
            return;
        quickMoveStack(playerInventory.player, slot.index);
        slot.setChanged();
        if (slot instanceof MutableSlot mutableSlot) {
            mutableSlot.setEnabled(false);
        }
        playerInventory.setChanged();
        inventory.setChanged();
        FriendlyByteBuf buf = Networking.createBuffer();
        buf.writeInt(slot.index);
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
        FriendlyByteBuf buf = Networking.createBuffer();
        buf.writeInt(slot.getContainerSlot());
        buf.writeInt(slot.index);
        mutableSlots.add(slot);
        Networking.sendC2S(packetIDSlotAdd, buf);

        slot.setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.context.execute((world, pos) -> {
            this.clearContainer(player, this.inventory);
        });

        // Transfer the items in the inventory to the player's inventory
        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            if (i == 0) continue;
            ItemStack stack = this.inventory.getItem(i);
            if (!stack.isEmpty()) {
                if (!player.getInventory().add(stack)) {
                    player.drop(stack, false);
                }
                this.inventory.setItem(i, ItemStack.EMPTY);
            }
        }
        Networking.unRegisterC2SPacket(packetID);
        Networking.unRegisterC2SPacket(packetIDSlotAdd);
        Networking.unRegisterC2SPacket(packetIDSlotRemove);
        Networking.unRegisterC2SPacket(editPacketID);
        if (notClient() && blockEntity != null) blockEntity.saveAndSync();
    }

    public void setItem(ItemStack stack) {
        inventory.setItem(0, stack);
        inventory.setChanged();
        if (blockEntity != null) {
            blockEntity.setItem(stack);
            if (notClient()) blockEntity.saveAndSync();
        }
    }

    private void updateBE() {
        if (notClient()) {
            blockEntity.setItem(inventory.getItem(0));
            blockEntity.saveAndSync();
        }
    }

    public ItemStack quickMoveStack(Player player, int index) {
        inventory.setChanged();
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack itemStack2 = slot.getItem();
            itemStack = itemStack2.copy();
            if (index >= 36) {
                //case 1: tool slot to player
                slot.onTake(player, itemStack2);
                if (!this.moveItemStackTo(itemStack2, 0, 36, true)) {
                    return ItemStack.EMPTY;
                }

                if (index == 36 && blockEntity != null) {
                    blockEntity.setItem(itemStack2);
                    if (notClient()) blockEntity.saveAndSync();
                }
                slot.setChanged();
            } else {
                //PlayerInv
                for (Slot slot1 : mutableSlots) {
                    if (slot1.index >= 36) {
                        if (!this.moveItemStackTo(itemStack2, slot1.index, slot1.index + 1, true)) {
                            return ItemStack.EMPTY;
                        }
                    }
                }
                if ((slots.get(36).getItem().isEmpty() || slots.get(36).getItem().getItem().equals(itemStack2.getItem())) && !this.moveItemStackTo(itemStack2, 36, 37, true)) {
                    return ItemStack.EMPTY;
                }
                slot.setChanged();
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    protected void clearContainer(Player player, Container inventory) {
        if (!player.isAlive() || player instanceof ServerPlayer serverPlayerEntity && serverPlayerEntity.hasDisconnected()) {
            for (int i = 0; i < inventory.getContainerSize(); ++i) {
                if (i == 0) continue;
                player.drop(inventory.removeItemNoUpdate(i), false);
            }
            return;
        }
        for (int i = 0; i < inventory.getContainerSize(); ++i) {
            if (i == 0) continue;
            Inventory currentPlayerInv = player.getInventory();
            if (!(currentPlayerInv.player instanceof ServerPlayer)) continue;
            currentPlayerInv.placeItemBackInInventory(inventory.removeItemNoUpdate(i));
        }
    }

    public static class ModifyingSlot extends Slot {
        protected final ModularWorkBenchEntity blockEntity;

        public ModifyingSlot(Container inventory, int index, int x, int y, ModularWorkBenchEntity blockEntity) {
            super(inventory, index, x, y);
            this.blockEntity = blockEntity;
        }

        public boolean notClient() {
            return blockEntity != null && blockEntity.hasLevel() && !blockEntity.getLevel().isClientSide;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return true;
        }

        @Override
        public int getMaxStackSize() {
            return 64;
        }

        @Override
        public void setByPlayer(ItemStack stack) {
            super.setByPlayer(stack);
            if (notClient()) {
                blockEntity.setItem(stack);
                blockEntity.saveAndSync();
            }
            this.setChanged();
        }
    }

    public static class PlayerInventorySlot extends Slot {
        public PlayerInventorySlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }
    }
}
