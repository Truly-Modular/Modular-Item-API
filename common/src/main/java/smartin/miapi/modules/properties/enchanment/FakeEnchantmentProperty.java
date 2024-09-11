package smartin.miapi.modules.properties.enchanment;

import com.mojang.serialization.Codec;
import dev.architectury.event.EventResult;
import net.fabricmc.api.EnvType;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import smartin.miapi.Environment;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.crafting.statdisplay.JsonStatDisplay;
import smartin.miapi.client.gui.crafting.statdisplay.SingleStatDisplay;
import smartin.miapi.client.gui.crafting.statdisplay.SingleStatDisplayDouble;
import smartin.miapi.client.gui.crafting.statdisplay.StatListWidget;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.*;

import static smartin.miapi.modules.properties.enchanment.FakeEnchantmentManager.ADD_ENCHANTMENT;

/**
 * This property allows the application of "fake" enchantments that simulate certain enchantment levels.
 * The enchantments are not visible in the item tooltips and can utilize `[old_level]` to reference previous enchantment levels in the Double Resolvable.
 * If the property is removed, the fake enchantments no longer apply, but they do not persist beyond their activation.
 * @header Fake Enchantment Property
 * @path /data_types/properties/enchantments/fake_enchants
 * @description_start
 * The Fake Enchantment Property simulates enchantment levels on items during specific operations.
 * These enchantments are not shown in the item's tooltip and are purely functional.
 * The property evaluates enchantment levels dynamically using the `[old_level]` placeholder, which allows referencing prior levels in the calculation.
 * Once the property is removed, the enchantments no longer affect the item.
 * @description_end
 * @data enchantment: The fake enchantment being applied (Holder<Enchantment>).
 * @data value: DoubleOperationResolvable, which defines how the level of the fake enchantment is calculated.
 */

public class FakeEnchantmentProperty extends CodecProperty<Map<Holder<Enchantment>, DoubleOperationResolvable>> {
    public static FakeEnchantmentProperty property;
    public static final ResourceLocation KEY = Miapi.id("fake_enchants");
    public static Codec<Map<Holder<Enchantment>, DoubleOperationResolvable>> CODEC = Codec.unboundedMap(Enchantment.CODEC, DoubleOperationResolvable.CODEC);

    public FakeEnchantmentProperty() {
        super(CODEC);
        property = this;
        FakeEnchantmentManager.transformerList.add((enchantmentHolder, itemStack, oldLevel) -> {
            for (Map.Entry<Holder<Enchantment>, DoubleOperationResolvable> location : getData(itemStack).orElse(new HashMap<>()).entrySet()) {
                if (enchantmentHolder.is(location.getKey())) {
                    DoubleOperationResolvable resolvable = location.getValue();
                    resolvable.setFunctionTransformer((s) -> s.getFirst().replace("[old_level]", String.valueOf(oldLevel)));
                    return (int) resolvable.evaluate(0.0, oldLevel);
                }
            }
            return oldLevel;
        });
        ADD_ENCHANTMENT.register(enchantmentMap -> {
            for (Map.Entry<Holder<Enchantment>, DoubleOperationResolvable> location : getData(enchantmentMap.referenceStack).orElse(new HashMap<>()).entrySet()) {
                if (!enchantmentMap.enchantments.contains(location.getKey())) {
                    enchantmentMap.enchantments.add(location.getKey());
                }
            }
            return EventResult.pass();
        });
        if (Environment.isClient()) {
            setupClient();
        }
    }

    @net.fabricmc.api.Environment(EnvType.CLIENT)
    public void setupClient() {
        StatListWidget.addStatDisplaySupplier(new StatListWidget.StatWidgetSupplier() {
            @Override
            public <T extends InteractAbleWidget & SingleStatDisplay> List<T> currentList(ItemStack original, ItemStack compareTo) {
                List<T> displays = new ArrayList<>();
                Set<Holder<Enchantment>> enchantments = new HashSet<>();
                enchantments.addAll(getData(original).orElse(new HashMap<>()).keySet());
                enchantments.addAll(getData(compareTo).orElse(new HashMap<>()).keySet());
                enchantments.forEach(enchantment -> {
                    JsonStatDisplay display = new JsonStatDisplay((stack) -> enchantment.value().description(),
                            (stack) -> enchantment.value().description(),
                            new SingleStatDisplayDouble.StatReaderHelper() {
                                @Override
                                public double getValue(ItemStack itemStack) {
                                    return EnchantmentHelper.getItemEnchantmentLevel(enchantment, itemStack);
                                }

                                @Override
                                public boolean hasValue(ItemStack itemStack) {
                                    return true;
                                }
                            },
                            0,
                            enchantment.value().getMaxLevel());
                    if (enchantment.is(EnchantmentTags.CURSE)) {
                        display.inverse = true;
                    }
                    displays.add((T) display);
                });
                return displays;
            }
        });
    }

    @Override
    public Map<Holder<Enchantment>, DoubleOperationResolvable> merge(Map<Holder<Enchantment>, DoubleOperationResolvable> left, Map<Holder<Enchantment>, DoubleOperationResolvable> right, MergeType mergeType) {
        return ModuleProperty.mergeMap(left, right, mergeType);
    }
}