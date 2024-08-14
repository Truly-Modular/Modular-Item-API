package smartin.miapi.attributes;

import com.redpxnda.nucleus.facet.FacetRegistry;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.PlayerEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import smartin.miapi.Miapi;
import smartin.miapi.entity.ItemProjectileEntity;
import smartin.miapi.entity.ShieldingArmorFacet;
import smartin.miapi.entity.StunHealthFacet;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.events.MiapiProjectileEvents;
import smartin.miapi.mixin.LivingEntityAccessor;
import smartin.miapi.modules.properties.attributes.AttributeProperty;
import smartin.miapi.modules.properties.attributes.AttributeUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;


public class AttributeRegistry {
    public static Map<String, Attribute> entityAttributeMap = new HashMap<>();
    /**
     * Idk, this is kinda bad but i couldnt do it in the mixin
     */
    public static Map<Player, Boolean> hasCrittedLast = new WeakHashMap<>();

    public static Holder<Attribute> REACH;
    public static Holder<Attribute> ATTACK_RANGE;

    public static Holder<Attribute> SWIM_SPEED;

    public static Holder<Attribute> MINING_SPEED_PICKAXE;
    public static Holder<Attribute> MINING_SPEED_AXE;
    public static Holder<Attribute> MINING_SPEED_SHOVEL;
    public static Holder<Attribute> MINING_SPEED_HOE;

    public static Holder<Attribute> MAGIC_DAMAGE;
    public static Holder<Attribute> STUN_DAMAGE;

    public static Holder<Attribute> STUN_MAX_HEALTH;

    public static Holder<Attribute> CRITICAL_DAMAGE;
    public static Holder<Attribute> CRITICAL_CHANCE;
    public static Holder<Attribute> DAMAGE_RESISTANCE;
    public static Holder<Attribute> BACK_STAB;
    public static Holder<Attribute> ARMOR_CRUSHING;
    public static Holder<Attribute> SHIELD_BREAK;

    public static Holder<Attribute> PROJECTILE_ARMOR;

    public static Holder<Attribute> BOW_DRAW_TIME;

    public static Holder<Attribute> PLAYER_ITEM_USE_MOVEMENT_SPEED;

    public static Holder<Attribute> PROJECTILE_DAMAGE;
    @Deprecated
    /**
     * @deprecated use {@link AttributeRegistry#CRITICAL_DAMAGE} instead, its more general and has better logic
     */
    public static Holder<Attribute> PROJECTILE_CRIT_MULTIPLIER;
    public static Holder<Attribute> PROJECTILE_SPEED;
    public static Holder<Attribute> PROJECTILE_ACCURACY;
    public static Holder<Attribute> PROJECTILE_PIERCING;

    public static Holder<Attribute> ELYTRA_TURN_EFFICIENCY;
    public static Holder<Attribute> ELYTRA_GLIDE_EFFICIENCY;
    public static Holder<Attribute> ELYTRA_ROCKET_EFFICIENCY;

    public static Holder<Attribute> SHIELDING_ARMOR;

    /**
     * Changing these can break savegames, so do not touch
     */
    private static final ResourceLocation TEMP_CRIT_DMG_UUID = Miapi.id("temp_crit_dmg");
    private static final ResourceLocation TEMP_CRIT_DMG_MULTIPLIER_UUID = Miapi.id("temp_crit_dmg_multiplier");
    private static final ResourceLocation TEMP_BACKSTAB_DMG_UUID = Miapi.id("temp_backstab_dmg");


    public static void setup() {
        FacetRegistry.ENTITY_FACET_ATTACHMENT.register((entity, attacher) -> {
            if (entity instanceof LivingEntity livingEntity) {
                ShieldingArmorFacet facet = new ShieldingArmorFacet(livingEntity);
                attacher.add(ShieldingArmorFacet.KEY, facet);
                StunHealthFacet stunHealthFacet = new StunHealthFacet(livingEntity);
                attacher.add(StunHealthFacet.KEY, stunHealthFacet);
            }
        });

        MiapiEvents.LIVING_HURT_AFTER_ARMOR.register((livingHurt) -> {
            StunHealthFacet facet = StunHealthFacet.KEY.get(livingHurt.livingEntity);
            if (
                    livingHurt.damageSource != null &&
                    livingHurt.damageSource.getEntity() != null &&
                    livingHurt.damageSource.getEntity() instanceof LivingEntity attacker) {
                if (facet != null && !livingHurt.livingEntity.level().isClientSide()) {
                    if (attacker.getAttributes().hasAttribute(STUN_DAMAGE)) {
                        double currentStunDamage = attacker.getAttributeValue(STUN_DAMAGE);
                        if (currentStunDamage > 0.1) {
                            facet.takeStunDamage((float) currentStunDamage, attacker);
                        }
                    }
                }
            }
            return EventResult.pass();
        });

        MiapiEvents.LIVING_HURT_AFTER_ARMOR.register((livingHurt) -> {
            ShieldingArmorFacet facet = ShieldingArmorFacet.KEY.get(livingHurt.livingEntity);
            if (facet != null && !livingHurt.livingEntity.level().isClientSide()) {
                if (
                        livingHurt.damageSource != null &&
                        !livingHurt.damageSource.is(DamageTypeTags.BYPASSES_ARMOR)
                ) {
                    livingHurt.amount = facet.takeDamage(livingHurt.amount);
                    if (livingHurt.livingEntity instanceof ServerPlayer player) {
                        facet.sendToClient(player);
                    }
                }
            }
            return EventResult.pass();
        });

        MiapiEvents.LIVING_ENTITY_TICK_END.register((entity) -> {
            ShieldingArmorFacet facet = ShieldingArmorFacet.KEY.get(entity);
            if (facet != null && !entity.level().isClientSide()) {
                facet.tick();
            }
            return EventResult.pass();
        });

        PlayerEvent.PLAYER_JOIN.register((player -> {
            ShieldingArmorFacet.KEY.get(player);
        }));

        MiapiEvents.LIVING_HURT.register((livingHurtEvent -> {
            if (livingHurtEvent.livingEntity.getAttributes().hasAttribute(DAMAGE_RESISTANCE)) {
                livingHurtEvent.amount = (float) (livingHurtEvent.amount * (100 - livingHurtEvent.livingEntity.getAttributeValue(DAMAGE_RESISTANCE)) / 100);
            }
            return EventResult.pass();
        }));
        MiapiEvents.LIVING_ATTACK.register(((attacker, defender) -> {
            if (attacker != null && defender != null && attacker.getAttributes().hasAttribute(MAGIC_DAMAGE)) {
                double value = attacker.getAttributeValue(MAGIC_DAMAGE);
                if (value > 0) {
                    defender.hurt(attacker.damageSources().magic(), (float) value);
                }
            }
            return EventResult.pass();
        }));
        MiapiEvents.LIVING_HURT.register((livingHurtEvent -> {
            if (
                    livingHurtEvent.damageSource != null &&
                    livingHurtEvent.damageSource.getEntity() instanceof LivingEntity attacker) {
                if (attacker.getAttributes().hasAttribute(BACK_STAB) && attacker.getAttributes().getInstance(BACK_STAB) != null) {
                    if (livingHurtEvent.damageSource.getEntity().getLookAngle().dot(livingHurtEvent.livingEntity.getLookAngle()) > 0) {
                        attacker.getAttributes().getInstance(BACK_STAB).addTransientModifier(new AttributeModifier(TEMP_BACKSTAB_DMG_UUID, livingHurtEvent.amount, AttributeModifier.Operation.ADD_VALUE));
                        livingHurtEvent.amount = (float) attacker.getAttributeValue(BACK_STAB);
                        attacker.getAttributes().getInstance(BACK_STAB).removeModifier(TEMP_BACKSTAB_DMG_UUID);
                    }
                }
            }
            return EventResult.pass();
        }));
        MiapiEvents.LIVING_HURT.register((livingHurtEvent -> {
            if (
                    livingHurtEvent.damageSource != null &&
                    livingHurtEvent.damageSource.is(DamageTypeTags.IS_PROJECTILE) &&
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
            if (
                    livingHurtEvent.damageSource != null &&
                    livingHurtEvent.damageSource.getEntity() instanceof LivingEntity attacker) {
                if (attacker.getAttributes().hasAttribute(SHIELD_BREAK)) {
                    double value = attacker.getAttributeValue(SHIELD_BREAK);
                    if (livingHurtEvent.livingEntity instanceof Player player) {
                        if (value > 0 && player.isBlocking()) {
                            player.getCooldowns().addCooldown(Items.SHIELD, (int) (value * 20));
                            player.stopUsingItem();
                            player.level().broadcastEntityEvent(player, (byte) 30);
                        }
                    }
                }
            }
            return EventResult.pass();
        }));
        MiapiEvents.LIVING_HURT.register((livingHurtEvent -> {
            if (
                    livingHurtEvent.damageSource != null &&
                    livingHurtEvent.damageSource.getEntity() instanceof LivingEntity attacker) {
                if (attacker.getAttributes().hasAttribute(ARMOR_CRUSHING)) {
                    double value = attacker.getAttributeValue(ARMOR_CRUSHING);
                    ((LivingEntityAccessor) livingHurtEvent.livingEntity).callDamageArmor(livingHurtEvent.damageSource, (float) (livingHurtEvent.livingEntity.getArmorValue() * value));
                }
            }
            return EventResult.pass();
        }));

        MiapiProjectileEvents.MODULAR_PROJECTILE_ENTITY_HIT.register(listener -> {
            ItemProjectileEntity projectile = listener.projectile;
            if (projectile.isCritArrow()) {
                //TODO:rework projectile logic as a whole
                //listener.damage = (float) (listener.damage * AttributeProperty.getActualValue(projectile.getPickupItem(), EquipmentSlot.MAINHAND, AttributeRegistry.PROJECTILE_CRIT_MULTIPLIER));
            }
            return EventResult.pass();
        });

        MiapiEvents.LIVING_HURT.register(livingHurtEvent -> {
            if (
                    livingHurtEvent.damageSource != null &&
                    livingHurtEvent.damageSource.getEntity() instanceof LivingEntity attacker &&
                    !livingHurtEvent.livingEntity.level().isClientSide()
            ) {
                if (attacker.getAttributes().hasAttribute(CRITICAL_CHANCE) && !livingHurtEvent.isCritical) {
                    attacker.getAttributes().getInstance(CRITICAL_CHANCE).addTransientModifier(new AttributeModifier(TEMP_CRIT_DMG_UUID, 1, AttributeModifier.Operation.ADD_VALUE));
                    double value = attacker.getAttributeValue(CRITICAL_CHANCE) - 1;
                    attacker.getAttributes().getInstance(CRITICAL_CHANCE).removeModifier(TEMP_CRIT_DMG_UUID);
                    if (attacker.level().getRandom().nextDouble() < value) {
                        livingHurtEvent.isCritical = true;
                        livingHurtEvent.amount = livingHurtEvent.amount * 1.5f;
                        attacker.level().playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, attacker.getSoundSource(), 1.0F, 1.0F);
                        if (attacker instanceof Player player) {
                            player.crit(livingHurtEvent.livingEntity);
                        }
                        if (attacker.level().isClientSide()) {
                            Minecraft.getInstance().particleEngine.createTrackingEmitter(livingHurtEvent.livingEntity, ParticleTypes.CRIT);
                        } else {
                            if (attacker.level() instanceof ServerLevel serverWorld) {
                                serverWorld.getChunkSource().broadcastAndSend(attacker, new ClientboundAnimatePacket(livingHurtEvent.livingEntity, 4));
                            }
                        }
                    }
                }
                if (
                        attacker.getAttributes().hasAttribute(CRITICAL_DAMAGE) &&
                        livingHurtEvent.isCritical &&
                        attacker.getAttributes().getInstance(CRITICAL_DAMAGE) != null) {
                    attacker.getAttribute(CRITICAL_DAMAGE);
                    attacker.getAttributes().getInstance(CRITICAL_DAMAGE).addTransientModifier(new AttributeModifier(TEMP_CRIT_DMG_UUID, livingHurtEvent.amount / 1.5, AttributeModifier.Operation.ADD_VALUE));
                    attacker.getAttributes().getInstance(CRITICAL_DAMAGE).addTransientModifier(new AttributeModifier(TEMP_CRIT_DMG_MULTIPLIER_UUID, 0.5, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                    livingHurtEvent.amount = (float) attacker.getAttributeValue(CRITICAL_DAMAGE);
                    attacker.getAttributes().getInstance(CRITICAL_DAMAGE).removeModifier(TEMP_CRIT_DMG_UUID);
                    attacker.getAttributes().getInstance(CRITICAL_DAMAGE).removeModifier(TEMP_CRIT_DMG_MULTIPLIER_UUID);
                }
            }
            return EventResult.pass();
        }, -1);
    }

    public static double getAttribute(ItemStack stack, Attribute attribute, EquipmentSlot slot, double defaultValue) {
        return AttributeUtil.getActualValue(stack, slot, attribute, defaultValue);
    }
}
