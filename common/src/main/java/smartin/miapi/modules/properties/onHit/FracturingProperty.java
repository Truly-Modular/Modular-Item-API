package smartin.miapi.modules.properties.onHit;

import dev.architectury.event.EventResult;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.events.MeleeModularAttackEvents;
import smartin.miapi.modules.properties.util.DoubleProperty;

import java.util.Optional;

/**
 * This property increases the attack damage of a weapon based on its remaining durability.
 *
 * @header Fracturing Property
 * @path /data_types/properties/on_hit/fracturing
 * @description_start
 * The Fracturing Property enhances the attack damage of a weapon as its durability decreases. This means that the lower the durability of the weapon, the higher its attack damage becomes.
 * This property is useful for creating weapons that become more powerful as they are used, adding a dynamic element to their performance.
 * The damage increase is calculated as a percentage of the weapon's current durability relative to its maximum durability.
 * @description_end
 * @data value: The percentage of increased attack damage relative to the weapon's remaining durability. A value of 100 means a 100% increase in attack damage at full wear.
 */

public class FracturingProperty extends DoubleProperty {
    public static final ResourceLocation KEY = Miapi.id("fracturing");
    public static FracturingProperty property;


    public FracturingProperty() {
        super(KEY);
        property = this;
        MeleeModularAttackEvents.ATTACK_DAMAGE_BONUS.register((target, itemStack, baseDamage, damageSource, bonusDamage) -> {
            Optional<Double> optionalStrength = getValue(itemStack);
            if (optionalStrength.isPresent() && itemStack.getMaxDamage() > 0) {
                double percentageIncrease = (optionalStrength.get() / 100) * ((double) itemStack.getDamageValue() / itemStack.getMaxDamage());
                bonusDamage.add(baseDamage * percentageIncrease);
            }
            return EventResult.pass();
        });
    }
}
