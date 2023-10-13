package smartin.miapi.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.properties.IsPiglinGold;

@Mixin(PiglinBrain.class)
public abstract class PiglinBrainMixin {

    @Inject(method = "wearsGoldArmor(Lnet/minecraft/entity/LivingEntity;)Z", at = @At("HEAD"), cancellable = true)
    private static void miapi$isGoldItemBypass(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        for (ItemStack armorItem : entity.getArmorItems()) {
            if (armorItem.getItem() instanceof ModularItem && IsPiglinGold.isPiglinGoldItem(armorItem)) {
                //IsPiglinGold.isPiglinGoldItem(armorItem)
                cir.setReturnValue(true);
                return;
            }
        }
    }
}
