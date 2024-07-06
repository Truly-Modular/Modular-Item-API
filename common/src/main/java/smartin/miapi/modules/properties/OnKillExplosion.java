package smartin.miapi.modules.properties;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import net.minecraft.world.entity.LivingEntity;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.modules.properties.util.CodecBasedProperty;
import smartin.miapi.modules.properties.util.MergeType;

public class OnKillExplosion extends CodecBasedProperty<ExplosionProperty.ExplosionInfo> {
    public static String KEY = "on_kill_explosion";
    public static OnKillExplosion property;

    public OnKillExplosion() {
        super(ExplosionProperty.codec);
        property = this;
        EntityEvent.LIVING_DEATH.register(((entity, source) -> {
            if (source.getEntity() instanceof LivingEntity attacker) {
                ExplosionProperty.ExplosionInfo info = getData(MiapiEvents.LivingHurtEvent.getCausingItemStack(source));
                if (info != null) {
                    info.explode(attacker.level(), attacker, entity.position());
                }
            }
            return EventResult.pass();
        }));
    }

    @Override
    public ExplosionProperty.ExplosionInfo merge(ExplosionProperty.ExplosionInfo left, ExplosionProperty.ExplosionInfo right, MergeType mergeType) {
        return right;
    }
}
