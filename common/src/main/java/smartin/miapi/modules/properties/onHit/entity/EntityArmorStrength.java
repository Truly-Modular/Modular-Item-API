package smartin.miapi.modules.properties.onHit.entity;

import dev.architectury.event.EventResult;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import smartin.miapi.Miapi;
import smartin.miapi.entity.EntityHelper;
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
                                    EntityHelper.getEquipedNonHandItems(event.defender));
                    event.amount *= (1 - (float) valueRemap(strength));
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
        return Component.translatable("miapi.property.entity.armor.description", String.format("%.2f", (100 * valueRemap(strength))));
    }

    public static double valueRemap(double x) {
        if (x < 0) {
            return -reductionHyperbolic(-x, 50, 3);
        }
        return reductionHyperbolic(x, 50, 3);
    }

    /**
     * This function is used to scale the incomming damage down, but have diminishing returns at an upper cap.
     * it works by calculating the reduction as a gain in effective health and converting this in a % dmg reduction.
     *
     * @param a the value to be rempaed
     * @param K scalling factor, scales how fast the limit m is approached
     * @param M the upper limit of effective health gain.
     * @return the remaped value
     */
    public static double reductionHyperbolic(double a, double K, double M) {
        double ehp = 1.0 + M * (a / (a + K));
        return 1.0 - 1.0 / ehp;
    }
}
