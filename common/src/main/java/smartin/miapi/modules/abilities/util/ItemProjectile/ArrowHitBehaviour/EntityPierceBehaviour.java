package smartin.miapi.modules.abilities.util.ItemProjectile.ArrowHitBehaviour;

import net.minecraft.entity.Entity;
import net.minecraft.util.hit.EntityHitResult;
import smartin.miapi.modules.abilities.util.ItemProjectile.ItemProjectile;

public class EntityPierceBehaviour implements ProjectileHitBehaviour {
    @Override
    public void onHit(ItemProjectile projectile, Entity hit, EntityHitResult hitResult) {
        projectile.setVelocity(projectile.getVelocity().multiply(-0.1));
    }
}
