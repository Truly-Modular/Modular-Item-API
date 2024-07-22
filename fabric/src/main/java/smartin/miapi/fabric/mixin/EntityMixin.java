package smartin.miapi.fabric.mixin;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Entity.class)
public abstract class EntityMixin {

    /*
    @ModifyVariable(method = "updateVelocity(FLnet/minecraft/util/math/Vec3d;)V", at = @At("HEAD"))
    private float miapi$adjustSwimSpeed(float speed) {
        Entity entity = (Entity) (Object) this;
        if (entity instanceof LivingEntity living && living.isTouchingWater()) {
            return (float) (speed * living.getAttributeValue(AttributeRegistry.SWIM_SPEED));
        }
        return speed;
    }

     */
}
