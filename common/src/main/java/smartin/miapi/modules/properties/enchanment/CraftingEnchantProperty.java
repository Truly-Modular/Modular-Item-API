package smartin.miapi.modules.properties.enchanment;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import smartin.miapi.Miapi;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.util.*;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

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

public class CraftingEnchantProperty extends CodecProperty<Map<ResourceLocation, DoubleOperationResolvable>> implements ComponentApplyProperty {
    public static final ResourceLocation KEY = Miapi.id("crafting_enchants");
    public static CraftingEnchantProperty property;
    public static Codec<Map<ResourceLocation, DoubleOperationResolvable>> CODEC = Codec.unboundedMap(ResourceLocation.CODEC, DoubleOperationResolvable.CODEC);

    public CraftingEnchantProperty() {
        super(CODEC);
        property = this;
    }

    @Override
    public void updateComponent(ItemStack itemStack, RegistryAccess registryAccess) {
        getData(itemStack).ifPresent(stringDoubleOperationResolvableMap -> {
            EnchantmentHelper.updateEnchantments(itemStack, (mutable -> {
                stringDoubleOperationResolvableMap.forEach((enchantmentID, value) -> {
                    try {
                        tryAndLookUp(enchantmentID, ItemModule.getModules(itemStack)).ifPresent(enchantment -> {
                            int prevLevel = mutable.getLevel(enchantment);
                            value.setFunctionTransformer((s) -> s.getFirst().replace("[old_level]", String.valueOf(prevLevel)));
                            int nextLevel = (int) value.evaluate(0.0, prevLevel);
                            if (MiapiConfig.getServerConfig().other.verboseLogging) {
                                Miapi.LOGGER.info("updated level to " + enchantment.value().description() + " " + nextLevel);
                            }
                            mutable.set(enchantment, nextLevel);
                        });
                    } catch (RuntimeException e) {
                        Miapi.LOGGER.info("failed to apply enchantments!", e);
                    }
                });
            }));
        });
    }

    public static Map<Holder<Enchantment>, DoubleOperationResolvable> tryConvert(Map<ResourceLocation, DoubleOperationResolvable> original, ItemStack itemStack) {
        Map<Holder<Enchantment>, DoubleOperationResolvable> mapped = new HashMap<>();
        original.forEach((id, ench) -> {
            tryAndLookUp(id, itemStack).ifPresent(holder -> {
                mapped.put(holder, ench);
            });
        });
        return mapped;
    }

    public static Optional<Holder<Enchantment>> tryAndLookUp(ResourceLocation id, ItemStack reference) {
        return tryAndLookUp(id, ItemModule.getModules(reference));
    }

    public static Optional<Holder<Enchantment>> tryAndLookUp(ResourceLocation id, ModuleInstance reference) {
        if (reference.registryAccess == null || reference.lookup == null) {
            return Optional.empty();
        }
        var registry = reference.registryAccess.registry(Registries.ENCHANTMENT).get();
        ResourceKey<Enchantment> enchantmentResourceKey = ResourceKey.create(Registries.ENCHANTMENT, id);
        var lookup = reference.lookup.lookup(Registries.ENCHANTMENT);
        if (lookup.isEmpty()) {
            Miapi.LOGGER.info("Enchantment Registry could not be found!");
            return Optional.empty();
        }
        try {
            var optional = lookup.get().getter().get(enchantmentResourceKey);
            if (optional.isEmpty()) {
                Miapi.LOGGER.info("could not find enchantment " + id);
                return Optional.empty();
            }
            return Optional.of(lookup.get().getter().get(enchantmentResourceKey).get());
        } catch (RuntimeException e) {
            Miapi.LOGGER.warn("could not properly lookup enchantments!", e);
        }
        return Optional.empty();
    }

    public Map<ResourceLocation, DoubleOperationResolvable> initialize(Map<ResourceLocation, DoubleOperationResolvable> property, ModuleInstance context) {
        Map<ResourceLocation, DoubleOperationResolvable> init = new LinkedHashMap<>();
        property.forEach((key, value) -> init.put(key, value.initialize(context)));
        return init;
    }

    @Override
    public Map<ResourceLocation, DoubleOperationResolvable> merge(Map<ResourceLocation, DoubleOperationResolvable> left, Map<ResourceLocation, DoubleOperationResolvable> right, MergeType mergeType) {
        return MergeAble.mergeMap(left, right, mergeType);
    }
}
