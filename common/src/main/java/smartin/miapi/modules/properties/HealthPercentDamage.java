package smartin.miapi.modules.properties;

import dev.architectury.event.EventResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.modules.properties.util.DoubleProperty;

/**
 * This Property allows currentHealthpercent damage ontop of already dealt damage
 */
public class HealthPercentDamage extends DoubleProperty {
    public static final String KEY = "healthPercent";
    public static HealthPercentDamage property;

    public HealthPercentDamage() {
        super(KEY);
        property = this;
        MiapiEvents.LIVING_HURT.register((livingHurtEvent -> {
            if (livingHurtEvent.damageSource.getEntity() instanceof LivingEntity livingAttacker) {
                ItemStack itemStack = livingHurtEvent.getCausingItemStack();
                if (livingAttacker instanceof Player player) {
                    if (player.oAttackAnim != 0.0) {
                        return EventResult.pass();
                    }
                }
                double percentage = getValueSafe(itemStack);
                double increasingBy = livingHurtEvent.livingEntity.getHealth() / (100 / percentage);
                livingHurtEvent.amount += increasingBy;
            }
            return EventResult.pass();
        }));
    }

    @Override
    public Double getValue(ItemStack stack) {
        Double value = property.getValueRaw(stack);
        if (value == null) {
            return null;
        }
        return ((200) / (1 + Math.exp(-Math.sqrt(value) / 50))) - 100;
    }

    @Override
    public double getValueSafe(ItemStack stack) {
        Double value = property.getValueRaw(stack);
        if (value == null) {
            return 0.0;
        }
        return ((200) / (1 + Math.exp(-Math.sqrt(value) / 50))) - 100;
    }
}
