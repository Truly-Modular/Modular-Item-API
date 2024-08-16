package smartin.miapi.modules.properties.util;

import dev.architectury.event.EventResult;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.events.MiapiEvents;

public abstract class EntityDamageBoostProperty extends DoubleProperty {

    public EntityDamageBoostProperty(ResourceLocation key, isOfEntity entityPredicate) {
        super(key);
        MiapiEvents.LIVING_HURT.register((livingHurtEvent) -> {
            if (!livingHurtEvent.livingEntity.level().isClientSide() && livingHurtEvent.livingEntity.level() instanceof ServerLevel serverWorld) {
                ItemStack itemStack = livingHurtEvent.getCausingItemStack();
                Double value = getValue(itemStack).orElse(0.0);
                if (value != null && entityPredicate.test(livingHurtEvent.livingEntity)) {
                    livingHurtEvent.amount += value;
                }
            }
            return EventResult.pass();
        });
    }

    public interface isOfEntity {
        boolean test(LivingEntity living);
    }
}
