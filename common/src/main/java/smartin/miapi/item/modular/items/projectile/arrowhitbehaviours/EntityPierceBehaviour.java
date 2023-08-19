package smartin.miapi.item.modular.items.projectile.arrowhitbehaviours;

import net.minecraft.entity.Entity;
import net.minecraft.util.hit.EntityHitResult;
import smartin.miapi.item.modular.items.projectile.ItemProjectileEntity;

public class EntityPierceBehaviour implements ProjectileHitBehaviour {
    @Override
    public void onHit(ItemProjectileEntity projectile, Entity hit, EntityHitResult hitResult) {
        projectile.setVelocity(projectile.getVelocity().multiply(0.99f));
        projectile.setDamageToDeal(true);
        projectile.setNoClip(false);
    }
}
