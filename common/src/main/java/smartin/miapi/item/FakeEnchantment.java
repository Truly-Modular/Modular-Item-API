package smartin.miapi.item;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FakeEnchantment {

    public static Map<Enchantment, List<SpecificEnchantmentTransformer>> enchantmentListMap = new ConcurrentHashMap<>();

    public static List<EnchantmentTransformer> enchantmentTransformers = new ArrayList<>();

    public static int getFakeLevel(Enchantment enchantment, ItemStack stack, int level) {
        if (enchantmentListMap.get(enchantment) != null) {
            for (SpecificEnchantmentTransformer transformer : enchantmentListMap.get(enchantment)) {
                level = transformer.level(stack, level);
            }
        }
        for(EnchantmentTransformer transformer:enchantmentTransformers){
            level = transformer.level(enchantment,stack,level);
        }
        return level;
    }

    public static void addTransformer(Enchantment enchantment, SpecificEnchantmentTransformer transformer) {
        List<SpecificEnchantmentTransformer> transformers = enchantmentListMap.getOrDefault(enchantment, new ArrayList<>());
        transformers.add(transformer);
        enchantmentListMap.put(enchantment, transformers);
    }

    public interface EnchantmentTransformer {
        int level(Enchantment enchantment, ItemStack stack, int level);
    }

    public interface SpecificEnchantmentTransformer {
        int level(ItemStack stack, int level);
    }
}
