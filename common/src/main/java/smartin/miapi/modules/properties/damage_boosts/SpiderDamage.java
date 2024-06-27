package smartin.miapi.modules.properties.damage_boosts;

import net.minecraft.entity.EntityGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.modules.properties.util.EntityDamageBoostProperty;

/**
 * this property increases damage to undead Targets
 */
public class SpiderDamage extends EntityDamageBoostProperty {
    public static final String KEY = "spider_damage";
    public static SpiderDamage property;

    public SpiderDamage() {
        super(KEY, SpiderDamage::isOfType);
        property = this;
    }

    public static boolean isOfType(LivingEntity living) {
        if(living.getGroup() == EntityGroup.ARTHROPOD){
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
