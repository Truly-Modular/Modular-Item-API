package smartin.miapi.item.modular.items.ItemProjectile.ArrowHitBehaviour;

import net.minecraft.entity.Entity;
import net.minecraft.util.hit.EntityHitResult;
import smartin.miapi.item.modular.items.ItemProjectile.ItemProjectile;

/**
 * This Behaviour is for determining what happens to the Projectile After it nbtEvent an Entity.
 * The Damage is already dealt at this point.
 */
public interface ProjectileHitBehaviour {
    void onHit(ItemProjectile projectile, Entity hit, EntityHitResult hitResult);
}
