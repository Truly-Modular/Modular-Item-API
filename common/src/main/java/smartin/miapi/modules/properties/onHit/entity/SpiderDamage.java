package smartin.miapi.modules.properties.onHit.entity;

import net.minecraft.advancements.critereon.EntityTypePredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.LivingEntity;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.util.EntityDamageBoostProperty;

/**
 * The `SpiderDamage` property increases damage dealt to spiders and other arthropod-type entities.
 *
 * @header Spider Damage Property
 * @path /data_types/properties/on_hit/entity/spider_damage
 * @description_start
 * The Spider Damage Property enhances damage against spiders and other entities that are sensitive to the Bane of Arthropods enchantment.
 * @description_end
 * @data spider_damage: the amount of damage increase.
 */

public class SpiderDamage extends EntityDamageBoostProperty {
    public static final ResourceLocation KEY = Miapi.id("spider_damage");
    public static SpiderDamage property;

    public SpiderDamage() {
        super(KEY, SpiderDamage::isOfType);
        property = this;
    }

    public static boolean isOfType(LivingEntity living) {
        return EntityTypePredicate.of(EntityTypeTags.SENSITIVE_TO_BANE_OF_ARTHROPODS).matches(living.getType());
    }
}
