package smartin.miapi.modules.properties;

import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ibm.icu.impl.Pair;
import com.mojang.serialization.Codec;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import dev.architectury.event.EventResult;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.ProtectionEnchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.explosion.EntityExplosionBehavior;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.craft.stat.StatProvidersMap;
import smartin.miapi.entity.ItemProjectileEntity;
import smartin.miapi.events.MiapiProjectileEvents;
import smartin.miapi.modules.ItemModule;
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
            @Nullable Pair<ItemModule.ModuleInstance, JsonElement> jsonElement = this.highestPriorityJsonElement(event.projectile.asItemStack());
            if (jsonElement != null) {
                ExplosionInfo info = new ExplosionInfo(jsonElement.second.getAsJsonObject(), jsonElement.first);
                if (!event.projectile.getWorld().isClient()) {
                    explode(info, event.projectile, event.entityHitResult);
                    event.projectile.discard();
                }
                return EventResult.interruptTrue();
            }
            return EventResult.pass();
        });
        MiapiProjectileEvents.MODULAR_PROJECTILE_BLOCK_HIT.register(event -> {
            ExplosionInfo info = this.get(event.projectile.asItemStack());
            if (info != null) {
                if (!event.projectile.getWorld().isClient()) {
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
    private void explode(ExplosionInfo info, ItemProjectileEntity projectile, HitResult result) {
        double x = result.getPos().getX();// - projectile.getVelocity().getX() / 60;
        double y = result.getPos().getY();// - projectile.getVelocity().getY() / 60;
        double z = result.getPos().getZ();// - projectile.getVelocity().getZ() / 60;
        ExplosionBehavior behavior = new ArrowExplosionInfo();
        Explosion.DestructionType destructionType;
        if (info.destroyBlocks) {
            destructionType = Explosion.DestructionType.DESTROY;
        } else {
            destructionType = Explosion.DestructionType.KEEP;
        }

        MiapiExplosion explosion = new MiapiExplosion(projectile.getWorld(),
                projectile.getOwner() == null ? projectile : projectile.getOwner(),
                (DamageSource) null, behavior, x, y, z, (float) info.strength, false, destructionType);
        explosion.entityMaxDamage = (float) info.entityMaxDamage;
        explosion.entityRadius = (float) info.entityRadius;
        explosion.entityExplosionPower = (float) info.entityStrength;
        if (!projectile.getWorld().isClient) {
            explosion.collectBlocksAndDamageEntities();
        }
        explosion.affectWorld(true);
        if (!projectile.getWorld().isClient()) {
            if (!explosion.shouldDestroy()) {
                explosion.clearAffectedBlocks();
            }

            Iterator var14 = projectile.getWorld().getPlayers().iterator();

            while (var14.hasNext()) {
                ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity) var14.next();
                if (serverPlayerEntity.squaredDistanceTo(x, y, z) < 4096.0) {
                    serverPlayerEntity.networkHandler.sendPacket(new ExplosionS2CPacket(x, y, z, (float) info.strength, explosion.getAffectedBlocks(), explosion.getAffectedPlayers().get(serverPlayerEntity)));
                }
            }
        }
    }

    public static class MiapiExplosion extends Explosion {
        private static final ExplosionBehavior DEFAULT_BEHAVIOR = new ExplosionBehavior();
        public float blockExplosionPower;
        public float entityExplosionPower;
        public float entityRadius;
        public float entityMaxDamage;
        private World world;
        private final double explosionX;
        private final double explosionY;
        private final double explosionZ;
        private final ExplosionBehavior behavior;

        public MiapiExplosion(World world, @Nullable Entity entity, @Nullable DamageSource damageSource, @Nullable ExplosionBehavior behavior, double x, double y, double z, float power, boolean createFire, DestructionType destructionType) {
            super(world, entity, damageSource, behavior, x, y, z, power, createFire, destructionType);
            this.world = world;
            this.explosionX = x;
            this.explosionY = y;
            this.explosionZ = z;
            this.behavior = behavior == null ? this.chooseBehavior(entity) : behavior;
            //default vanilla values
            blockExplosionPower = power;
            entityExplosionPower = power * 7;
            entityRadius = power * 2;
            entityMaxDamage = Float.POSITIVE_INFINITY;
            // Loop through the list of entities
        }

        private ExplosionBehavior chooseBehavior(@Nullable Entity entity) {
            return entity == null ? DEFAULT_BEHAVIOR : new EntityExplosionBehavior(entity);
        }

        @Override
        public void collectBlocksAndDamageEntities() {
            this.world.emitGameEvent(this.entity, GameEvent.EXPLODE, new Vec3d(this.explosionX, this.explosionY, this.explosionZ));
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
                                BlockPos blockPos = BlockPos.ofFloored(currentX, currentY, currentZ);
                                BlockState blockState = this.world.getBlockState(blockPos);
                                FluidState fluidState = this.world.getFluidState(blockPos);

                                if (!this.world.isInBuildLimit(blockPos)) {
                                    break;
                                }

                                Optional<Float> resistance = this.behavior.getBlastResistance(this, this.world, blockPos, blockState, fluidState);

                                if (resistance.isPresent()) {
                                    explosionPower -= (resistance.get() + 0.3F) * 0.3F;
                                }

                                if (explosionPower > 0.0F && this.behavior.canDestroyBlock(this, this.world, blockPos, blockState, explosionPower)) {
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

            this.getAffectedBlocks().addAll(affectedBlockPositions);
            int minX = MathHelper.floor(this.explosionX - entityRadius - 1.0);
            int maxX = MathHelper.floor(this.explosionX + entityRadius + 1.0);
            int minY = MathHelper.floor(this.explosionY - entityRadius - 1.0);
            int maxY = MathHelper.floor(this.explosionY + entityRadius + 1.0);
            int minZ = MathHelper.floor(this.explosionZ - entityRadius - 1.0);
            int maxZ = MathHelper.floor(this.explosionZ + entityRadius + 1.0);
            List<Entity> nearbyEntities = this.world.getOtherEntities(this.entity, new Box(minX, minY, minZ, maxX, maxY, maxZ));
            nearbyEntities.add(entity);
            Vec3d explosionCenter = new Vec3d(this.explosionX, this.explosionY, this.explosionZ);

            for (int i = 0; i < nearbyEntities.size(); ++i) {
                Entity targetEntity = nearbyEntities.get(i);

                if (!targetEntity.isImmuneToExplosion()) {
                    double distanceToCenter = Math.sqrt(targetEntity.squaredDistanceTo(explosionCenter)) / entityRadius;

                    if (distanceToCenter <= 1.0) {
                        double deltaX = targetEntity.getX() - this.explosionX;
                        double deltaY = (targetEntity instanceof TntEntity ? targetEntity.getY() : targetEntity.getEyeY()) - this.explosionY;
                        double deltaZ = targetEntity.getZ() - this.explosionZ;
                        double entityDistance = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);

                        if (entityDistance != 0.0) {
                            deltaX /= entityDistance;
                            deltaY /= entityDistance;
                            deltaZ /= entityDistance;
                            double exposure = getExposure(explosionCenter, targetEntity);
                            double damage = (1.0 - distanceToCenter) * exposure;
                            float totalDamage = Math.min(entityMaxDamage, ((int) ((damage * damage + damage) / 2.0 * entityExplosionPower + 1.0)));
                            targetEntity.damage(this.getDamageSource(), totalDamage);

                            double knockback;

                            if (targetEntity instanceof LivingEntity livingEntity) {
                                knockback = ProtectionEnchantment.transformExplosionKnockback(livingEntity, damage);
                            } else {
                                knockback = damage;
                            }

                            deltaX *= knockback;
                            deltaY *= knockback;
                            deltaZ *= knockback;
                            Vec3d velocity = new Vec3d(deltaX, deltaY, deltaZ);
                            targetEntity.setVelocity(targetEntity.getVelocity().add(velocity));

                            if (targetEntity instanceof PlayerEntity playerEntity) {

                                if (!playerEntity.isSpectator() && (!playerEntity.isCreative() || !playerEntity.getAbilities().flying)) {
                                    this.getAffectedPlayers().put(playerEntity, velocity);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public class ArrowExplosionInfo extends ExplosionBehavior {
        public Optional<Float> getBlastResistance(Explosion explosion, BlockView world, BlockPos pos, BlockState blockState, FluidState fluidState) {
            Optional<Float> distance = blockState.isAir() && fluidState.isEmpty() ? Optional.empty() : Optional.of(Math.max(blockState.getBlock().getBlastResistance(), fluidState.getBlastResistance()));
            if (distance.isPresent()) {
                //return Optional.of(0f);
            }
            return distance;
        }
    }

    public static class ExplosionInfo {
        public boolean destroyBlocks;
        public double chance;
        public double strength;
        public double entityStrength;
        public double entityMaxDamage;
        public double entityRadius;

        public ExplosionInfo(JsonObject element, ItemModule.ModuleInstance moduleInstance) {
            destroyBlocks = ModuleProperty.getBoolean(element, "destroyBlocks", false);
            chance = ModuleProperty.getDouble(element, "chance", moduleInstance, 1.0);
            strength = ModuleProperty.getDouble(element, "strength", moduleInstance, 1.0);
            entityStrength = ModuleProperty.getDouble(element, "entityStrength", moduleInstance, strength * 7);
            entityMaxDamage = ModuleProperty.getDouble(element, "entityMaxDamage", moduleInstance, Float.POSITIVE_INFINITY);
            entityRadius = ModuleProperty.getDouble(element, "entityRadius", moduleInstance, strength * 2);
        }
    }
}
