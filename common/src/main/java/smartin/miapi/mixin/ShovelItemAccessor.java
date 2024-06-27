package smartin.miapi.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(ShovelItem.class)
public interface ShovelItemAccessor {
    @Accessor
    static Map<Block, BlockState> getPATH_STATES() {
        throw new UnsupportedOperationException();
    }
}
