package smartin.miapi.modules.properties.onHit.entity;

import net.minecraft.advancements.critereon.EntityTypePredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.LivingEntity;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.util.EntityDamageBoostProperty;

/**
 * The `IllagerBane` property increases damage dealt to Illager-type entities and their allies.
 *
 * @header Illager Bane Property
 * @path /data_types/properties/on_hit/entity/illager_bane
 * @description_start
 * The Illager Bane Property boosts damage dealt to entities classified as Illagers or their allies. This includes Illagers and their associated friends, providing enhanced effectiveness against these specific entity types.
 * This property is particularly useful in scenarios where players need to deal additional damage to Illager-type mobs, such as during raids or in areas populated with these enemies.
 * @description_end
 * @data illager_bane: the amount of damage increase.
 */

public class IllagerBane extends EntityDamageBoostProperty {
    ///TODO:look how to better detect those entitys, maybe a tag and look into mod compat as well
    public static final ResourceLocation KEY = Miapi.id("illager_bane");
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
