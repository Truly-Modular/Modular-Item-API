package smartin.miapi.Events;

import dev.architectury.event.EventFactory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;

import java.util.function.Consumer;

public class Event {
    public static dev.architectury.event.Event<Consumer<LivingHurtEvent>> LIVING_HURT = EventFactory.createEventResult();

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
}
