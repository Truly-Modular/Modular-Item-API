package smartin.miapi.modules.properties;

import dev.architectury.event.EventResult;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import smartin.miapi.events.MiapiEvents;

public class OnHitTargetEffects extends PotionEffectProperty {
    public static String KEY = "on_hit_potion";
    public OnHitTargetEffects property;

    public OnHitTargetEffects() {
        super(KEY, Text.translatable("miapi.potion.target.other"));
        property = this;
        MiapiEvents.LIVING_HURT.register((listener) -> {
            if (listener.damageSource.getAttacker() instanceof LivingEntity livingEntity && !livingEntity.getWorld().isClient()) {
                applyEffects(listener.livingEntity, livingEntity, livingEntity);
            }
            return EventResult.pass();
        });
    }
}
