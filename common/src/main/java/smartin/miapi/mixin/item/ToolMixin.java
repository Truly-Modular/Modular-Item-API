package smartin.miapi.mixin.item;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Tool.class)
public class ToolMixin {
    @Unique
    private final Object2FloatOpenHashMap<BlockState> miningSpeedCache = new Object2FloatOpenHashMap<>();
    @Unique
    private final Object2BooleanOpenHashMap<BlockState> miningDropCache = new Object2BooleanOpenHashMap<>();

    @WrapMethod(
            method = "getMiningSpeed"
    )
    private float cacheMiningSpeed(
            BlockState state,
            Operation<Float> original
    ) {
        return miningSpeedCache.computeIfAbsent(state, (s) -> original.call(state));
    }

    @WrapMethod(
            method = "isCorrectForDrops"
    )
    private boolean cacheCorrectForDrops(
            BlockState state,
            Operation<Boolean> original
    ) {
        return miningDropCache.computeIfAbsent(state, (s) -> original.call(state));
    }
}
