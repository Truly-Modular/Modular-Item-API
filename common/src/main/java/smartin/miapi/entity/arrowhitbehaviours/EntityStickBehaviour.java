package smartin.miapi.entity.arrowhitbehaviours;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.util.hit.EntityHitResult;
import smartin.miapi.entity.ItemProjectileEntity;

public class EntityStickBehaviour implements ProjectileHitBehaviour {
    @Override
    public void onHit(ItemProjectileEntity projectile, Entity hit, EntityHitResult hitResult) {
        ArrowEntity arrowEntity;
        LivingEntity livingEntity;
        projectile.discard();
    }
}
