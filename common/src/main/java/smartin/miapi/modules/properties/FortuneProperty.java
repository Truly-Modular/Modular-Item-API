package smartin.miapi.modules.properties;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import smartin.miapi.item.FakeEnchantment;
import smartin.miapi.modules.properties.util.DoubleProperty;

/**
 * This property allows to adjust the fortune level of a Tool
 */
public class FortuneProperty extends DoubleProperty {
    public static final String KEY = "fortune";
    public static FortuneProperty property;


    public FortuneProperty() {
        super(KEY);
        property = this;
        FakeEnchantment.addTransformer(Enchantments.FORTUNE, (stack, level) -> (int) (getValueSafeRaw(stack) + level));
    }

    @Override
    public Double getValue(ItemStack stack) {
        return getValueRaw(stack);
    }

    @Override
    public double getValueSafe(ItemStack stack) {
        return EnchantmentHelper.getLevel(Enchantments.FORTUNE, stack);
    }
}
