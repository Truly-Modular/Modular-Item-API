package smartin.miapi.modules.properties;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
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
        return EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FORTUNE, stack);
    }
}
