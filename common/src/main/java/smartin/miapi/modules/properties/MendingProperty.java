package smartin.miapi.modules.properties;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import smartin.miapi.item.FakeEnchantment;
import smartin.miapi.modules.properties.util.DoubleProperty;

/**
 * Applies fake mending to the Item
 */
public class MendingProperty extends DoubleProperty {
    //TODO:delete, fake enchants does the same thing better
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
        return EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MENDING, stack);
    }
}
