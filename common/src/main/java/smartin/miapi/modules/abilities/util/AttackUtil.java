package smartin.miapi.modules.abilities.util;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class AttackUtil {
    public static void performAttack(PlayerEntity player, LivingEntity target, float damage, boolean useEnchants) {
        int fireAspect = EnchantmentHelper.getFireAspect(player);
        int knockback = EnchantmentHelper.getKnockback(player);
        if (target.damage(player.getDamageSources().playerAttack(player), damage)) {
            if (fireAspect > 0 && !target.isOnFire() && useEnchants) {
                target.setOnFireFor(1);
            }
            if (knockback > 0 && useEnchants) {
                target.takeKnockback((knockback * 0.5F), MathHelper.sin(player.getYaw() * 0.017453292F), (-MathHelper.cos(player.getYaw() * 0.017453292F)));

                player.setVelocity(player.getVelocity().multiply(0.6, 1.0, 0.6));
                player.setSprinting(false);
            }
        }
    }

    public static void performSweeping(PlayerEntity player, LivingEntity target, float sweepingRange, float sweepingDamage) {
        World world = player.getWorld();
        List<LivingEntity> entities = world.getNonSpectatingEntities(LivingEntity.class, target.getBoundingBox().expand(1.0, 0.25, 1.0).expand(sweepingRange * 2,1,sweepingRange * 2));

        for (LivingEntity entity : entities) {
            if (entity != player && entity != target && !player.isTeammate(entity) && !(entity instanceof ArmorStandEntity && ((ArmorStandEntity) entity).isMarker()) && player.squaredDistanceTo(entity) < sweepingRange * sweepingRange) {
                entity.takeKnockback(0.4, MathHelper.sin(player.getYaw() * 0.017453292F), -MathHelper.cos(player.getYaw() * 0.017453292F));
                entity.damage(player.getDamageSources().playerAttack(player), sweepingDamage);
            }
        }

        world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, player.getSoundCategory(), 1.0F, 1.0F);
        player.spawnSweepAttackParticles();
    }

    public static EntityHitResult raycastFromPlayer(double maxDistance, PlayerEntity player) {
        Vec3d start = player.getCameraPosVec(0);
        Vec3d vec3d2 = player.getRotationVec(0);
        Vec3d end = start.add(vec3d2.x * maxDistance, vec3d2.y * maxDistance, vec3d2.z * maxDistance);
        Box box = player.getBoundingBox().stretch(vec3d2.multiply(maxDistance)).expand(1.0, 1.0, 1.0);
        return ProjectileUtil.raycast(player, start, end, box, (entityx) -> {
            return !entityx.isSpectator() && entityx.canHit();
        }, maxDistance * maxDistance);
    }
}
