package smartin.miapi.modules.properties.enchanment;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import smartin.miapi.modules.properties.util.*;

import java.util.Map;

public class CraftingEnchantProperty extends CodecBasedProperty<Map<Holder<Enchantment>, DoubleOperationResolvable>> implements ComponentApplyProperty {
    public static final String KEY = "crafting_enchants";
    public static CraftingEnchantProperty property;
    public static Codec<Map<Holder<Enchantment>, DoubleOperationResolvable>> CODEC = Codec.unboundedMap(Enchantment.CODEC, DoubleOperationResolvable.CODEC);

    public CraftingEnchantProperty() {
        super(CODEC);
        property = this;
    }

    @Override
    public void updateComponent(ItemStack itemStack, RegistryAccess registryAccess) {
        getData(itemStack).ifPresent(stringDoubleOperationResolvableMap -> {
            EnchantmentHelper.updateEnchantments(itemStack, (mutable -> {
                stringDoubleOperationResolvableMap.forEach((enchantment, value) -> {
                    int prevLevel = mutable.getLevel(enchantment);
                    value.setFunctionTransformer((s) -> s.getFirst().replace("[old_level]", String.valueOf(prevLevel)));
                    mutable.set(enchantment, (int) value.evaluate(0.0, prevLevel));
                });
            }));
        });
    }

    @Override
    public Map<Holder<Enchantment>, DoubleOperationResolvable> merge(Map<Holder<Enchantment>, DoubleOperationResolvable> left, Map<Holder<Enchantment>, DoubleOperationResolvable> right, MergeType mergeType) {
        return ModuleProperty.mergeMap(left, right, mergeType);
    }
}
