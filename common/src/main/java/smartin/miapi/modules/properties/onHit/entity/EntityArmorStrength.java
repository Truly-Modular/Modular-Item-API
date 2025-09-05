package smartin.miapi.modules.properties.onHit.entity;

import dev.architectury.event.EventResult;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import smartin.miapi.Miapi;
import smartin.miapi.events.MiapiEvents;

public class EntityArmorStrength extends GenericEntityStrengthProperty {
    public static final ResourceLocation KEY = Miapi.id("entity_armor");

    public EntityArmorStrength() {
        super();
        MiapiEvents.LIVING_HURT.register(new MiapiEvents.LivingHurt() {
            @Override
            public EventResult hurt(MiapiEvents.LivingHurtEvent event) {
                if (event.attacker != null) {
                    double strength =
                            strengthForEntity(
                                    event.attacker.getType().arch$holder(),
                                    event.defender.getArmorAndBodyArmorSlots());
                    event.amount *= (float) valueRemap(strength);
                }
                return EventResult.pass();
            }
        });
    }

    @Override
    public Component getFallbackName(EntityType<?> firstType) {
        return Component.translatable("miapi.property.entity.armor.default", firstType.getDescription());
    }

    @Override
    public Component getBaseDescription(double strength) {
        return Component.translatable("miapi.property.entity.armor.description", 1 - valueRemap(strength));
    }

    public static double valueRemap(double x) {
        return 1 - (2 / (1 + Math.exp(-x / 10)) - 1);
    }
}
