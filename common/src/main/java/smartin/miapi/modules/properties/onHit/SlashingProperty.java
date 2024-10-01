package smartin.miapi.modules.properties.onHit;

import dev.architectury.event.EventResult;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import smartin.miapi.Miapi;
import smartin.miapi.events.ModularAttackEvents;
import smartin.miapi.modules.properties.util.DoubleProperty;

/**
 * @header Slashing Property
 * @path /data_types/properties/on_hit/slashing
 * @description_start The SlashingProperty introduces additional slashing damage to attacks based on the equipped item.
 * This property calculates the slashing damage and adjusts the damage dealt to entities in the game by factoring
 * in the target's **Armor** / 3 and **Armor Toughness** attributes.
 * <p>
 * When an entity is hurt, this property retrieves the slashing value of the item being used to cause the damage,
 * and reduces the slashing effect based on the target's armor attributes. If the slashing damage remains greater than
 * zero after reductions, the damage is added to the base damage dealt to the target.
 * <p>
 * This property is useful for defining weapons or tools that focus on cutting-based damage or bypassing armor,
 * giving certain items more versatility in combat scenarios.
 * @description_end
 * @data slashing: A double value indicating the amount of slashing damage that the item can deal.
 */

public class SlashingProperty extends DoubleProperty {
    public static final ResourceLocation KEY = Miapi.id("slashing");
    public static SlashingProperty property;

    public SlashingProperty() {
        super(KEY);
        property = this;
        ModularAttackEvents.ATTACK_DAMAGE_BONUS.register((target, itemStack, baseDamage, damageSource, bonusDamage) -> {
            double slashing = getValue(itemStack).orElse(0.0);
            if(target instanceof LivingEntity livingEntity){
                slashing -= livingEntity.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
                slashing -= livingEntity.getAttributeValue(Attributes.ARMOR) / 3;
            }
            if (slashing > 0) {
                bonusDamage.add((float) slashing);
            }
            return EventResult.pass();
        });
    }
}
