package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import net.minecraft.world.entity.LivingEntity;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.util.ModuleProperty;

public class OnKillExplosion implements ModuleProperty {
    public static String KEY = "on_kill_explosion";
    public static OnKillExplosion property;

    public OnKillExplosion() {
        property = this;
        EntityEvent.LIVING_DEATH.register(((entity, source) -> {
            if (source.getEntity() instanceof LivingEntity attacker) {
                JsonElement element = getJsonElement(MiapiEvents.LivingHurtEvent.getCausingItemStack(source));
                if (element != null) {
                    ExplosionProperty.ExplosionInfo info = ExplosionProperty.property.getInfo(attacker.getMainHandItem(), property);
                    if (info != null) {
                        ExplosionProperty.explode(info, attacker.level(), entity.position(), attacker);
                    }
                }
            }
            return EventResult.pass();
        }));
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        ExplosionProperty.ExplosionInfo info = new ExplosionProperty.ExplosionInfo(data.getAsJsonObject(), new ModuleInstance(ItemModule.empty));
        return true;
    }
}
