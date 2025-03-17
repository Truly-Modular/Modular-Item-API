package smartin.miapi.fabric.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import smartin.miapi.attributes.AttributeRegistry;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @ModifyVariable(method = "Lnet/minecraft/world/entity/Entity;moveRelative(FLnet/minecraft/world/phys/Vec3;)V", at = @At("HEAD"))
    private float miapi$adjustSwimSpeed(float speed) {
        Entity entity = (Entity) (Object) this;
        if (entity instanceof LivingEntity living && living.isInWater() && living.getAttributes().hasAttribute(AttributeRegistry.SWIM_SPEED)) {
            return (float) (speed * living.getAttributeValue(AttributeRegistry.SWIM_SPEED));
        }
        return speed;
    }
}
