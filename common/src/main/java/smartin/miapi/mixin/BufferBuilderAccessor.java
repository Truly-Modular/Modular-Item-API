package smartin.miapi.mixin;

import net.minecraft.client.render.BufferBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BufferBuilder.class)
public interface BufferBuilderAccessor {
    @Accessor
    boolean isBuilding();
}
