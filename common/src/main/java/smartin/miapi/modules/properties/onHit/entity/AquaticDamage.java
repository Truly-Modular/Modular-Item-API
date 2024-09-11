package smartin.miapi.modules.properties.onHit.entity;

import net.minecraft.advancements.critereon.EntityTypePredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.LivingEntity;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.util.EntityDamageBoostProperty;

/**
 * The `AquaticDamage` property increases damage dealt to entities that are sensitive to impaling, such as aquatic creatures.
 *
 * @header Aquatic Damage Property
 * @path /data_types/properties/on_hit/entity/aquatic_damage
 * @description_start
 * The Aquatic Damage Property enhances damage dealt to entities that are classified as sensitive to impaling.
 * This includes aquatic creatures like fish and other water-dwelling entities.
 * @description_end
 * @data aquatic_damage: the amount of damage increase.
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
