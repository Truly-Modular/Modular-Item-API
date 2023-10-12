package smartin.miapi.modules.properties;

import com.mojang.serialization.Codec;
import com.redpxnda.nucleus.codec.AutoCodec;
import dev.architectury.event.EventResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import smartin.miapi.entity.ItemProjectileEntity;
import smartin.miapi.events.MiapiProjectileEvents;
import smartin.miapi.modules.properties.util.CodecBasedProperty;

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
                explode(info, event.projectile, event.entityHitResult);
                if (!event.projectile.getWorld().isClient()) {
                    event.projectile.discard();
                    return EventResult.interruptTrue();
                }
            }
            return EventResult.pass();
        });
        MiapiProjectileEvents.MODULAR_PROJECTILE_BLOCK_HIT.register(event -> {
            ExplosionInfo info = this.get(event.projectile.asItemStack());
            if (info != null) {
                explode(info, event.projectile, event.blockHitResult);
                if (!event.projectile.getWorld().isClient()) {
                    event.projectile.discard();
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
        World.ExplosionSourceType explosionSourceType = World.ExplosionSourceType.TNT;
        if (!info.destroyBlocks) {
            explosionSourceType = World.ExplosionSourceType.NONE;
        }
        if(!projectile.getWorld().isClient()){
            projectile.getWorld().createExplosion(projectile, result.getPos().getX(), result.getPos().getY(), result.getPos().getZ(), info.strength, explosionSourceType);
        }
    }

    public static class ExplosionInfo {
        public boolean destroyBlocks = false;
        public float strength = 1.0f;
    }
}
