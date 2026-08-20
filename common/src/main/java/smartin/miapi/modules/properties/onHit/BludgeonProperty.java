package smartin.miapi.modules.properties.onHit;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import smartin.miapi.Miapi;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.modules.properties.util.DoubleProperty;

/**
 * @header Bludgeon Property
 * @path /data_types/properties/on_hit/bludgeon
 * @description_start The BludgeonProperty introduces additional blunt damage to attacks based on the equipped item.
 * This property calculates bludgeoning damage and applies it as bonus damage during melee combat,
 * factoring in the target's **Armor Toughness** to determine the effectiveness.
 * <p>
 * The applied bonus damage is the bludgeon value but capped at the targets Armor Thoughness.
 * @description_end
 * @data bludgeon: A double value indicating the amount of blunt (impact) damage the item can deal.
 */
public class BludgeonProperty extends DoubleProperty {
    public static final ResourceLocation KEY = Miapi.id("bludgeon");
    public static BludgeonProperty property;

    public BludgeonProperty() {
        super(KEY);
        property = this;
        MiapiEvents.MODIFY_DAMAGE_EVENT.register((target, itemStack, baseDamage, damageSource, bonusDamage, serverLevel) -> {
            if (target instanceof LivingEntity livingEntity) {
                double bludgeonDamage = Math.min(getValue(itemStack).orElse(0.0), livingEntity.getAttributeValue(Attributes.ARMOR_TOUGHNESS));
                if (bludgeonDamage > 0) {
                    bonusDamage.add((float) bludgeonDamage);
                }
            }
        });
    }
}
