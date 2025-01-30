package smartin.miapi.mixin;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractArrow.class)
public interface AbstractArrowAccessor {
    @Invoker
    void callSetPierceLevel(byte pierceLevel);

    @Accessor
    SoundEvent getSoundEvent();

    @Accessor
    void setSoundEvent(SoundEvent soundEvent);
}
