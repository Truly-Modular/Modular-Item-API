package smartin.miapi.modules.properties.damage_boosts;

import net.minecraft.advancements.critereon.EntityTypePredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.LivingEntity;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.util.EntityDamageBoostProperty;

/**
 * this property increases damage to undead Targets
 */
public class AquaticDamage extends EntityDamageBoostProperty {
    public static final ResourceLocation KEY = Miapi.id("aquatic_damage");
    public static AquaticDamage property;

    public AquaticDamage() {
        super(KEY, AquaticDamage::isOfType);
        property = this;
    }

    public static boolean isOfType(LivingEntity living) {
        return EntityTypePredicate.of(EntityTypeTags.SENSITIVE_TO_IMPALING).matches(living.getType());
    }
}
