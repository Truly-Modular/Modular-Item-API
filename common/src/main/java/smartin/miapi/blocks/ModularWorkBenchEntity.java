package smartin.miapi.blocks;

import dev.architectury.event.EventResult;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.event.BlockPositionSource;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.PositionSource;
import net.minecraft.world.event.listener.GameEventListener;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.client.gui.SimpleScreenHandlerListener;
import smartin.miapi.client.gui.crafting.CraftingScreenHandler;
import smartin.miapi.craft.stat.CraftingStat;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.item.StatProvidingItem;
import smartin.miapi.modules.properties.StatProvisionProperty;
import smartin.miapi.registries.RegistryInventory;

public class ModularWorkBenchEntity extends BlockEntity implements NamedScreenHandlerFactory, GameEventListener {
    protected final PropertyDelegate propertyDelegate;
    private ItemStack stack;
    public final CraftingStat.StatMap<?> blockStats = new CraftingStat.StatMap<>();
    public final CraftingStat.StatMap<?> itemStats = new CraftingStat.StatMap<>();
    public int x;
    public int y;
    public int z;
    protected BlockPositionSource blockPositionSource;
    public long lastItemStatUpdate = -100;

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
        this.markDirty();
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
            stack = ItemStack.fromNbt(tag.getCompound("Item"));

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
        markDirty();
        if (hasWorld())
            world.updateListeners(pos, world.getBlockState(pos), getCachedState(), 3);
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("test");
    }

    // Set player to null if you don't have one. This will make items that try to provide stats without the StatProvisionProperty ignored.
    public void updateStatsFromItems(Iterable<ItemStack> inventory, @Nullable PlayerEntity player) {
        EventResult result = MiapiEvents.ITEM_STAT_UPDATE.invoker().call(this, inventory, player);
        if (result.interruptsFurtherEvaluation()) return;
        itemStats.clear();
        inventory.forEach(stack -> {
            CraftingStat.StatMap<?> stats = StatProvisionProperty.property.get(stack);
            if (stats == null) {
                if (player != null && stack.getItem() instanceof StatProvidingItem item)
                    stats = item.getStats(this, player, stack);
                else return;
            }
            stats.forEach((stat, inst) -> setItemStat((CraftingStat<Object>) stat, inst)); // casting here weirdly because uh java is weird
        });
    }
    public void updateBlockStats(@Nullable PlayerEntity player) {
        EventResult result = MiapiEvents.BLOCK_STAT_UPDATE.invoker().call(this, player);
        if (result.interruptsFurtherEvaluation()) return;
        blockStats.clear();
        world.emitGameEvent(RegistryInventory.statUpdateEvent, pos, new GameEvent.Emitter(player, getCachedState()));
    }
    public void updateAllStats(Iterable<ItemStack> inventory, PlayerEntity player) {
        if (hasWorld() && !world.isClient) {
            updateBlockStats(player);
            updateStatsFromItems(inventory, player);
            world.updateListeners(pos, world.getBlockState(pos), getCachedState(), 3);
        }
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        updateBlockStats(player);
        CraftingScreenHandler handler = new CraftingScreenHandler(syncId, playerInventory, this, propertyDelegate);
        handler.addListener(new SimpleScreenHandlerListener((h, slotId, stack) -> {
            long currentWorldTime;
            if (
                    hasWorld() && !world.isClient &&
                            lastItemStatUpdate != (currentWorldTime = world.getTime()) &&
                            slotId < 36 && h instanceof CraftingScreenHandler csh) {
                updateStatsFromItems(csh.playerInventory.main, csh.playerInventory.player);
                lastItemStatUpdate = currentWorldTime;
                world.updateListeners(pos, world.getBlockState(pos), getCachedState(), 3);
            }
        }));
        return handler;
    }

    @Override
    public PositionSource getPositionSource() {
        if (blockPositionSource == null) blockPositionSource = new BlockPositionSource(pos);
        return blockPositionSource;
    }

    @Override
    public int getRange() {
        return 16;
    }

    @Override
    public boolean listen(ServerWorld world, GameEvent event, GameEvent.Emitter emitter, Vec3d emitterPos) {
        if (event.equals(RegistryInventory.statProviderUpdatedEvent)) {
            if (hasWorld()) {
                blockStats.clear();
                updateBlockStats(null);
                this.world.updateListeners(pos, world.getBlockState(pos), getCachedState(), 3);
                return true;
            }
        }
        return false;
    }
}