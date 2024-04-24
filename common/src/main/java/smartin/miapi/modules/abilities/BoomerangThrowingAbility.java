package smartin.miapi.modules.abilities;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.entity.BoomerangItemProjectileEntity;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.properties.AttributeProperty;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

public class BoomerangThrowingAbility extends ThrowingAbility {
    public static boolean isHolding = false;
    public static LinkedHashSet<Entity> entities = new LinkedHashSet<>();

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        user.setCurrentHand(hand);
        start();
        return TypedActionResult.consume(user.getStackInHand(hand));
    }

    @Environment(EnvType.CLIENT)
    public static Optional<Entity> getLookingEntity(double range,float deltaTick) {
        PlayerEntity player = MinecraftClient.getInstance().player;
        List<Entity> foundEntites = player.getWorld().getOtherEntities(player, Box.from(player.getPos()).expand(range));
        return foundEntites.stream().filter(target -> {
            Vec3d vec3d = player.getRotationVec(deltaTick).normalize();
            Vec3d vec3d2 = new Vec3d(target.getX() - player.getX(), target.getEyeY() - player.getEyeY(), target.getZ() - player.getZ());
            double d = vec3d2.length();
            vec3d2 = vec3d2.normalize();
            double e = vec3d.dotProduct(vec3d2);
            return e > 1.0 - 0.015 / d && player.canSee(target);
        }).min(Comparator.comparing(a -> (int) a.distanceTo(player)));
    }

    @Override
    public void onStoppedUsingAfter(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (user instanceof PlayerEntity playerEntity) {
            int i = this.getMaxUseTime(stack) - remainingUseTicks;
            if (i >= 10) {
                playerEntity.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
                if (!world.isClient) {
                    stack.damage(1, playerEntity, (p) -> {
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
                    boomerangEntity.setVelocity(playerEntity, playerEntity.getPitch(), playerEntity.getYaw(), 0.0F, speed, divergence);
                    boomerangEntity.setDamage(damage);
                    boomerangEntity.setBowItem(ItemStack.EMPTY);
                    boomerangEntity.setPierceLevel((byte) (int) AttributeProperty.getActualValue(stack, EquipmentSlot.MAINHAND, AttributeRegistry.PROJECTILE_PIERCING));
                    boomerangEntity.setSpeedDamage(true);
                    boomerangEntity.setPreferredSlot(playerEntity.getInventory().selectedSlot);
                    boomerangEntity.thrownStack = stack;
                    world.spawnEntity(boomerangEntity);
                    world.playSoundFromEntity(null, user, SoundEvents.ITEM_TRIDENT_THROW, SoundCategory.PLAYERS, 1.0f, 1.0f);
                    if (playerEntity.getAbilities().creativeMode) {
                        boomerangEntity.pickupType = PersistentProjectileEntity.PickupPermission.CREATIVE_ONLY;
                    } else {
                        user.setStackInHand(user.getActiveHand(), ItemStack.EMPTY);
                    }
                }
            }
        }
    }

    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        return super.finishUsing(stack, world, user);
    }

    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        super.onStoppedUsing(stack, world, user, remainingUseTicks);
    }

    public void onStoppedHolding(ItemStack stack, World world, LivingEntity user) {
        super.onStoppedHolding(stack, world, user);
    }

    public void start() {
        isHolding = true;
    }

    public void stop() {
        isHolding = false;
    }
}
