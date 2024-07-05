package smartin.miapi.modules.abilities;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.entity.BoomerangItemProjectileEntity;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.properties.attributes.AttributeProperty;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Stream;

public class BoomerangThrowingAbility extends ThrowingAbility {
    public static boolean isHolding = false;
    public static LinkedHashSet<Entity> entities = new LinkedHashSet<>();

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        user.startUsingItem(hand);
        start();
        return InteractionResultHolder.consume(user.getItemInHand(hand));
    }

    @Environment(EnvType.CLIENT)
    public static Stream<Entity> getLookingEntity(Player player, double range, float deltaTick, double angleFilter) {
        List<Entity> foundEntites = player.level().getEntities(player, AABB.unitCubeFromLowerCorner(player.position()).inflate(range));
        return foundEntites.stream()
                .filter(target -> isEntityLookedAtByPlayer(player, target, deltaTick, angleFilter))
                .sorted(Comparator.comparing(a -> (int) a.distanceTo(player)));
    }

    public static boolean isEntityLookedAtByPlayer(Player player, Entity target, float deltaTick, double angleFilter) {
        Vec3 vec3d = player.getViewVector(deltaTick).normalize();
        Vec3 vec3d2 = new Vec3(target.getX() - player.getX(), target.getEyeY() - player.getEyeY(), target.getZ() - player.getZ());
        double d = vec3d2.length();
        vec3d2 = vec3d2.normalize();
        double e = vec3d.dot(vec3d2);
        return e > 1.0 - angleFilter / d && player.hasLineOfSight(target);
    }

    @Override
    public void onStoppedUsingAfter(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks) {
        if (user instanceof Player playerEntity) {
            int i = this.getMaxUseTime(stack) - remainingUseTicks;
            if (i >= 10) {
                playerEntity.awardStat(Stats.ITEM_USED.get(stack.getItem()));
                if (!world.isClientSide) {
                    stack.hurtAndBreak(1, playerEntity, (p) -> {
                        p.sendToolBreakStatus(user.getActiveHand());
                    });

                    BoomerangItemProjectileEntity boomerangEntity = new BoomerangItemProjectileEntity(world, playerEntity, stack);
                    boomerangEntity.setTargets(entities);
                    float divergence = (float) AttributeProperty.getActualValue(stack, EquipmentSlot.MAINHAND, AttributeRegistry.PROJECTILE_ACCURACY);
                    float speed = (float) AttributeProperty.getActualValue(stack, EquipmentSlot.MAINHAND, AttributeRegistry.PROJECTILE_SPEED);
                    float damage = (float) AttributeProperty.getActualValue(stack, EquipmentSlot.MAINHAND, AttributeRegistry.PROJECTILE_DAMAGE);
                    damage = damage / speed;
                    if (stack.getItem() instanceof ModularItem) {
                        speed = 0.5f;
                    }
                    boomerangEntity.shootFromRotation(playerEntity, playerEntity.getXRot(), playerEntity.getYRot(), 0.0F, speed, divergence);
                    boomerangEntity.setBaseDamage(damage);
                    boomerangEntity.setBowItem(ItemStack.EMPTY);
                    boomerangEntity.setPierceLevel((byte) (int) AttributeProperty.getActualValue(stack, EquipmentSlot.MAINHAND, AttributeRegistry.PROJECTILE_PIERCING));
                    boomerangEntity.setSpeedDamage(true);
                    boomerangEntity.setPreferredSlot(playerEntity.getInventory().selected);
                    boomerangEntity.thrownStack = stack;
                    world.addFreshEntity(boomerangEntity);
                    world.playSound(null, user, SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0f, 1.0f);
                    if (playerEntity.getAbilities().instabuild) {
                        boomerangEntity.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                    } else {
                        user.setItemInHand(user.getUsedItemHand(), ItemStack.EMPTY);
                    }
                }
            }
        }
    }

    public ItemStack finishUsing(ItemStack stack, Level world, LivingEntity user) {
        return super.finishUsing(stack, world, user);
    }

    public void onStoppedUsing(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks) {
        super.onStoppedUsing(stack, world, user, remainingUseTicks);
    }

    public void onStoppedHolding(ItemStack stack, Level world, LivingEntity user) {
        super.onStoppedHolding(stack, world, user);
    }

    public void start() {
        isHolding = true;
    }

    public void stop() {
        isHolding = false;
    }
}
