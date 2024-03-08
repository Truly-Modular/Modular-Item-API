package smartin.miapi.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import smartin.miapi.registries.RegistryInventory;

@Mixin(EnderPearlEntity.class)
public class EnderpearlEntityMixin {

    @Inject(method = "onCollision(Lnet/minecraft/util/hit/HitResult;)V", at = @At("HEAD"), cancellable = true)
    private void miapi$teleportBlockEffect(HitResult hitResult, CallbackInfo ci) {
        EnderPearlEntity entity = (EnderPearlEntity) (Object) this;
        Entity owner = entity.getOwner();
        if (entity != null && owner instanceof LivingEntity livingEntity) {
            if (livingEntity.hasStatusEffect(RegistryInventory.teleportBlockEffect)) {
                ci.cancel();
            }
        }
    }
}
