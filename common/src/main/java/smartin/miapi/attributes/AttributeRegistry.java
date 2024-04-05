package smartin.miapi.attributes;

import com.redpxnda.nucleus.facet.FacetRegistry;
import dev.architectury.event.EventResult;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.EntityAnimationS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import smartin.miapi.entity.ItemProjectileEntity;
import smartin.miapi.entity.ShieldingArmorFacet;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.events.MiapiProjectileEvents;
import smartin.miapi.mixin.LivingEntityAccessor;
import smartin.miapi.modules.abilities.util.WrappedSoundEvent;
import smartin.miapi.modules.properties.AttributeProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;


public class AttributeRegistry {
    public static Map<String, EntityAttribute> entityAttributeMap = new HashMap<>();
    /**
     * Idk, this is kinda bad but i couldnt do it in the mixin
     */
    public static Map<PlayerEntity, Boolean> hasCrittedLast = new WeakHashMap<>();

    public static EntityAttribute REACH;
    public static EntityAttribute ATTACK_RANGE;

    public static EntityAttribute SWIM_SPEED;

    public static EntityAttribute MINING_SPEED_PICKAXE;
    public static EntityAttribute MINING_SPEED_AXE;
    public static EntityAttribute MINING_SPEED_SHOVEL;
    public static EntityAttribute MINING_SPEED_HOE;

    public static EntityAttribute MAGIC_DAMAGE;

    public static EntityAttribute CRITICAL_DAMAGE;
    public static EntityAttribute CRITICAL_CHANCE;
    public static EntityAttribute DAMAGE_RESISTANCE;
    public static EntityAttribute BACK_STAB;
    public static EntityAttribute ARMOR_CRUSHING;
    public static EntityAttribute SHIELD_BREAK;

    public static EntityAttribute PROJECTILE_ARMOR;

    public static EntityAttribute BOW_DRAW_TIME;

    public static EntityAttribute PLAYER_ITEM_USE_MOVEMENT_SPEED;

    public static EntityAttribute PROJECTILE_DAMAGE;
    @Deprecated
    /**
     * @deprecated use {@link AttributeRegistry#CRITICAL_DAMAGE} instead, its more general and has better logic
     */
    public static EntityAttribute PROJECTILE_CRIT_MULTIPLIER;
    public static EntityAttribute PROJECTILE_SPEED;
    public static EntityAttribute PROJECTILE_ACCURACY;
    public static EntityAttribute PROJECTILE_PIERCING;

    public static EntityAttribute ELYTRA_TURN_EFFICIENCY;
    public static EntityAttribute ELYTRA_GLIDE_EFFICIENCY;
    public static EntityAttribute ELYTRA_ROCKET_EFFICIENCY;

    public static EntityAttribute SHIELDING_ARMOR;

    /**
     * Changing these can break savegames, so do not touch
     */
    private static final UUID TEMP_CRIT_DMG_UUID = UUID.fromString("483b007a-c7db-11ee-a506-0242ac120002");
    private static final UUID TEMP_CRIT_DMG_MULTIPLIER_UUID = UUID.fromString("238664bf-ae30-40f7-b717-230655bd6595");
    private static final UUID TEMP_BACKSTAB_DMG_UUID = UUID.fromString("03740034-c97c-11ee-a506-0242ac120002");


    public static void setup() {
        FacetRegistry.ENTITY_FACET_ATTACHMENT.register((entity, attacher) -> {
            if (entity instanceof LivingEntity livingEntity){
                attacher.add(ShieldingArmorFacet.KEY, new ShieldingArmorFacet(livingEntity));
            }
        });

        MiapiEvents.LIVING_HURT_AFTER_ARMOR.register((livingHurt) -> {
            ShieldingArmorFacet facet = ShieldingArmorFacet.KEY.get(livingHurt.livingEntity);
            if (facet != null && !livingHurt.livingEntity.getWorld().isClient()) {
                if(
                        !livingHurt.damageSource.isIn(DamageTypeTags.BYPASSES_ARMOR)
                ){
                    livingHurt.amount = facet.takeDamage(livingHurt.amount);
                    if (livingHurt.livingEntity instanceof ServerPlayerEntity player) {
                        facet.sendToClient(player);
                    }
                }
            }
            return EventResult.pass();
        });

        MiapiEvents.LIVING_ENTITY_TICK_END.register((entity) -> {
            ShieldingArmorFacet facet = ShieldingArmorFacet.KEY.get(entity);
            if (facet != null && !entity.getWorld().isClient()) {
                facet.tick();
            }
            return EventResult.pass();
        });

        MiapiEvents.LIVING_HURT.register((livingHurtEvent -> {
            if (livingHurtEvent.livingEntity.getAttributes().hasAttribute(DAMAGE_RESISTANCE)) {
                livingHurtEvent.amount = (float) (livingHurtEvent.amount * (100 - livingHurtEvent.livingEntity.getAttributeValue(DAMAGE_RESISTANCE)) / 100);
            }
            return EventResult.pass();
        }));
        MiapiEvents.LIVING_ATTACK.register(((attacker, defender) -> {
            if (attacker.getAttributes().hasAttribute(MAGIC_DAMAGE)) {
                double value = attacker.getAttributeValue(MAGIC_DAMAGE);
                if (value > 0) {
                    defender.damage(attacker.getDamageSources().magic(), (float) value);
                }
            }
            return EventResult.pass();
        }));
        MiapiEvents.LIVING_HURT.register((livingHurtEvent -> {
            if (livingHurtEvent.damageSource.getAttacker() instanceof LivingEntity attacker) {
                if (attacker.getAttributes().hasAttribute(BACK_STAB)) {
                    if (livingHurtEvent.damageSource.getAttacker().getRotationVector().dotProduct(livingHurtEvent.livingEntity.getRotationVector()) > 0) {
                        attacker.getAttributes().getCustomInstance(BACK_STAB).addTemporaryModifier(new EntityAttributeModifier(TEMP_BACKSTAB_DMG_UUID, "temp_backstab_base_damage", livingHurtEvent.amount, EntityAttributeModifier.Operation.ADDITION));
                        livingHurtEvent.amount = (float) attacker.getAttributeValue(BACK_STAB);
                        attacker.getAttributes().getCustomInstance(BACK_STAB).removeModifier(TEMP_BACKSTAB_DMG_UUID);
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

        MiapiEvents.LIVING_HURT.register(livingHurtEvent -> {
            if (
                    livingHurtEvent.damageSource.getAttacker() instanceof LivingEntity attacker &&
                            !livingHurtEvent.livingEntity.getWorld().isClient()
            ) {
                if (attacker.getAttributes().hasAttribute(CRITICAL_CHANCE) && !livingHurtEvent.isCritical) {
                    attacker.getAttributes().getCustomInstance(CRITICAL_CHANCE).addTemporaryModifier(new EntityAttributeModifier(TEMP_CRIT_DMG_UUID, "temp_crit_base", 1, EntityAttributeModifier.Operation.ADDITION));
                    double value = attacker.getAttributeValue(CRITICAL_CHANCE) - 1;
                    attacker.getAttributes().getCustomInstance(CRITICAL_CHANCE).removeModifier(TEMP_CRIT_DMG_UUID);
                    if (attacker.getWorld().getRandom().nextDouble() < value) {
                        livingHurtEvent.isCritical = true;
                        livingHurtEvent.amount = livingHurtEvent.amount * 1.5f;
                        attacker.getWorld().playSound((PlayerEntity) null, attacker.getX(), attacker.getY(), attacker.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, attacker.getSoundCategory(), 1.0F, 1.0F);
                        if (attacker instanceof PlayerEntity player) {
                            player.addCritParticles(livingHurtEvent.livingEntity);
                        }
                        if (attacker.getWorld().isClient()) {
                            MinecraftClient.getInstance().particleManager.addEmitter(livingHurtEvent.livingEntity, ParticleTypes.CRIT);
                        } else {
                            if (attacker.getWorld() instanceof ServerWorld serverWorld) {
                                serverWorld.getChunkManager().sendToNearbyPlayers(attacker, new EntityAnimationS2CPacket(livingHurtEvent.livingEntity, 4));
                            }
                        }
                    }
                }
                if (
                        attacker.getAttributes().hasAttribute(CRITICAL_DAMAGE) &&
                                livingHurtEvent.isCritical &&
                                attacker.getAttributes().getCustomInstance(CRITICAL_DAMAGE) != null) {
                    attacker.getAttributeInstance(CRITICAL_DAMAGE);
                    attacker.getAttributes().getCustomInstance(CRITICAL_DAMAGE).addTemporaryModifier(new EntityAttributeModifier(TEMP_CRIT_DMG_UUID, "temp_crit_base_damage", livingHurtEvent.amount / 1.5, EntityAttributeModifier.Operation.ADDITION));
                    attacker.getAttributes().getCustomInstance(CRITICAL_DAMAGE).addTemporaryModifier(new EntityAttributeModifier(TEMP_CRIT_DMG_MULTIPLIER_UUID, "temp_crit_base_multiplier", 0.5, EntityAttributeModifier.Operation.MULTIPLY_BASE));
                    livingHurtEvent.amount = (float) attacker.getAttributeValue(CRITICAL_DAMAGE);
                    attacker.getAttributes().getCustomInstance(CRITICAL_DAMAGE).removeModifier(TEMP_CRIT_DMG_UUID);
                    attacker.getAttributes().getCustomInstance(CRITICAL_DAMAGE).removeModifier(TEMP_CRIT_DMG_MULTIPLIER_UUID);
                }
            }
            return EventResult.pass();
        }, -1);
    }

    public static double getAttribute(ItemStack stack, EntityAttribute attribute, EquipmentSlot slot, double defaultValue) {
        return AttributeProperty.getActualValue(stack, slot, attribute, defaultValue);
    }
}
