package smartin.miapi.modules.properties.damage_boosts;

import net.minecraft.advancements.critereon.EntityTypePredicate;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.LivingEntity;
import smartin.miapi.modules.properties.util.EntityDamageBoostProperty;

/**
 * this property increases damage to Raid type mods
 */
public class IllagerBane extends EntityDamageBoostProperty {
    ///TODO:look how to better detect those entitys, maybe a tag and look into mod compat as well
    public static final String KEY = "illager_bane";
    public static IllagerBane property;

    public IllagerBane() {
        super(KEY,IllagerBane::isIllagerType);
        property = this;
    }

    public static boolean isIllagerType(LivingEntity living) {
        return EntityTypePredicate.of(EntityTypeTags.ILLAGER).matches(living.getType()) ||
               EntityTypePredicate.of(EntityTypeTags.ILLAGER_FRIENDS).matches(living.getType());
    }
}
