package smartin.miapi.mixin;

import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(FireworkRocketEntity.class)
public abstract class FireworkRocketEntityMixin {

    /**
    @Shadow
    @Nullable
    private LivingEntity shooter;

    @ModifyVariable(method = "tick", at = @At("STORE"), ordinal = 1)
    private Vec3d miapi$crabVec3D(Vec3d vec) {
        if (this.shooter == null) return vec;
        var speedModifier = this.shooter.getAttributeValue(ELYTRA_ROCKET_EFFICIENCY);

        if (speedModifier == 0) return vec.multiply(0);
        return vec.multiply(1 / speedModifier);
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Vec3d;add(DDD)Lnet/minecraft/util/math/Vec3d;", ordinal = 0))
    private Vec3d miapi$increaseRocketSpeed(Vec3d velocity, double x, double y, double z) {
        if (this.shooter == null) return velocity;
        var speedModifier = this.shooter.getAttributeValue(ELYTRA_ROCKET_EFFICIENCY);

        return velocity.multiply(speedModifier).add(x, y, z);
    }
    */
}
