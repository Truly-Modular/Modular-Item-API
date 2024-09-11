package smartin.miapi.modules.properties.onHit;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import smartin.miapi.Miapi;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.modules.properties.projectile.ExplosionProperty;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.Optional;

/**
 *
 * @header Explosion On Hit Property
 * @path /data_types/properties/on_hit/explosion
 * @description_start
 * Explosion on a melee kill, the explosion is configurable like other explosion
 * @description_end
 * @data value: The number of lightning bolts that will strike the location of the entity being hit. For example, a value of 3 means three lightning bolts will appear.
 */
public class OnKillExplosion extends CodecProperty<ExplosionProperty.ExplosionInfo> {
    public static final ResourceLocation KEY = Miapi.id("on_kill_explosion");
    public static OnKillExplosion property;

    public OnKillExplosion() {
        super(ExplosionProperty.codec);
        property = this;
        EntityEvent.LIVING_DEATH.register(((entity, source) -> {
            if (source.getEntity() instanceof LivingEntity attacker) {
                Optional<ExplosionProperty.ExplosionInfo> info = getData(MiapiEvents.LivingHurtEvent.getCausingItemStack(source));
                info.ifPresent(explosionInfo -> explosionInfo.explode(attacker.level(), attacker, entity.position()));
            }
            return EventResult.pass();
        }));
    }

    @Override
    public ExplosionProperty.ExplosionInfo merge(ExplosionProperty.ExplosionInfo left, ExplosionProperty.ExplosionInfo right, MergeType mergeType) {
        return right;
    }
}
