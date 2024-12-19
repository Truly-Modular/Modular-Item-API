package smartin.miapi.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(CrossbowItem.class)
public interface CrossbowItemAccessor {
    @Invoker
    static boolean callTryLoadProjectiles(LivingEntity shooter, ItemStack crossbowStack) {
        throw new UnsupportedOperationException();
    }

    @Accessor
    boolean isStartSoundPlayed();

    @Accessor
    void setStartSoundPlayed(boolean startSoundPlayed);

    @Accessor
    boolean isMidLoadSoundPlayed();

    @Accessor
    void setMidLoadSoundPlayed(boolean midLoadSoundPlayed);

    @Invoker
    CrossbowItem.ChargingSounds callGetChargingSounds(ItemStack stack);
}
