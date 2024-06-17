package smartin.miapi.modules.properties;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.util.DoubleProperty;

import java.util.ArrayList;

/**
 * This property increases the Attackdamage of a weapon the lower the durability and the higher its value is.
 */
public class FracturingProperty extends DoubleProperty {
    public static final String KEY = "fracturing";
    public static FracturingProperty property;
    public static Identifier ATTRIBUTE_ID = Miapi.MiapiIdentifier("fracturing_property_damage");


    public FracturingProperty() {
        super(KEY);
        property = this;
        AttributeProperty.attributeTransformers.add((map, itemstack) -> {
            double strength = getValueSafe(itemstack);
            if (strength > 0 && itemstack.getMaxDamage() > 0) {
                var holders = map.asMap().getOrDefault(EntityAttributes.GENERIC_ATTACK_DAMAGE, new ArrayList<>());
                double percentageIncrease = (strength / 100) * ((double) itemstack.getDamage() / itemstack.getMaxDamage());
                holders.add(
                        new AttributeProperty.EntityAttributeModifierHolder(
                                new EntityAttributeModifier(
                                        ATTRIBUTE_ID,
                                        percentageIncrease + 1,
                                        EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),
                                EquipmentSlot.MAINHAND,
                                EntityAttributeModifier.Operation.ADD_VALUE));

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
