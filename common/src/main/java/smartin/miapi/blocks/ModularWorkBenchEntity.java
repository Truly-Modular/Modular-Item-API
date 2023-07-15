package smartin.miapi.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import net.minecraft.item.ItemStack;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.client.gui.crafting.CraftingScreenHandler;
import smartin.miapi.craft.stat.CraftingStat;
import smartin.miapi.item.StatProvidingItem;
import smartin.miapi.modules.properties.StatProvisionProperty;
import smartin.miapi.registries.RegistryInventory;

import java.util.List;

public class ModularWorkBenchEntity extends BlockEntity implements NamedScreenHandlerFactory {
    protected final PropertyDelegate propertyDelegate;
    private ItemStack stack;
    public final CraftingStat.StatMap<?> blockStats = new CraftingStat.StatMap<>();
    public final CraftingStat.StatMap<?> itemStats = new CraftingStat.StatMap<>();
    public int x;
    public int y;
    public int z;

    public ModularWorkBenchEntity(BlockPos pos, BlockState state) {
        super(RegistryInventory.modularWorkBenchEntityType, pos, state);
        this.stack = ItemStack.EMPTY;
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
        this.propertyDelegate = new PropertyDelegate() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> 1;
                    case 1 -> (short) (x >> 16);
                    case 2 -> (short) x;
                    case 3 -> (short) (y >> 16);
                    case 4 -> (short) y;
                    case 5 -> (short) (z >> 16);
                    case 6 -> (short) z;
                    default -> -1;
                };
            }

            @Override
            public void set(int index, int value) {
                // set unneeded, values read directly
            }

            @Override
            public int size() {
                return 7;
            }
        };
    }

    public void setItem(ItemStack stack) {
        this.stack = stack == null ? ItemStack.EMPTY : stack.copy();
    }
    public ItemStack getItem() {
        return stack;
    }

    public <T> T getBlockStat(CraftingStat<T> stat) {
        return blockStats.getOrDefault(stat);
    }
    public <T> T getItemStat(CraftingStat<T> stat) {
        return itemStats.getOrDefault(stat);
    }
    public <T> T getStat(CraftingStat<T> stat) {
        return stat.merge(this, getItemStat(stat), getBlockStat(stat));
    }
    public <T> void setBlockStat(CraftingStat<T> stat, T val) {
        T old = getBlockStat(stat);
        blockStats.set(stat, stat.merge(this, old, val));
    }
    public <T> void setItemStat(CraftingStat<T> stat, T val) {
        T old = getItemStat(stat);
        itemStats.set(stat, stat.merge(this, old, val));
    }
    public <T> void overrideBlockStat(CraftingStat<T> stat, T val) {
        blockStats.set(stat, val);
    }
    public <T> void overrideItemStat(CraftingStat<T> stat, T val) {
        itemStats.set(stat, val);
    }

    @Override
    public void writeNbt(NbtCompound tag) {
        super.writeNbt(tag);

        tag.put("Item", stack.writeNbt(new NbtCompound()));

        NbtCompound statsBlock = new NbtCompound();
        blockStats.forEach((stat, val) -> {
            statsBlock.put(RegistryInventory.craftingStats.findKey(stat), stat.saveToNbt(val));
        });
        NbtCompound statsItem = new NbtCompound();
        itemStats.forEach((stat, val) -> {
            statsItem.put(RegistryInventory.craftingStats.findKey(stat), stat.saveToNbt(val));
        });
        tag.put("BlockStats", statsBlock);
        tag.put("ItemStats", statsItem);
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
        blockStats.clear();
        itemStats.clear();

        if (tag.contains("Item"))
            this.stack = ItemStack.fromNbt(tag.getCompound("Item"));

        NbtCompound blocks = tag.getCompound("BlockStats");
        blocks.getKeys().forEach(key -> {
            NbtElement val = blocks.get(key);
            CraftingStat stat = RegistryInventory.craftingStats.get(key);
            if (stat != null)
                blockStats.set(stat, stat.createFromNbt(val));
        });
        NbtCompound items = tag.getCompound("ItemStats");
        items.getKeys().forEach(key -> {
            NbtElement val = items.get(key);
            CraftingStat stat = RegistryInventory.craftingStats.get(key);
            if (stat != null)
                blockStats.set(stat, stat.createFromNbt(val));
        });
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        NbtCompound tag = new NbtCompound();
        writeNbt(tag);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this, be -> be.createNbt());
    }

    public void saveAndSync() {
        this.markDirty();
        if (this.hasWorld())
            this.world.updateListeners(pos, world.getBlockState(pos), this.getCachedState(), 3);
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("test");
    }

    // Set player to null if you don't have one. This will make items that try to provide stats without the StatProvisionProperty ignored.
    public void updateStatsFromItems(List<ItemStack> inventory, @Nullable PlayerEntity player) {
        inventory.forEach(stack -> {
            CraftingStat.StatMap<?> stats = StatProvisionProperty.property.get(stack);
            if (stats == null) {
                if (player != null && stack.getItem() instanceof StatProvidingItem item)
                    stats = item.getStats(this, inventory, player, stack);
                else return;
            }
            stats.forEach((stat, inst) -> setItemStat((CraftingStat<Object>) stat, inst)); // casting here weirdly because uh java is weird
        });
    }
    public void updateAllStats(List<ItemStack> inventory, PlayerEntity player) {
        if (hasWorld() && !world.isClient) {
            blockStats.clear();
            itemStats.clear();
            world.emitGameEvent(RegistryInventory.statUpdateEvent, pos, new GameEvent.Emitter(player, getCachedState()));
            updateStatsFromItems(inventory, player);
            this.world.updateListeners(pos, world.getBlockState(pos), this.getCachedState(), 3);
        }
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        updateAllStats(playerInventory.main, player);
        return new CraftingScreenHandler(syncId, playerInventory, this, propertyDelegate);
    }
}