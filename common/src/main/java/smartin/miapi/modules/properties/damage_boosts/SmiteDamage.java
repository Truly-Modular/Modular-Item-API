package smartin.miapi.modules.properties.damage_boosts;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import smartin.miapi.modules.properties.util.EntityDamageBoostProperty;

/**
 * this property increases damage to undead Targets
 */
public class SmiteDamage extends EntityDamageBoostProperty {
    public static final String KEY = "smite_damage";
    public static SmiteDamage property;

    public SmiteDamage() {
        super(KEY, SmiteDamage::isOfType);
        property = this;
    }

    public static boolean isOfType(LivingEntity living) {
        if(
                living.isUndead()
        ){
            return true;
        }
        return false;
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
