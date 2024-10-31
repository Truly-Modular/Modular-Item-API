package smartin.miapi.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.mob.EndermanEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import smartin.miapi.registries.RegistryInventory;

@Mixin(EndermanEntity.class)
public class EndermanEntityMixin {

    @ModifyReturnValue(method = "teleportRandomly()Z", at = @At("RETURN"))
    private boolean miapi$teleportBlockEffect(boolean original) {
        EndermanEntity entity = (EndermanEntity) (Object) this;
        if(entity.hasStatusEffect(RegistryInventory.teleportBlockEffect)){
            return false;
        }
        return original;
    }
}
