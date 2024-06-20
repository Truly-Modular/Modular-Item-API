package smartin.miapi.mixin;

import net.minecraft.entity.mob.EndermanEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import smartin.miapi.registries.RegistryInventory;

@Mixin(EndermanEntity.class)
public class EndermanEntityMixin {

    @Inject(method = "teleportRandomly()Z", at = @At("HEAD"), cancellable = true)
    private void miapi$teleportBlockEffect(CallbackInfoReturnable<Boolean> cir) {
        EndermanEntity entity = (EndermanEntity) (Object) this;
        if(entity.hasStatusEffect(RegistryInventory.teleportBlockEffect)){
            cir.setReturnValue(false);
        }
    }
}
