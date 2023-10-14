package smartin.miapi.modules.properties;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import smartin.miapi.modules.properties.util.DoubleProperty;

import java.util.ArrayList;
import java.util.UUID;

public class FracturingProperty extends DoubleProperty {
    public static final String KEY = "fracturing";
    public static FracturingProperty property;


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
                                        UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF"),
                                        "miapi_fracturing",
                                        percentageIncrease + 1,
                                        EntityAttributeModifier.Operation.MULTIPLY_TOTAL),
                                EquipmentSlot.MAINHAND,
                                false));

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
