package smartin.miapi.modules.abilities.util;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class AttackUtil {
    public static void performAttack(Player player, LivingEntity target, float damage, boolean useEnchants, ItemStack causing) {
        DamageSource source = player.damageSources().playerAttack(player);
        if (player.level() instanceof ServerLevel level && target.hurt(source, damage)) {
            if (useEnchants) {
                EnchantmentHelper.doPostAttackEffectsWithItemSource(level, target, source, causing);
            }
        }
    }

    public static void performSweeping(Player player, LivingEntity target, float sweepingRange, float sweepingDamage) {
        Level world = player.level();
        List<LivingEntity> entities = world.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(1.0, 0.25, 1.0).inflate(sweepingRange * 2, 1, sweepingRange * 2));

        for (LivingEntity entity : entities) {
            if (entity != player && entity != target && !player.isAlliedTo(entity) && !(entity instanceof ArmorStand armorStandEntity && armorStandEntity.isMarker()) && player.distanceToSqr(entity) < sweepingRange * sweepingRange) {
                entity.knockback(0.4, Mth.sin(player.getYRot() * 0.017453292F), -Mth.cos(player.getYRot() * 0.017453292F));
                entity.hurt(player.damageSources().playerAttack(player), sweepingDamage);
            }
        }

        world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, player.getSoundSource(), 1.0F, 1.0F);
        player.sweepAttack();
    }

    public static EntityHitResult raycastFromPlayer(double maxDistance, Player player) {
        Vec3 start = player.getEyePosition(0);
        Vec3 vec3d2 = player.getViewVector(0);
        Vec3 end = start.add(vec3d2.x * maxDistance, vec3d2.y * maxDistance, vec3d2.z * maxDistance);
        AABB box = player.getBoundingBox().expandTowards(vec3d2.scale(maxDistance)).inflate(1.0, 1.0, 1.0);
        return ProjectileUtil.getEntityHitResult(player, start, end, box, (entityx) -> {
            return !entityx.isSpectator() && entityx.isPickable();
        }, maxDistance * maxDistance);
    }
}
