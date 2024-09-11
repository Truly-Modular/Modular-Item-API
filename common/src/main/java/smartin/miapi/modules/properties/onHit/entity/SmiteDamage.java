package smartin.miapi.modules.properties.onHit.entity;

import net.minecraft.advancements.critereon.EntityTypePredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.LivingEntity;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.util.EntityDamageBoostProperty;

/**
 * The `SmiteDamage` property increases damage dealt to undead entities.
 *
 * @header Smite Damage Property
 * @path /data_types/properties/on_hit/entity/smite_damage
 * @description_start
 * The Smite Damage Property enhances damage against undead entities.
 * This includes all entities that are classified as sensitive to the Smite effect, which typically encompasses various undead mobs.
 * @description_end
 * @data smite_damage: the amount of damage increase.
 */

public class SmiteDamage extends EntityDamageBoostProperty {
    public static final ResourceLocation KEY = Miapi.id("smite_damage");
    public static SmiteDamage property;

    public SmiteDamage() {
        super(KEY, SmiteDamage::isOfType);
        property = this;
    }

    public static boolean isOfType(LivingEntity living) {
        return EntityTypePredicate.of(EntityTypeTags.SENSITIVE_TO_SMITE).matches(living.getType());
    }
}
