package smartin.miapi.modules.properties.damage_boosts;

import net.minecraft.advancements.critereon.EntityTypePredicate;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.modules.properties.util.EntityDamageBoostProperty;

/**
 * this property increases damage to Raid type mods
 */
public class IllagerBane extends EntityDamageBoostProperty {
    ///TODO:look how to better detect those entitys, maybe a tag and look into mod compat as well
    public static final String KEY = "illagerBane";
    public static IllagerBane property;

    public IllagerBane() {
        super(KEY,IllagerBane::isIllagerType);
        property = this;
    }

    public static boolean isIllagerType(LivingEntity living) {
        return EntityTypePredicate.of(EntityTypeTags.ILLAGER).matches(living.getType()) ||
               EntityTypePredicate.of(EntityTypeTags.ILLAGER_FRIENDS).matches(living.getType());
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
