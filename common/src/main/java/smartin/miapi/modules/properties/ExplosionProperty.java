package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import com.redpxnda.nucleus.codec.behavior.CodecBehavior;
import dev.architectury.event.EventResult;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Tuple;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.events.MiapiProjectileEvents;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.util.CodecBasedProperty;
import smartin.miapi.modules.properties.util.ModuleProperty;

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
                    info.explode(event.projectile.level(), event.projectile, event.projectile.position());
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
                    info.explode(event.projectile.level(), event.projectile, event.blockHitResult.getLocation());
                    event.projectile.discard();
                }
                return EventResult.interruptTrue();
            }
            return EventResult.pass();
        });
    }

    public ExplosionInfo getInfo(ItemStack itemStack, ModuleProperty property) {
        ExplosionInfo info = null;
        for (ModuleInstance moduleInstance : ItemModule.getModules(itemStack).allSubModules()) {
            if (moduleInstance.getOldProperties().containsKey(property)) {
                info = new ExplosionInfo(moduleInstance.getOldProperties().get(property).getAsJsonObject(), moduleInstance);
            }
        }
        return info;
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

        public ExplosionInfo(JsonObject element, ModuleInstance moduleInstance) {
            destroyBlocks = ModuleProperty.getBoolean(element, "destroyBlocks", false);
            chance = ModuleProperty.getDouble(element, "chance", moduleInstance, 1.0);
            strength = ModuleProperty.getDouble(element, "strength", moduleInstance, 1.0);
            entityStrength = ModuleProperty.getDouble(element, "entityStrength", moduleInstance, strength * 7);
            entityMaxDamage = ModuleProperty.getDouble(element, "entityMaxDamage", moduleInstance, Float.POSITIVE_INFINITY);
            entityRadius = ModuleProperty.getDouble(element, "entityRadius", moduleInstance, strength * 2);
        }

        public ExplosionDamageCalculator getCalculator() {
            return new BalancedExplosionDamage((float) entityStrength, (float) entityMaxDamage, (float) entityRadius, destroyBlocks);
        }

        public void explode(Level world, Entity source, Vec3 position) {
            world.explode(
                    source,
                    (DamageSource) null,
                    getCalculator(),
                    position,
                    (float) strength,
                    false,
                    Level.ExplosionInteraction.MOB);
        }
    }
}
