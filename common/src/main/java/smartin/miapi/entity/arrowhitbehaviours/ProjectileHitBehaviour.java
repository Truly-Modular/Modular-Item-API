package smartin.miapi.entity.arrowhitbehaviours;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import smartin.miapi.entity.ItemProjectileEntity;

/**
 * This Behaviour is for determining what happens to the Projectile After it nbtEvent an Entity.
 * The Damage is already dealt at this point.
 */
public interface ProjectileHitBehaviour {
    void onHit(ItemProjectileEntity projectile, Entity hit, EntityHitResult hitResult);

    default void onBlockHit(ItemProjectileEntity projectile, BlockHitResult hitResult){

    }
}
