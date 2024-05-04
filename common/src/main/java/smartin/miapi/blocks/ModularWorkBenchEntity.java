package smartin.miapi.blocks;

import com.redpxnda.nucleus.codec.misc.MiscCodecs;
import com.redpxnda.nucleus.util.MiscUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.event.BlockPositionSource;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.PositionSource;
import net.minecraft.world.event.listener.GameEventListener;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.crafting.CraftingScreenHandler;
import smartin.miapi.craft.stat.CraftingStat;
import smartin.miapi.craft.stat.StatProvidersMap;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.registries.RegistryInventory;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ModularWorkBenchEntity extends BlockEntity implements NamedScreenHandlerFactory, GameEventListener {
    public static final Map<GameEvent, CustomGameEventHandler> gameEventHandlers = MiscUtil.initialize(new ConcurrentHashMap<>(), map -> {
        map.put(RegistryInventory.statProviderCreatedEvent, (bench, world, event, emitter, emitterPos) -> {
            if (emitter.affectedState() != null) {
                BlockPos pos = new BlockPos((int) emitterPos.x, (int) emitterPos.y, (int) emitterPos.z);
                Block block = emitter.affectedState().getBlock();
                if (block instanceof IStatProvidingBlock statProvider) {
                    bench.persistentStats.computeIfAbsent(pos.toString(), s -> new StatProvidersMap());
                    bench.persistentStats.get(pos.toString()).putAll(statProvider.getProviders(bench, emitter.affectedState(), pos, world));
                }
            }
            return false;
        });
        map.put(RegistryInventory.statProviderRemovedEvent, (bench, world, event, emitter, emitterPos) -> {
            if (emitter.affectedState() != null) {
                BlockPos pos = new BlockPos((int) emitterPos.x, (int) emitterPos.y, (int) emitterPos.z);
                bench.persistentStats.remove(pos.toString());
            }
            return false;
        });
    });

    protected final PropertyDelegate propertyDelegate;
    private ItemStack stack;
    public final Map<String, StatProvidersMap> persistentStats = new HashMap<>();
    protected final Map<CraftingStat, Object> stats = new HashMap<>();
    public int x;
    public int y;
    public int z;
    protected BlockPositionSource blockPositionSource;

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

    public <T> T getStat(CraftingStat<T> stat) {
        return (T) stats.get(stat);
    }

    @Override
    public void writeNbt(NbtCompound tag) {
        super.writeNbt(tag);

        tag.put("Item", stack.writeNbt(new NbtCompound()));

        NbtCompound persisStatsNbt = new NbtCompound();
        persistentStats.forEach((key, val) -> persisStatsNbt.put(key, StatProvidersMap.MODULELESS_CODEC.encodeStart(NbtOps.INSTANCE, val).getOrThrow(false, s -> Miapi.LOGGER.error("Failed to encode persistent StatProvidersMap for MWBE! -> {}", s))));

        NbtCompound statsNbt = new NbtCompound();
        stats.forEach((stat, inst) -> {
            statsNbt.put(RegistryInventory.craftingStats.findKey(stat), stat.saveToNbt(inst));
        });

        tag.put("PersistentStats", persisStatsNbt);
        tag.put("Stats", statsNbt);
    }


    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
        persistentStats.clear();
        stats.clear();

        if (tag.contains("Item")) stack = ItemStack.fromNbt(tag.getCompound("Item"));

        NbtCompound persisStatsNbt = tag.getCompound("PersistentStats");
        persisStatsNbt.getKeys().forEach(key -> {
            persistentStats.put(key, MiscCodecs.quickParse(NbtOps.INSTANCE, persisStatsNbt.getCompound(key), StatProvidersMap.MODULELESS_CODEC, s -> Miapi.LOGGER.error("Failed to decode persistent StatProvidersMap for MWBE! -> {}", s)));
        });

        NbtCompound statsNbt = tag.getCompound("Stats");
        statsNbt.getKeys().forEach(key -> {
            CraftingStat stat = RegistryInventory.craftingStats.get(key);
            if (stat == null) {
                Miapi.LOGGER.warn("Found unknown CraftingStat id '{}'!", key);
                return;
            }
            stats.put(stat, stat.createFromNbt(statsNbt.get(key)));
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
        if (hasWorld()) world.updateListeners(pos, world.getBlockState(pos), getCachedState(), 3);
        handlers.forEach(weakReference -> {
            CraftingScreenHandler handler = weakReference.get();
            if (handler != null) {
                if (!ItemStack.areEqual(handler.inventory.getStack(0),getItem())) {
                    handler.inventory.setStack(0, getItem());
                }
            }
        });
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("test");
    }

    List<WeakReference<CraftingScreenHandler>> handlers = new ArrayList<>();

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        CraftingScreenHandler handler = new CraftingScreenHandler(syncId, playerInventory, this, propertyDelegate);

        stats.clear();
        StatProvidersMap providers = new StatProvidersMap();
        MiapiEvents.STAT_UPDATE_EVENT.invoker().update(this, providers, syncId, playerInventory, player, handler);
        persistentStats.forEach((key, map) -> providers.putAll(map));
        stats.putAll(providers.evaluateAll());
        saveAndSync();
        handlers.add(new WeakReference<>(handler));

        /*handler.addListener(new SimpleScreenHandlerListener((h, slotId, itemStack) -> {
            long currentWorldTime;
            if (
                    hasWorld() && !world.isClient &&
                            lastItemStatUpdate != (currentWorldTime = world.getTime()) &&
                            slotId < 36 && h instanceof CraftingScreenHandler csh) {
                updateStatsFromItems(csh.playerInventory.main, csh.playerInventory.player);
                lastItemStatUpdate = currentWorldTime;
                world.updateListeners(pos, world.getBlockState(pos), getCachedState(), 3);
            }
        }));*/
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
        CustomGameEventHandler handler = gameEventHandlers.get(event);
        if (handler == null) return false;
        return handler.handle(this, world, event, emitter, emitterPos);
    }

    public interface CustomGameEventHandler {
        boolean handle(ModularWorkBenchEntity bench, ServerWorld world, GameEvent event, GameEvent.Emitter emitter, Vec3d emitterPos);
    }
}