package smartin.miapi.mixin;

import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Slot.class)
public interface SlotAccessor {
    @Mutable
    @Accessor("y")
    void setYMiapi(int y);

    @Mutable
    @Accessor("x")
    void setXMiapi(int x);
}
