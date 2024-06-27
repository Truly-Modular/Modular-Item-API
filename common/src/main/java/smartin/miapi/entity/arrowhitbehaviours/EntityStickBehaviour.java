package smartin.miapi.entity.arrowhitbehaviours;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;
import smartin.miapi.entity.ItemProjectileEntity;

public class EntityStickBehaviour implements ProjectileHitBehaviour {
    @Override
    public void onHit(ItemProjectileEntity projectile, Entity hit, EntityHitResult hitResult) {
        projectile.discard();
    }
}
