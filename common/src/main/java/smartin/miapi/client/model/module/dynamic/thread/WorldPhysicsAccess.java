package smartin.miapi.client.model.module.dynamic.thread;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import oshi.annotation.concurrent.NotThreadSafe;
import oshi.annotation.concurrent.ThreadSafe;
import smartin.miapi.events.ClientEvents;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public final class WorldPhysicsAccess {
    public static final long CACHE_SIZE = 10000;
    public static final long CACHE_LIFETIME = 20;
    public static final TimeUnit CACHE_LIFETIME_UNIT = TimeUnit.SECONDS;
    private final Set<BlockPos> queuedPositions = ConcurrentHashMap.newKeySet();
    private final Cache<BlockPos, VoxelShape> voxelShapeCache = CacheBuilder.newBuilder()
            .maximumSize(CACHE_SIZE)
            .expireAfterAccess(CACHE_LIFETIME, CACHE_LIFETIME_UNIT)
            .build();
    public static WorldPhysicsAccess CLIENT_WORLD = new WorldPhysicsAccess();

    static {
        ClientEvents.CLIENT_TICK.register(client -> {
            if (client.level != null) {
                CLIENT_WORLD.drainQueue(client.level);
            }
        });
        ClientLifecycleEvent.CLIENT_LEVEL_LOAD.register(clientLevel -> CLIENT_WORLD.clearCache());
    }

    @Nullable
    @ThreadSafe
    public VoxelShape getVoxelShape(@NotNull BlockPos position) {
        BlockPos key = new BlockPos(position);

        VoxelShape shape = voxelShapeCache.getIfPresent(key);

        if (shape != null) {
            return shape.isEmpty() ? null : shape;
        }

        enqueueUpdate(key);

        return null;
    }

    @NotThreadSafe
    public void blockUpdate(Level level, BlockPos pos) {
        VoxelShape shape = level.getBlockState(pos).getCollisionShape(level, pos);
        voxelShapeCache.put(new BlockPos(pos), shape != null ? shape : Shapes.empty());
    }


    @NotThreadSafe
    public void blockUpdate(Level level, BlockPos pos, BlockState state) {
        if (voxelShapeCache.getIfPresent(pos) != null) {
            VoxelShape shape = state.getCollisionShape(level, pos);
            voxelShapeCache.put(new BlockPos(pos), shape != null ? shape : Shapes.empty());
        }
    }

    @NotThreadSafe
    public void drainQueue(@NotNull Level level) {
        if (this.queuedPositions.isEmpty()) {
            return;
        }
        Set<BlockPos> todo = new HashSet<>(this.queuedPositions);
        queuedPositions.clear();
        todo.forEach(pos -> {
            blockUpdate(level, pos);
        });
    }


    private void enqueueUpdate(@NotNull BlockPos position) {
        BlockPos key = new BlockPos(position);

        queuedPositions.add(key);
    }

    @ThreadSafe
    public void clearCache() {
        voxelShapeCache.invalidateAll();
        queuedPositions.clear();
    }
}
