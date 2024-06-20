package smartin.miapi.modules.properties.damage_boosts;

import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import smartin.miapi.modules.properties.util.EntityDamageBoostProperty;

/**
 * this property increases damage to undead Targets
 */
public class AquaticDamage extends EntityDamageBoostProperty {
    public static final String KEY = "aquatic_damage";
    public static AquaticDamage property;

    public AquaticDamage() {
        super(KEY, AquaticDamage::isOfType);
        property = this;
    }

    public static boolean isOfType(LivingEntity living) {
        if(living.getGroup() == EntityGroup.AQUATIC){
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
