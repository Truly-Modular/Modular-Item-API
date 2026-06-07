package smartin.miapi.mixin.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(CrossbowItem.class)
public interface CrossbowItemAccessor {
    @Invoker("tryLoadProjectiles")
    static boolean callMiapiTryLoadProjectiles(LivingEntity shooter, ItemStack crossbowStack) {
        throw new UnsupportedOperationException();
    }

    @Accessor("startSoundPlayed")
    void setMiapiStartSoundPlayed(boolean startSoundPlayed);

    @Accessor("midLoadSoundPlayed")
    void setMiapiMidLoadSoundPlayed(boolean midLoadSoundPlayed);

    @Invoker("getChargingSounds")
    CrossbowItem.ChargingSounds getMiapiGetChargeSounds(ItemStack stack);
}
