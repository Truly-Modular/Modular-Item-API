package smartin.miapi.modules.properties.util;

import dev.architectury.event.EventResult;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import smartin.miapi.events.MiapiEvents;

public abstract class EntityDamageBoostProperty extends DoubleProperty {

    public EntityDamageBoostProperty(String key, isOfEntity entityPredicate) {
        super(key);
        MiapiEvents.LIVING_HURT.register((livingHurtEvent) -> {
            if (!livingHurtEvent.defender.getWorld().isClient() && livingHurtEvent.defender.getWorld() instanceof ServerWorld serverWorld) {
                ItemStack itemStack = livingHurtEvent.getCausingItemStack();
                Double value = getValue(itemStack);
                if (value != null && entityPredicate.test(livingHurtEvent.defender)) {
                    livingHurtEvent.amount += value;
                }
            }
            return EventResult.pass();
        });
    }

    public interface isOfEntity {
        boolean test(LivingEntity living);
    }

    @Override
    public Double getValue(ItemStack stack) {
        return getValueRaw(stack);
    }

    @Override
    public double getValueSafe(ItemStack stack) {
        return getValueSafeRaw(stack);
    }
}
