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
public class SpiderDamage extends EntityDamageBoostProperty {
    public static final ResourceLocation KEY = Miapi.id("spider_damage");
    public static SpiderDamage property;

    public SpiderDamage() {
        super(KEY, SpiderDamage::isOfType);
        property = this;
    }

    public static boolean isOfType(LivingEntity living) {
        return EntityTypePredicate.of(EntityTypeTags.SENSITIVE_TO_SMITE).matches(living.getType());
    }
}
