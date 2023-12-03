package smartin.miapi.fabric.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import smartin.miapi.attributes.AttributeRegistry;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @ModifyVariable(method = "updateVelocity(FLnet/minecraft/util/math/Vec3d;)V", at = @At("HEAD"))
    private float miapi$adjustSwimSpeed(float speed) {
        Entity entity = (Entity) (Object) this;
        if (entity instanceof LivingEntity living && living.isTouchingWater()) {
            return (float) (speed * living.getAttributeValue(AttributeRegistry.SWIM_SPEED));
        }
        return speed;
    }
}
