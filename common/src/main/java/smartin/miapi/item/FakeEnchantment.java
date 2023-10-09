package smartin.miapi.item;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FakeEnchantment {

    public static Map<Enchantment, List<EnchantmentTransformer>> enchantmentListMap = new HashMap<>();

    public static int getFakeLevel(Enchantment enchantment, ItemStack stack, int level) {
        if (enchantmentListMap.get(enchantment) != null) {
            for (EnchantmentTransformer transformer : enchantmentListMap.get(enchantment)) {
                level = transformer.level(stack, level);
            }
        }
        return level;
    }

    public static void addTransformer(Enchantment enchantment, EnchantmentTransformer transformer) {
        List<EnchantmentTransformer> transformers = enchantmentListMap.getOrDefault(enchantment, new ArrayList<>());
        transformers.add(transformer);
        enchantmentListMap.put(enchantment, transformers);
    }

    public interface EnchantmentTransformer {
        int level(ItemStack stack, int level);
    }
}
