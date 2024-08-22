package smartin.miapi.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.properties.IsPiglinGold;

@Mixin(PiglinBrain.class)
public abstract class PiglinBrainMixin {

    @ModifyReturnValue(method = "wearsGoldArmor(Lnet/minecraft/entity/LivingEntity;)Z", at = @At("RETURN"))
    private static boolean miapi$isGoldItemBypass(boolean original, LivingEntity entity) {
        for (ItemStack armorItem : entity.getArmorItems()) {
            if (armorItem.getItem() instanceof ModularItem && IsPiglinGold.isPiglinGoldItem(armorItem)) {
                return true;
            }
        }
        return original;
    }
}
