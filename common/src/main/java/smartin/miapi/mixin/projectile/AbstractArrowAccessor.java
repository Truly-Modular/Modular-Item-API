package smartin.miapi.mixin.projectile;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractArrow.class)
public interface AbstractArrowAccessor {
    @Accessor("PIERCE_LEVEL")
    static EntityDataAccessor<Byte> getPerceLevelDataPublic() {
        throw new UnsupportedOperationException();
    }

    @Invoker
    void callSetPierceLevel(byte pierceLevel);

    @Accessor
    SoundEvent getSoundEvent();

    @Accessor
    void setSoundEvent(SoundEvent soundEvent);

    @Accessor
    void setPickupItemStack(ItemStack pickupItemStack);
}
