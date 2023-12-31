package smartin.miapi.attributes;

import dev.architectury.event.EventResult;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import smartin.miapi.entity.ItemProjectileEntity;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.events.MiapiProjectileEvents;
import smartin.miapi.mixin.LivingEntityAccessor;
import smartin.miapi.modules.abilities.util.WrappedSoundEvent;
import smartin.miapi.modules.properties.AttributeProperty;

import java.util.HashMap;
import java.util.Map;


public class AttributeRegistry {
    public static Map<String, EntityAttribute> entityAttributeMap = new HashMap<>();

    public static EntityAttribute REACH;
    public static EntityAttribute ATTACK_RANGE;

    public static EntityAttribute SWIM_SPEED;

    public static EntityAttribute MINING_SPEED_PICKAXE;
    public static EntityAttribute MINING_SPEED_AXE;
    public static EntityAttribute MINING_SPEED_SHOVEL;
    public static EntityAttribute MINING_SPEED_HOE;

    public static EntityAttribute DAMAGE_RESISTANCE;
    public static EntityAttribute BACK_STAB;
    public static EntityAttribute ARMOR_CRUSHING;
    public static EntityAttribute SHIELD_BREAK;

    public static EntityAttribute PROJECTILE_ARMOR;

    public static EntityAttribute BOW_DRAW_TIME;

    public static EntityAttribute PLAYER_ITEM_USE_MOVEMENT_SPEED;

    public static EntityAttribute PROJECTILE_DAMAGE;
    public static EntityAttribute PROJECTILE_CRIT_MULTIPLIER;
    public static EntityAttribute PROJECTILE_SPEED;
    public static EntityAttribute PROJECTILE_ACCURACY;
    public static EntityAttribute PROJECTILE_PIERCING;

    public static EntityAttribute ELYTRA_TURN_EFFICIENCY;
    public static EntityAttribute ELYTRA_GLIDE_EFFICIENCY;
    public static EntityAttribute ELYTRA_ROCKET_EFFICIENCY;


    public static void setup() {
        MiapiEvents.LIVING_HURT.register((livingHurtEvent -> {
            if (livingHurtEvent.livingEntity.getAttributes().hasAttribute(DAMAGE_RESISTANCE)) {
                livingHurtEvent.amount = (float) (livingHurtEvent.amount * (100 - livingHurtEvent.livingEntity.getAttributeValue(DAMAGE_RESISTANCE)) / 100);
            }
            return EventResult.pass();
        }));
        MiapiEvents.LIVING_HURT.register((livingHurtEvent -> {
            if (livingHurtEvent.damageSource.getAttacker() instanceof LivingEntity attacker) {
                if (attacker.getAttributes().hasAttribute(BACK_STAB)) {
                    if (livingHurtEvent.damageSource.getAttacker().getRotationVector().dotProduct(livingHurtEvent.livingEntity.getRotationVector()) > 0) {
                        float backStab = (float) attacker.getAttributeValue(BACK_STAB);
                        livingHurtEvent.amount = livingHurtEvent.amount * (backStab);
                    }
                }
            }
            return EventResult.pass();
        }));
        MiapiEvents.LIVING_HURT.register((livingHurtEvent -> {
            if (livingHurtEvent.damageSource.isIn(DamageTypeTags.IS_PROJECTILE) &&
                    livingHurtEvent.livingEntity.getAttributes().hasAttribute(PROJECTILE_ARMOR)) {
                double projectileArmor = livingHurtEvent.livingEntity.getAttributeValue(PROJECTILE_ARMOR);
                if (projectileArmor > 0) {
                    double totalDamage = livingHurtEvent.amount * (1 - ((Math.max(20, projectileArmor)) / 25));
                    livingHurtEvent.amount = (float) totalDamage;
                }
            }
            return EventResult.pass();
        }));
        MiapiEvents.LIVING_HURT_AFTER.register((livingHurtEvent -> {
            if (livingHurtEvent.damageSource.getAttacker() instanceof LivingEntity attacker) {
                if (attacker.getAttributes().hasAttribute(SHIELD_BREAK)) {
                    double value = attacker.getAttributeValue(SHIELD_BREAK);
                    if (livingHurtEvent.livingEntity instanceof PlayerEntity player) {
                        if (value > 0 && player.isBlocking()) {
                            player.getItemCooldownManager().set(Items.SHIELD, (int) (value * 20));
                            player.clearActiveItem();
                            player.getWorld().sendEntityStatus(player, (byte) 30);
                        }
                    }
                }
            }
            return EventResult.pass();
        }));
        MiapiEvents.LIVING_HURT.register((livingHurtEvent -> {
            if (livingHurtEvent.damageSource.getAttacker() instanceof LivingEntity attacker) {
                if (attacker.getAttributes().hasAttribute(ARMOR_CRUSHING)) {
                    double value = attacker.getAttributeValue(ARMOR_CRUSHING);
                    ((LivingEntityAccessor) livingHurtEvent.livingEntity).callDamageArmor(livingHurtEvent.damageSource, (float) (livingHurtEvent.livingEntity.getArmor() * value));
                }
            }
            return EventResult.pass();
        }));
        MiapiProjectileEvents.MODULAR_PROJECTILE_ENTITY_POST_HIT.register(listener -> {
            ItemProjectileEntity projectile = listener.projectile;
            Entity victim = listener.entityHitResult.getEntity();
            Entity owner = listener.projectile.getOwner();
            if (projectile.getWorld() instanceof ServerWorld && projectile.getWorld().isThundering() && projectile.hasChanneling()) {
                BlockPos blockPos = victim.getBlockPos();
                if (projectile.getWorld().isSkyVisible(blockPos)) {
                    LightningEntity lightningEntity = EntityType.LIGHTNING_BOLT.create(projectile.getWorld());
                    lightningEntity.refreshPositionAfterTeleport(Vec3d.ofBottomCenter(blockPos));
                    lightningEntity.setChanneler(owner instanceof ServerPlayerEntity ? (ServerPlayerEntity) owner : null);
                    projectile.getWorld().spawnEntity(lightningEntity);
                    projectile.hitEntitySound = new WrappedSoundEvent(SoundEvents.ITEM_TRIDENT_THUNDER, 5.0f, 1.0f);
                }
            }
            return EventResult.pass();
        });

        MiapiProjectileEvents.MODULAR_PROJECTILE_ENTITY_HIT.register(listener -> {
            ItemProjectileEntity projectile = listener.projectile;
            if (projectile.isCritical()) {
                listener.damage = (float) (listener.damage * AttributeProperty.getActualValue(projectile.asItemStack(), EquipmentSlot.MAINHAND, AttributeRegistry.PROJECTILE_CRIT_MULTIPLIER));
            }
            return EventResult.pass();
        });
    }

    public static double getAttribute(ItemStack stack, EntityAttribute attribute, EquipmentSlot slot, double defaultValue) {
        return AttributeProperty.getActualValue(stack, slot, attribute, defaultValue);
    }
}
