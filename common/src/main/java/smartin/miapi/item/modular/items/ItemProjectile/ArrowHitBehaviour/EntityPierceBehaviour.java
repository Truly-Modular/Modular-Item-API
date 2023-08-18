package smartin.miapi.item.modular.items.ItemProjectile.ArrowHitBehaviour;

import net.minecraft.entity.Entity;
import net.minecraft.util.hit.EntityHitResult;
import smartin.miapi.item.modular.items.ItemProjectile.ItemProjectile;

public class EntityPierceBehaviour implements ProjectileHitBehaviour {
    @Override
    public void onHit(ItemProjectile projectile, Entity hit, EntityHitResult hitResult) {
        projectile.setVelocity(projectile.getVelocity().multiply(0.99f));
        projectile.setDamageToDeal(true);
        projectile.setNoClip(false);
    }
}
