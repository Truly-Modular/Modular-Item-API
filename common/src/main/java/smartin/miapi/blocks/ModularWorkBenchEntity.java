package smartin.miapi.blocks;

import com.redpxnda.nucleus.util.MiscUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.phys.Vec3;
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

public class ModularWorkBenchEntity extends BlockEntity implements MenuProvider, GameEventListener {
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

    protected final ContainerData propertyDelegate;
    public ItemStack stack;
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
        this.propertyDelegate = new ContainerData() {
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
            public int getCount() {
                return 7;
            }
        };
        this.setChanged();
        AnvilBlock block;
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
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider wrapperLookup) {
        super.saveAdditional(tag, wrapperLookup);
        CompoundTag persisStatsNbt = new CompoundTag();

        //TODO:persistentStats are disabled for now
        //persistentStats.forEach((key, val) -> persisStatsNbt.put(key, StatProvidersMap.MODULELESS_CODEC.encodeStart(NbtOps.INSTANCE, val).getOrThrow(false, s -> Miapi.LOGGER.error("Failed to encode persistent StatProvidersMap for MWBE! -> {}", s))));

        CompoundTag statsNbt = new CompoundTag();
        stats.forEach((stat, inst) -> {
            statsNbt.put(RegistryInventory.craftingStats.findKey(stat).toString(), stat.saveToNbt(inst));
        });

        if (!getItem().isEmpty()) {
            tag.put("item", ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, getItem()).getOrThrow());
        } else {
            tag.remove("item");
        }

        tag.put("PersistentStats", persisStatsNbt);
        tag.put("Stats", statsNbt);
    }


    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider wrapperLookup) {
        super.loadAdditional(tag, wrapperLookup);
        persistentStats.clear();
        stats.clear();

        if (tag.contains("item")) {
            stack = ItemStack.parse(wrapperLookup, tag.getCompound("item")).get();
            setItem(stack);
        } else {
            stack = ItemStack.EMPTY;
            setItem(stack);
        }

        CompoundTag persisStatsNbt = tag.getCompound("PersistentStats");
        persisStatsNbt.getAllKeys().forEach(key -> {
            //TODO:this did not work, idk the stats stuff though
            //persistentStats.put(key, MiscCodecs.quickParse(NbtOps.INSTANCE, persisStatsNbt.getCompound(key), StatProvidersMap.MODULELESS_CODEC, s -> Miapi.LOGGER.error("Failed to decode persistent StatProvidersMap for MWBE! -> {}", s)));
        });

        CompoundTag statsNbt = tag.getCompound("Stats");
        statsNbt.getAllKeys().forEach(key -> {
            CraftingStat stat = RegistryInventory.craftingStats.get(key);
            if (stat == null) {
                Miapi.LOGGER.warn("Found unknown CraftingStat id '{}'!", key);
                return;
            }
            stats.put(stat, stat.createFromNbt(statsNbt.get(key)));
        });
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this, BlockEntity::saveWithFullMetadata);
    }

    public void saveAndSync() {
        setChanged();
        if (hasLevel()) level.sendBlockUpdated(worldPosition, level.getBlockState(worldPosition), getBlockState(), 3);
        handlers.forEach(weakReference -> {
            CraftingScreenHandler handler = weakReference.get();
            if (handler != null) {
                if (!ItemStack.matches(handler.inventory.getItem(0), getItem())) {
                    handler.inventory.setItem(0, getItem());
                }
            }
        });
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("test");
    }

    List<WeakReference<CraftingScreenHandler>> handlers = new ArrayList<>();

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
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
    public PositionSource getListenerSource() {
        if (blockPositionSource == null) blockPositionSource = new BlockPositionSource(worldPosition);
        return blockPositionSource;
    }

    @Override
    public int getListenerRadius() {
        return 16;
    }


    @Override
    public boolean handleGameEvent(ServerLevel world, Holder<GameEvent> registryEntry, GameEvent.Context emitter, Vec3 emitterPos) {
        CustomGameEventHandler handler = gameEventHandlers.get(registryEntry.value());
        if (handler == null) return false;
        return handler.handle(this, world, registryEntry.value(), emitter, emitterPos);
    }

    public interface CustomGameEventHandler {
        boolean handle(ModularWorkBenchEntity bench, ServerLevel world, GameEvent event, GameEvent.Context emitter, Vec3 emitterPos);
    }
}