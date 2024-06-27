package smartin.miapi.modules.properties;

import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.util.DoubleProperty;

import java.util.ArrayList;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

/**
 * This property increases the Attackdamage of a weapon the lower the durability and the higher its value is.
 */
public class FracturingProperty extends DoubleProperty {
    public static final String KEY = "fracturing";
    public static FracturingProperty property;
    public static ResourceLocation ATTRIBUTE_ID = Miapi.id("fracturing_property_damage");


    public FracturingProperty() {
        super(KEY);
        property = this;
        AttributeProperty.attributeTransformers.add((map, itemstack) -> {
            double strength = getValueSafe(itemstack);
            if (strength > 0 && itemstack.getMaxDamage() > 0) {
                var holders = map.asMap().getOrDefault(Attributes.ATTACK_DAMAGE, new ArrayList<>());
                double percentageIncrease = (strength / 100) * ((double) itemstack.getDamageValue() / itemstack.getMaxDamage());
                holders.add(
                        new AttributeProperty.EntityAttributeModifierHolder(
                                new AttributeModifier(
                                        ATTRIBUTE_ID,
                                        percentageIncrease + 1,
                                        AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),
                                EquipmentSlot.MAINHAND,
                                AttributeModifier.Operation.ADD_VALUE));

            }
            return map;
        });
    }

    @Override
    public Double getValue(ItemStack stack) {
        return getValueRaw(stack);
    }

    @Override
    public double getValueSafe(ItemStack stack) {
        return Math.max(0, getValueSafeRaw(stack));
    }
}
