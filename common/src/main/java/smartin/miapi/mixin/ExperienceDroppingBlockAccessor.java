package smartin.miapi.mixin;

import net.minecraft.block.ExperienceDroppingBlock;
import net.minecraft.util.math.intprovider.IntProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ExperienceDroppingBlock.class)
public interface ExperienceDroppingBlockAccessor {
    @Accessor
    IntProvider getExperienceDropped();
}
