package smartin.miapi.modules.properties;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import net.minecraft.world.entity.LivingEntity;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.Optional;

public class OnKillExplosion extends CodecProperty<ExplosionProperty.ExplosionInfo> {
    public static String KEY = "on_kill_explosion";
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
