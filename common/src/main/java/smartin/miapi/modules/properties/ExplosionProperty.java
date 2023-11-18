package smartin.miapi.modules.properties;

import com.mojang.serialization.Codec;
import com.redpxnda.nucleus.codec.AutoCodec;
import dev.architectury.event.EventResult;
import net.minecraft.block.BlockState;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import smartin.miapi.entity.ItemProjectileEntity;
import smartin.miapi.events.MiapiProjectileEvents;
import smartin.miapi.modules.properties.util.CodecBasedProperty;

import java.util.Optional;

/**
 * This property is exploding projectiles on impact
 */
public class ExplosionProperty extends CodecBasedProperty<ExplosionProperty.ExplosionInfo> {
    public static final String KEY = "explosion_projectile";
    public static final Codec<ExplosionInfo> codec = AutoCodec.of(ExplosionInfo.class).codec();

    public ExplosionProperty() {
        super(KEY, codec);
        MiapiProjectileEvents.MODULAR_PROJECTILE_ENTITY_HIT.register(event -> {
            ExplosionInfo info = this.get(event.projectile.asItemStack());
            if (info != null) {
                if (!event.projectile.getWorld().isClient()) {
                    event.projectile.discard();
                    explode(info, event.projectile, event.entityHitResult);
                    return EventResult.interruptTrue();
                }
            }
            return EventResult.pass();
        });
        MiapiProjectileEvents.MODULAR_PROJECTILE_BLOCK_HIT.register(event -> {
            ExplosionInfo info = this.get(event.projectile.asItemStack());
            if (info != null) {
                if (!event.projectile.getWorld().isClient()) {
                    event.projectile.discard();
                    explode(info, event.projectile, event.blockHitResult);
                    return EventResult.interruptTrue();
                }
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
        World.ExplosionSourceType explosionSourceType = World.ExplosionSourceType.MOB;
        FireballEntity entity;
        FireworkRocketEntity entity1;
        if (!info.destroyBlocks) {
            explosionSourceType = World.ExplosionSourceType.NONE;
        }
        if (!projectile.getWorld().isClient()) {
            double x = result.getPos().getX();// - projectile.getVelocity().getX() / 60;
            double y = result.getPos().getY();// - projectile.getVelocity().getY() / 60;
            double z = result.getPos().getZ();// - projectile.getVelocity().getZ() / 60;
            projectile.getWorld().createExplosion(projectile.getOwner(), null, new ArrowExplosionInfo(), x, y, z, info.strength, false, explosionSourceType);
        }
    }

    public class ArrowExplosionInfo extends ExplosionBehavior {
        public Optional<Float> getBlastResistance(Explosion explosion, BlockView world, BlockPos pos, BlockState blockState, FluidState fluidState) {
            Optional<Float> distance = blockState.isAir() && fluidState.isEmpty() ? Optional.empty() : Optional.of(Math.max(blockState.getBlock().getBlastResistance(), fluidState.getBlastResistance()));
            if (distance.isPresent()) {
                return Optional.of(0f);
            }
            return distance;
        }
    }

    public static class ExplosionInfo {
        public boolean destroyBlocks = false;
        public float strength = 1.0f;
    }
}
