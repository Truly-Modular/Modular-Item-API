package smartin.miapi.modules.properties;

import com.mojang.serialization.Codec;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import com.redpxnda.nucleus.codec.behavior.CodecBehavior;
import dev.architectury.event.EventResult;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import smartin.miapi.Miapi;
import smartin.miapi.events.MiapiProjectileEvents;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.Optional;

/**
 * This property is exploding projectiles on impact
 */
public class ExplosionProperty extends CodecProperty<ExplosionProperty.ExplosionInfo> {
    public static final ResourceLocation KEY = Miapi.id("explosion_projectile");
    public static final Codec<ExplosionInfo> codec = AutoCodec.of(ExplosionInfo.class).codec();
    public static ExplosionProperty property;

    public ExplosionProperty() {
        super(codec);
        property = this;
        MiapiProjectileEvents.MODULAR_PROJECTILE_ENTITY_HIT.register(event -> {
            Optional<ExplosionInfo> info = getData(event.projectile.getPickupItem());
            if (info.isPresent()) {
                if (!event.projectile.level().isClientSide()) {
                    info.get().explode(event.projectile.level(), event.projectile, event.projectile.position());
                    event.projectile.discard();
                }
                return EventResult.interruptTrue();
            }
            return EventResult.pass();
        });
        MiapiProjectileEvents.MODULAR_PROJECTILE_BLOCK_HIT.register(event -> {
            Optional<ExplosionInfo> info = getData(event.projectile.getPickupItem());
            if (info.isPresent()) {
                if (!event.projectile.level().isClientSide()) {
                    info.get().explode(event.projectile.level(), event.projectile, event.blockHitResult.getLocation());
                    event.projectile.discard();
                }
                return EventResult.interruptTrue();
            }
            return EventResult.pass();
        });
    }

    @Override
    public ExplosionInfo merge(ExplosionInfo left, ExplosionInfo right, MergeType mergeType) {
        return right;
    }

    public static class BalancedExplosionDamage extends ExplosionDamageCalculator {
        public float entityExplosionPower;
        public float entityRadius;
        public float entityMaxDamage;
        public boolean destroyBlock;

        public BalancedExplosionDamage(
                float entityExplosionPower,
                float entityRadius,
                float entityMaxDamage,
                boolean destroyBlocks) {
            super();
            this.entityExplosionPower = entityExplosionPower;
            this.entityRadius = entityRadius;
            this.entityMaxDamage = entityMaxDamage;
            this.destroyBlock = destroyBlocks;
        }

        @Override
        public boolean shouldBlockExplode(Explosion explosion, BlockGetter reader, BlockPos pos, BlockState state, float power) {
            return destroyBlock;
        }

        @Override
        public float getEntityDamageAmount(Explosion explosion, Entity entity) {
            float f = entityRadius * 2.0F;
            Vec3 vec3 = explosion.center();
            double d = Math.sqrt(entity.distanceToSqr(vec3)) / (double) f;
            double e = (1.0 - d) * (double) Explosion.getSeenPercent(vec3, entity);
            return Math.min(entityMaxDamage, (float) ((e * e + e) / 2.0 * entityExplosionPower * (double) f + 1.0));
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

        public ExplosionDamageCalculator getCalculator() {
            return new BalancedExplosionDamage((float) entityStrength, (float) entityMaxDamage, (float) entityRadius, destroyBlocks);
        }

        public void explode(Level world, Entity source, Vec3 position) {
            world.explode(
                    source,
                    null,
                    getCalculator(),
                    position,
                    (float) strength,
                    false,
                    Level.ExplosionInteraction.MOB);
        }
    }
}
