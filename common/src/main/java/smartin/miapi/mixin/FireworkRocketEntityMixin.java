package smartin.miapi.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

import static smartin.miapi.attributes.AttributeRegistry.ELYTRA_ROCKET_EFFICIENCY;

@Mixin(FireworkRocketEntity.class)
public abstract class FireworkRocketEntityMixin {

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
}
