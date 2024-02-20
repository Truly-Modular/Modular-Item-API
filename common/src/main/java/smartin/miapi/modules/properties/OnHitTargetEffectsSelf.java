package smartin.miapi.modules.properties;

import dev.architectury.event.EventResult;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import smartin.miapi.events.MiapiEvents;

public class OnHitTargetEffectsSelf extends PotionEffectProperty {
    public static String KEY = "on_hit_potion_self";
    public OnHitTargetEffectsSelf property;

    public OnHitTargetEffectsSelf() {
        super(KEY);
        property = this;
        MiapiEvents.LIVING_HURT.register((listener) -> {
            if (listener.damageSource.getAttacker() instanceof LivingEntity livingEntity && !livingEntity.getWorld().isClient()) {
                applyPotions(livingEntity, livingEntity.getHandItems(), livingEntity);
            }
            return EventResult.pass();
        });
    }
}
