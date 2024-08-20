package smartin.miapi.modules.properties.onHit;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.attributes.AttributeProperty;
import smartin.miapi.modules.properties.util.DoubleProperty;

import java.util.ArrayList;
import java.util.Optional;

/**
 * This property increases the Attackdamage of a weapon the lower the durability and the higher its value is.
 */
public class FracturingProperty extends DoubleProperty {
    public static final ResourceLocation KEY = Miapi.id("fracturing");
    public static FracturingProperty property;
    public static ResourceLocation ATTRIBUTE_ID = Miapi.id("fracturing_property_damage");


    public FracturingProperty() {
        super(KEY);
        property = this;
        AttributeProperty.attributeTransformers.add((map, itemstack) -> {
            Optional<Double> optionalStrength = getValue(itemstack);
            if (optionalStrength.isPresent() && itemstack.getMaxDamage() > 0) {
                var holders = map.asMap().getOrDefault(Attributes.ATTACK_DAMAGE, new ArrayList<>());
                double percentageIncrease = (optionalStrength.get() / 100) * ((double) itemstack.getDamageValue() / itemstack.getMaxDamage());
                holders.add(
                        new AttributeProperty.EntityAttributeModifierHolder(
                                new AttributeModifier(
                                        ATTRIBUTE_ID,
                                        percentageIncrease + 1,
                                        AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),
                                EquipmentSlotGroup.MAINHAND,
                                AttributeModifier.Operation.ADD_VALUE));

            }
            return map;
        });
    }
}
