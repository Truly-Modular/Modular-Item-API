package smartin.miapi.modules.properties;

import dev.architectury.event.EventResult;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;
import smartin.miapi.Events.Event;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.util.SimpleDoubleProperty;

public class HealthPercentDamage extends SimpleDoubleProperty {
    public static final String KEY = "healthPercent";
    public static HealthPercentDamage property;

    public HealthPercentDamage() {
        super(KEY);
        property = this;
        Event.LIVING_HURT.register((livingHurtEvent -> {
            if (livingHurtEvent.damageSource.getAttacker() instanceof LivingEntity livingAttacker) {
                ItemStack itemStack = livingAttacker.getMainHandStack();
                if(livingAttacker instanceof PlayerEntity player){
                    if(player.lastHandSwingProgress!=0.0){
                        return EventResult.pass();
                    }
                }
                double percentage = actualValue(itemStack);
                double increasingBy = livingHurtEvent.livingEntity.getHealth() / (100 / percentage);
                livingHurtEvent.amount += increasingBy;
            }
            return EventResult.pass();
        }));
    }

    public static double actualValue(ItemStack itemStack) {
        Double value = property.getValue(itemStack);
        if (value == null) {
            return 0;
        }
        return ((200) / (1 + Math.exp(-Math.sqrt(value) / 50))) - 100;
    }
}
