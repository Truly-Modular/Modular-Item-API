package smartin.miapi.modules.properties;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import smartin.miapi.item.FakeEnchantment;
import smartin.miapi.modules.properties.util.SimpleDoubleProperty;

public class MendingProperty extends SimpleDoubleProperty {
    public static final String KEY = "mending";
    public static MendingProperty property;


    public MendingProperty() {
        super(KEY);
        property = this;
        FakeEnchantment.addTransformer(Enchantments.MENDING, (stack, level) -> (int) (getValueSafeRaw(stack) + level));
    }

    @Override
    public Double getValue(ItemStack stack) {
        return getValueRaw(stack);
    }

    @Override
    public double getValueSafe(ItemStack stack) {
        return EnchantmentHelper.getLevel(Enchantments.MENDING, stack);
    }
}
