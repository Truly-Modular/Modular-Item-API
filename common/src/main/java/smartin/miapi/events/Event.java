package smartin.miapi.events;

import dev.architectury.event.EventFactory;
import dev.architectury.event.EventResult;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;

public class Event {
    public static dev.architectury.event.Event<LivingHurt> LIVING_HURT = EventFactory.createEventResult();

    public static dev.architectury.event.Event<LivingHurt> LIVING_HURT_AFTER = EventFactory.createEventResult();

    public static class LivingHurtEvent {
        public final LivingEntity livingEntity;
        public DamageSource damageSource;
        public float amount;

        public LivingHurtEvent(LivingEntity livingEntity, DamageSource damageSource, float amount) {
            this.livingEntity = livingEntity;
            this.damageSource = damageSource;
            this.amount = amount;

        }
    }

    public interface LivingHurt {
        EventResult hurt(LivingHurtEvent event);
    }
}
