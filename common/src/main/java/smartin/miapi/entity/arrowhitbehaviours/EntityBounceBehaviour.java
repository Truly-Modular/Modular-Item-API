package smartin.miapi.entity.arrowhitbehaviours;

import net.minecraft.entity.Entity;
import net.minecraft.util.hit.EntityHitResult;
import smartin.miapi.entity.ItemProjectileEntity;

public class EntityBounceBehaviour implements ProjectileHitBehaviour {
    @Override
    public void onHit(ItemProjectileEntity projectile, Entity hit, EntityHitResult hitResult) {
        projectile.setVelocity(projectile.getVelocity().multiply(-0.01, -0.1, -0.01));
    }
}
