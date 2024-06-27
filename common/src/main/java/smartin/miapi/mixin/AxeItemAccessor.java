package smartin.miapi.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.level.block.Block;

@Mixin(AxeItem.class)
public interface AxeItemAccessor {
    @Accessor
    static Map<Block, Block> getSTRIPPED_BLOCKS() {
        throw new UnsupportedOperationException();
    }
}
