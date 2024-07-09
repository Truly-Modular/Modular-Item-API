package smartin.miapi.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FakeEnchantment {

    public static Map<Enchantment, List<SpecificEnchantmentTransformer>> enchantmentListMap = new ConcurrentHashMap<>();

    public static List<EnchantmentTransformer> enchantmentTransformers = new ArrayList<>();

    public static List<EnchantmentAdder> adder = new ArrayList<>();

    public static int getFakeLevel(Enchantment enchantment, ItemStack stack, int level) {
        if (enchantmentListMap.get(enchantment) != null) {
            for (SpecificEnchantmentTransformer transformer : enchantmentListMap.get(enchantment)) {
                level = transformer.level(stack, level);
            }
        }
        for (EnchantmentTransformer transformer : enchantmentTransformers) {
            level = transformer.level(enchantment, stack, level);
        }
        return level;
    }

    public static void addTransformer(Enchantment enchantment, SpecificEnchantmentTransformer transformer) {
        List<SpecificEnchantmentTransformer> transformers = enchantmentListMap.getOrDefault(enchantment, new ArrayList<>());
        transformers.add(transformer);
        enchantmentListMap.put(enchantment, transformers);
    }

    /*
    //TODO:rewo
    public static void addEnchantments(EnchantmentHelper.EnchantmentVisitor consumer, ItemStack stack) {
        Set<Enchantment> enchantments = new HashSet<>();
        adder.forEach(enchantmentAdder -> {
            enchantments.addAll(enchantmentAdder.getEnchantments(stack));
        });
        enchantments.forEach(enchantment -> {
            consumer.accept(enchantment, getFakeLevel(enchantment, stack, 0));
        });
    }

     */

    public interface EnchantmentTransformer {
        int level(Enchantment enchantment, ItemStack stack, int level);
    }

    public interface SpecificEnchantmentTransformer {
        int level(ItemStack stack, int level);
    }

    public interface EnchantmentAdder {
        List<Enchantment> getEnchantments(ItemStack stack);
    }
}
