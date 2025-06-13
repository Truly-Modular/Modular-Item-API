package smartin.miapi.mixin.projectile;

import net.minecraft.network.syncher.SyncedDataHolder;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import smartin.miapi.entity.ProjectileWithBow;

@Mixin(SynchedEntityData.Builder.class)
public class SynchedEntityDataMixin {

    @Inject(method = "Lnet/minecraft/network/syncher/SynchedEntityData$Builder;<init>(Lnet/minecraft/network/syncher/SyncedDataHolder;)V", at = @At("TAIL"))
    private void miapi$injectBowItem(SyncedDataHolder entity, CallbackInfo ci) {
        if (entity instanceof Projectile projectile) {
            ((SynchedEntityData.Builder) (Object) this).define(ProjectileWithBow.get(), ItemStack.EMPTY);
        }
    }
}
