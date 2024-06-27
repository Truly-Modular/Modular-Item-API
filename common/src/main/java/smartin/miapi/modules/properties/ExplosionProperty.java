package smartin.miapi.modules.properties;

import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import com.redpxnda.nucleus.codec.behavior.CodecBehavior;
import dev.architectury.event.EventResult;
import net.minecraft.core.BlockPos;
import net.minecraft.enchantment.ProtectionEnchantment;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EntityBasedExplosionDamageCalculator;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.entity.ItemProjectileEntity;
import smartin.miapi.events.MiapiProjectileEvents;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.util.CodecBasedProperty;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * This property is exploding projectiles on impact
 */
public class ExplosionProperty extends CodecBasedProperty<ExplosionProperty.ExplosionInfo> {
    public static final String KEY = "explosion_projectile";
    public static final Codec<ExplosionInfo> codec = AutoCodec.of(ExplosionInfo.class).codec();
    public static ExplosionProperty property;

    public ExplosionProperty() {
        super(KEY, codec);
        property = this;
        MiapiProjectileEvents.MODULAR_PROJECTILE_ENTITY_HIT.register(event -> {
            @Nullable Tuple<ModuleInstance, JsonElement> jsonElement = this.highestPriorityJsonElement(event.projectile.getPickupItem());
            if (jsonElement != null) {
                ExplosionInfo info = new ExplosionInfo(jsonElement.getB().getAsJsonObject(), jsonElement.getA());
                if (!event.projectile.level().isClientSide()) {
                    explode(info, event.projectile, event.entityHitResult);
                    event.projectile.discard();
                }
                return EventResult.interruptTrue();
            }
            return EventResult.pass();
        });
        MiapiProjectileEvents.MODULAR_PROJECTILE_BLOCK_HIT.register(event -> {
            ExplosionInfo info = this.get(event.projectile.getPickupItem());
            if (info != null) {
                if (!event.projectile.level().isClientSide()) {
                    explode(info, event.projectile, event.blockHitResult);
                    event.projectile.discard();
                }
                return EventResult.interruptTrue();
            }
            return EventResult.pass();
        });
    }

    /**
     * Explode things
     *
     * @param info       Info about the Explosion
     * @param projectile the projectile that is exploding
     * @param result     hitresult / the contact point of the explosion
     */
    public static void explode(ExplosionInfo info, ItemProjectileEntity projectile, HitResult result) {
        double x = result.getLocation().x();// - projectile.getVelocity().getX() / 60;
        double y = result.getLocation().y();// - projectile.getVelocity().getY() / 60;
        double z = result.getLocation().z();// - projectile.getVelocity().getZ() / 60;
        ExplosionDamageCalculator behavior = new ArrowExplosionInfo();
        Explosion.BlockInteraction destructionType;
        if (info.destroyBlocks) {
            destructionType = Explosion.BlockInteraction.DESTROY;
        } else {
            destructionType = Explosion.BlockInteraction.KEEP;
        }

        MiapiExplosion explosion = new MiapiExplosion(projectile.level(),
                projectile.getOwner() == null ? projectile : projectile.getOwner(),
                (DamageSource) null, behavior, x, y, z, (float) info.strength, false, destructionType);
        explosion.entityMaxDamage = (float) info.entityMaxDamage;
        explosion.entityRadius = (float) info.entityRadius;
        explosion.entityExplosionPower = (float) info.entityStrength;
        if (!projectile.level().isClientSide) {
            explosion.explode();
        }
        explosion.finalizeExplosion(true);
        if (!projectile.level().isClientSide()) {
            if (!explosion.interactsWithBlocks()) {
                explosion.clearToBlow();
            }

            Iterator var14 = projectile.level().players().iterator();

            while (var14.hasNext()) {
                ServerPlayer serverPlayerEntity = (ServerPlayer) var14.next();
                if (serverPlayerEntity.distanceToSqr(x, y, z) < 4096.0) {
                    serverPlayerEntity.connection.send(new ClientboundExplodePacket(x, y, z, (float) info.strength, explosion.getToBlow(), explosion.getHitPlayers().get(serverPlayerEntity)));
                }
            }
        }
    }

    public ExplosionInfo getInfo(ItemStack itemStack, ModuleProperty property) {
        ExplosionInfo info = null;
        for (ModuleInstance moduleInstance : ItemModule.getModules(itemStack).allSubModules()) {
            if (moduleInstance.getProperties().containsKey(property)) {
                info = new ExplosionInfo(moduleInstance.getProperties().get(property).getAsJsonObject(), moduleInstance);
            }
        }
        return info;
    }

    public static void explode(ExplosionInfo info, Level world, Vec3 vec3d, @Nullable Entity owner) {
        double x = vec3d.x();
        double y = vec3d.y();
        double z = vec3d.z();
        ExplosionDamageCalculator behavior = new ArrowExplosionInfo();
        Explosion.BlockInteraction destructionType;
        if (info.destroyBlocks) {
            destructionType = Explosion.BlockInteraction.DESTROY;
        } else {
            destructionType = Explosion.BlockInteraction.KEEP;
        }

        MiapiExplosion explosion = new MiapiExplosion(world,
                owner,
                (DamageSource) null, behavior, x, y, z, (float) info.strength, false, destructionType);
        explosion.entityMaxDamage = (float) info.entityMaxDamage;
        explosion.entityRadius = (float) info.entityRadius;
        explosion.entityExplosionPower = (float) info.entityStrength;
        if (!world.isClientSide) {
            explosion.explode();
        }
        explosion.finalizeExplosion(true);
        if (!world.isClientSide()) {
            if (!explosion.interactsWithBlocks()) {
                explosion.clearToBlow();
            }

            Iterator var14 = world.players().iterator();

            while (var14.hasNext()) {
                ServerPlayer serverPlayerEntity = (ServerPlayer) var14.next();
                if (serverPlayerEntity.distanceToSqr(x, y, z) < 4096.0) {
                    serverPlayerEntity.connection.send(new ClientboundExplodePacket(x, y, z, (float) info.strength, explosion.getToBlow(), explosion.getHitPlayers().get(serverPlayerEntity)));
                }
            }
        }
    }

    public static class MiapiExplosion extends Explosion {
        private static final ExplosionDamageCalculator DEFAULT_BEHAVIOR = new ExplosionDamageCalculator();
        public float blockExplosionPower;
        public float entityExplosionPower;
        public float entityRadius;
        public float entityMaxDamage;
        private Level world;
        private final double explosionX;
        private final double explosionY;
        private final double explosionZ;
        private final ExplosionDamageCalculator behavior;

        public MiapiExplosion(Level world, @Nullable Entity entity, @Nullable DamageSource damageSource, @Nullable ExplosionDamageCalculator behavior, double x, double y, double z, float power, boolean createFire, BlockInteraction destructionType) {
            super(world, entity, damageSource, behavior, x, y, z, power, createFire, destructionType);
            this.world = world;
            this.explosionX = x;
            this.explosionY = y;
            this.explosionZ = z;
            this.behavior = behavior == null ? this.makeDamageCalculator(entity) : behavior;
            //default vanilla values
            blockExplosionPower = power;
            entityExplosionPower = power * 7;
            entityRadius = power * 2;
            entityMaxDamage = Float.POSITIVE_INFINITY;
            // Loop through the list of entities
        }

        private ExplosionDamageCalculator makeDamageCalculator(@Nullable Entity entity) {
            return entity == null ? DEFAULT_BEHAVIOR : new EntityBasedExplosionDamageCalculator(entity);
        }

        @Override
        public void explode() {
            this.world.gameEvent(this.source, GameEvent.EXPLODE, new Vec3(this.explosionX, this.explosionY, this.explosionZ));
            Set<BlockPos> affectedBlockPositions = Sets.newHashSet();

            for (int x = 0; x < 16; ++x) {
                for (int y = 0; y < 16; ++y) {
                    for (int z = 0; z < 16; ++z) {
                        if (x == 0 || x == 15 || y == 0 || y == 15 || z == 0 || z == 15) {
                            double offsetX = x / 15.0 * 2.0 - 1.0;
                            double offsetY = y / 15.0 * 2.0 - 1.0;
                            double offsetZ = z / 15.0 * 2.0 - 1.0;
                            double distance = Math.sqrt(offsetX * offsetX + offsetY * offsetY + offsetZ * offsetZ);
                            offsetX /= distance;
                            offsetY /= distance;
                            offsetZ /= distance;
                            double currentX = this.explosionX;
                            double currentY = this.explosionY;
                            double currentZ = this.explosionZ;

                            for (
                                    float explosionPower = this.blockExplosionPower * (0.7F + this.world.random.nextFloat() * 0.6F);
                                    explosionPower > 0.0F;
                                    explosionPower -= 0.22500001F) {
                                BlockPos blockPos = BlockPos.containing(currentX, currentY, currentZ);
                                BlockState blockState = this.world.getBlockState(blockPos);
                                FluidState fluidState = this.world.getFluidState(blockPos);

                                if (!this.world.isInWorldBounds(blockPos)) {
                                    break;
                                }

                                Optional<Float> resistance = this.behavior.getBlockExplosionResistance(this, this.world, blockPos, blockState, fluidState);

                                if (resistance.isPresent()) {
                                    explosionPower -= (resistance.get() + 0.3F) * 0.3F;
                                }

                                if (explosionPower > 0.0F && this.behavior.shouldBlockExplode(this, this.world, blockPos, blockState, explosionPower)) {
                                    affectedBlockPositions.add(blockPos);
                                }

                                currentX += offsetX * 0.30000001192092896;
                                currentY += offsetY * 0.30000001192092896;
                                currentZ += offsetZ * 0.30000001192092896;
                            }
                        }
                    }
                }
            }

            this.getToBlow().addAll(affectedBlockPositions);
            int minX = Mth.floor(this.explosionX - entityRadius - 1.0);
            int maxX = Mth.floor(this.explosionX + entityRadius + 1.0);
            int minY = Mth.floor(this.explosionY - entityRadius - 1.0);
            int maxY = Mth.floor(this.explosionY + entityRadius + 1.0);
            int minZ = Mth.floor(this.explosionZ - entityRadius - 1.0);
            int maxZ = Mth.floor(this.explosionZ + entityRadius + 1.0);
            List<Entity> nearbyEntities = this.world.getEntities(this.source, new AABB(minX, minY, minZ, maxX, maxY, maxZ));
            nearbyEntities.add(source);
            Vec3 explosionCenter = new Vec3(this.explosionX, this.explosionY, this.explosionZ);

            for (int i = 0; i < nearbyEntities.size(); ++i) {
                Entity targetEntity = nearbyEntities.get(i);

                if (!targetEntity.ignoreExplosion()) {
                    double distanceToCenter = Math.sqrt(targetEntity.distanceToSqr(explosionCenter)) / entityRadius;

                    if (distanceToCenter <= 1.0) {
                        double deltaX = targetEntity.getX() - this.explosionX;
                        double deltaY = (targetEntity instanceof PrimedTnt ? targetEntity.getY() : targetEntity.getEyeY()) - this.explosionY;
                        double deltaZ = targetEntity.getZ() - this.explosionZ;
                        double entityDistance = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);

                        if (entityDistance != 0.0) {
                            deltaX /= entityDistance;
                            deltaY /= entityDistance;
                            deltaZ /= entityDistance;
                            double exposure = getSeenPercent(explosionCenter, targetEntity);
                            double damage = (1.0 - distanceToCenter) * exposure;
                            float totalDamage = Math.min(entityMaxDamage, ((int) ((damage * damage + damage) / 2.0 * entityExplosionPower + 1.0)));
                            targetEntity.hurt(this.getDamageSource(), totalDamage);

                            double knockback;

                            if (targetEntity instanceof LivingEntity livingEntity) {
                                knockback = ProtectionEnchantment.transformExplosionKnockback(livingEntity, damage);
                            } else {
                                knockback = damage;
                            }

                            deltaX *= knockback;
                            deltaY *= knockback;
                            deltaZ *= knockback;
                            Vec3 velocity = new Vec3(deltaX, deltaY, deltaZ);
                            targetEntity.setDeltaMovement(targetEntity.getDeltaMovement().add(velocity));

                            if (targetEntity instanceof Player playerEntity) {

                                if (!playerEntity.isSpectator() && (!playerEntity.isCreative() || !playerEntity.getAbilities().flying)) {
                                    this.getHitPlayers().put(playerEntity, velocity);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static class ArrowExplosionInfo extends ExplosionDamageCalculator {
        public Optional<Float> getBlockExplosionResistance(Explosion explosion, BlockGetter world, BlockPos pos, BlockState blockState, FluidState fluidState) {
            Optional<Float> distance = blockState.isAir() && fluidState.isEmpty() ? Optional.empty() : Optional.of(Math.max(blockState.getBlock().getExplosionResistance(), fluidState.getExplosionResistance()));
            if (distance.isPresent()) {
                //return Optional.of(0f);
            }
            return distance;
        }
    }

    public static class ExplosionInfo {
        @CodecBehavior.Optional
        public boolean destroyBlocks;
        @CodecBehavior.Optional
        public double chance;
        public double strength;
        @CodecBehavior.Optional
        public double entityStrength;
        @CodecBehavior.Optional
        public double entityMaxDamage;
        @CodecBehavior.Optional
        public double entityRadius;

        public ExplosionInfo(JsonObject element, ModuleInstance moduleInstance) {
            destroyBlocks = ModuleProperty.getBoolean(element, "destroyBlocks", false);
            chance = ModuleProperty.getDouble(element, "chance", moduleInstance, 1.0);
            strength = ModuleProperty.getDouble(element, "strength", moduleInstance, 1.0);
            entityStrength = ModuleProperty.getDouble(element, "entityStrength", moduleInstance, strength * 7);
            entityMaxDamage = ModuleProperty.getDouble(element, "entityMaxDamage", moduleInstance, Float.POSITIVE_INFINITY);
            entityRadius = ModuleProperty.getDouble(element, "entityRadius", moduleInstance, strength * 2);
        }

        public ExplosionInfo() {

        }
    }
}
