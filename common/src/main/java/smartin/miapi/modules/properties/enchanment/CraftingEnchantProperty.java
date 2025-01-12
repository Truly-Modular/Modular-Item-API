package smartin.miapi.modules.properties.enchanment;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.util.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This property allows modules to apply enchantments during crafting, which will persist on the item.
 *
 * @header Crafting Enchantments Property
 * @description_start The Crafting Enchantments Property adds specific enchantments to an item when it is crafted.
 * These enchantments are permanently added to the item and remain during subsequent uses or modifications.
 * The property dynamically calculates the level of the enchantment using a resolvable operation that can reference the previous level of the enchantment.
 * It integrates with the core enchantment system and ensures compatibility with existing enchantment handling mechanisms.
 * @path /data_types/properties/enchantments/crafting_enchants
 * @data enchantment: The enchantment being applied.
 * @data value: Double Resolvable, used to calculate the level of the enchantment.
 */

public class CraftingEnchantProperty extends CodecProperty<Map<Holder<Enchantment>, DoubleOperationResolvable>> implements ComponentApplyProperty {
    public static final ResourceLocation KEY = Miapi.id("crafting_enchants");
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
                    int nextLevel = (int) value.evaluate(0.0, prevLevel);
                    Miapi.LOGGER.info("updated level to " + enchantment.value().description() + " " + nextLevel);
                    mutable.set(enchantment, nextLevel);
                });
            }));
        });
    }

    public Map<Holder<Enchantment>, DoubleOperationResolvable> initialize(Map<Holder<Enchantment>, DoubleOperationResolvable> property, ModuleInstance context) {
        Map<Holder<Enchantment>, DoubleOperationResolvable> init = new LinkedHashMap<>();
        property.forEach((key, value) -> init.put(key, value.initialize(context)));
        return init;
    }

    @Override
    public Map<Holder<Enchantment>, DoubleOperationResolvable> merge(Map<Holder<Enchantment>, DoubleOperationResolvable> left, Map<Holder<Enchantment>, DoubleOperationResolvable> right, MergeType mergeType) {
        return MergeAble.mergeMap(left, right, mergeType);
    }
}
