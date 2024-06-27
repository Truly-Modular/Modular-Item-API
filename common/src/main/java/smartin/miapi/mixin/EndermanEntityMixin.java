package smartin.miapi.mixin;

import net.minecraft.world.entity.monster.EnderMan;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import smartin.miapi.registries.RegistryInventory;

@Mixin(EnderMan.class)
public class EndermanEntityMixin {

    @Inject(method = "teleportRandomly()Z", at = @At("HEAD"), cancellable = true)
    private void miapi$teleportBlockEffect(CallbackInfoReturnable<Boolean> cir) {
        EnderMan entity = (EnderMan) (Object) this;
        if(entity.hasEffect(RegistryInventory.teleportBlockEffect)){
            cir.setReturnValue(false);
        }
    }
}
