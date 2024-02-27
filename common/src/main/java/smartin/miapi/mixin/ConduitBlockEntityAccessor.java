package smartin.miapi.mixin;

import net.minecraft.block.entity.ConduitBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ConduitBlockEntity.class)
public interface ConduitBlockEntityAccessor {
    @Accessor
    boolean isActive();

    @Accessor
    void setActive(boolean active);
}
