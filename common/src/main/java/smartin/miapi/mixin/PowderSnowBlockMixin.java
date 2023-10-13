package smartin.miapi.mixin;

import net.minecraft.block.PowderSnowBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.properties.CanWalkOnSnow;

@Mixin(PowderSnowBlock.class)
public abstract class PowderSnowBlockMixin {

    @Inject(method = "canWalkOnPowderSnow(Lnet/minecraft/entity/Entity;)Z", at = @At("HEAD"), cancellable = true)
    private static void miapi$bypassSnowWalk(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof LivingEntity livingEntity) {
            ItemStack boots = livingEntity.getEquippedStack(EquipmentSlot.FEET);
            if (boots.getItem() instanceof ModularItem) {
                cir.setReturnValue(CanWalkOnSnow.canSnowWalk(boots));
            }
        }
    }
}
