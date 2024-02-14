package smartin.miapi.modules.properties.util;

import dev.architectury.event.EventResult;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.server.world.ServerWorld;
import smartin.miapi.events.MiapiEvents;

public abstract class EntityDamageBoostProperty extends DoubleProperty {

    public EntityDamageBoostProperty(String key, EntityPredicate entityPredicate) {
        super(key);
        MiapiEvents.LIVING_HURT.register((livingHurtEvent) -> {
            if (!livingHurtEvent.livingEntity.getWorld().isClient() && livingHurtEvent.livingEntity.getWorld() instanceof ServerWorld serverWorld) {
                ItemStack itemStack = livingHurtEvent.getCausingItemStack();
                Double value = getValue(itemStack);
                if (value != null && entityPredicate.test(serverWorld, livingHurtEvent.livingEntity.getPos(), livingHurtEvent.livingEntity)) {
                    livingHurtEvent.amount += value;
                }
            }
            return EventResult.pass();
        });
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
