package smartin.miapi.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.properties.armor.IsPiglinGold;

@Mixin(PiglinAi.class)
public abstract class PiglinBrainMixin {

    @Inject(method = "isWearingGold", at = @At("HEAD"), cancellable = true)
    private static void miapi$isGoldItemBypass(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        for (ItemStack armorItem : entity.getArmorSlots()) {
            if (armorItem.getItem() instanceof ModularItem && IsPiglinGold.isPiglinGoldItem(armorItem)) {
                //IsPiglinGold.isPiglinGoldItem(armorItem)
                cir.setReturnValue(true);
                return;
            }
        }
    }
}
