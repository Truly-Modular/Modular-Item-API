package smartin.miapi.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import smartin.miapi.registries.RegistryInventory;

@Mixin(ThrownEnderpearl.class)
public class EnderpearlEntityMixin {

    @Inject(method = "onHit", at = @At("HEAD"), cancellable = true)
    private void miapi$teleportBlockEffect(HitResult hitResult, CallbackInfo ci) {
        ThrownEnderpearl entity = (ThrownEnderpearl) (Object) this;
        Entity owner = entity.getOwner();
        if (entity != null && owner instanceof LivingEntity livingEntity) {
            if (livingEntity.hasEffect(RegistryInventory.teleportBlockEffect)) {
                ci.cancel();
            }
        }
    }
}
