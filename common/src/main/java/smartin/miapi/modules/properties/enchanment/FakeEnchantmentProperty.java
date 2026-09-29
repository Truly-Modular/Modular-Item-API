package smartin.miapi.modules.properties.enchanment;

import com.mojang.serialization.Codec;
import dev.architectury.event.EventResult;
import net.fabricmc.api.EnvType;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
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
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;
import smartin.miapi.modules.properties.util.MergeAble;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.*;

import static smartin.miapi.modules.properties.enchanment.FakeEnchantmentManager.ADD_ENCHANTMENT;


public class FakeEnchantmentProperty extends CodecProperty<Map<ResourceLocation, DoubleOperationResolvable>> {
    public static FakeEnchantmentProperty property;
    public static final ResourceLocation KEY = Miapi.id("fake_enchants_old");
    public static Codec<Map<ResourceLocation, DoubleOperationResolvable>> CODEC = Codec.unboundedMap(ResourceLocation.CODEC, DoubleOperationResolvable.CODEC);

    public FakeEnchantmentProperty() {
        super(CODEC);
        property = this;
        FakeEnchantmentManager.transformerList.add((enchantmentHolder, itemStack, oldLevel) -> {
            for (Map.Entry<ResourceLocation, DoubleOperationResolvable> location : getData(itemStack).orElse(new HashMap<>()).entrySet()) {
                if (enchantmentHolder.is(location.getKey())) {
                    DoubleOperationResolvable resolvable = location.getValue();
                    resolvable.setFunctionTransformer((s) -> s.getFirst().replace("[old_level]", String.valueOf(oldLevel)));
                    return (int) resolvable.evaluate(0.0, oldLevel);
                }
            }
            return oldLevel;
        });
        ADD_ENCHANTMENT.register(enchantmentMap -> {
            for (Map.Entry<ResourceLocation, DoubleOperationResolvable> location : getData(enchantmentMap.referenceStack).orElse(new HashMap<>()).entrySet()) {
                CraftingEnchantProperty.tryAndLookUp(location.getKey(), null, enchantmentMap.referenceStack).ifPresent(enchantment -> {
                    if (!enchantmentMap.enchantments.contains(enchantment)) {
                        enchantmentMap.enchantments.add(enchantment);
                    }
                });
            }
            return EventResult.pass();
        });
        if (Environment.isClient()) {
            setupClient();
        }
    }

    public Map<Holder<Enchantment>, DoubleOperationResolvable> initialize(Map<Holder<Enchantment>, DoubleOperationResolvable> property, ModuleInstance context) {
        Map<Holder<Enchantment>, DoubleOperationResolvable> init = new LinkedHashMap<>();
        property.forEach((key, value) -> {
            value.setFunctionTransformer((stringModuleInstancePair -> stringModuleInstancePair.getFirst().replace("[old_level]", "0")));
            init.put(key, value.initialize(context));
        });
        return init;
    }

    @net.fabricmc.api.Environment(EnvType.CLIENT)
    public void setupClient() {
        StatListWidget.addStatDisplaySupplier(new StatListWidget.StatWidgetSupplier() {
            @Override
            public <T extends InteractAbleWidget & SingleStatDisplay> List<T> currentList(ItemStack original, ItemStack compareTo) {
                List<T> displays = new ArrayList<>();
                Set<Holder<Enchantment>> enchantments = new HashSet<>();
                enchantments.addAll(CraftingEnchantProperty.tryConvert(getData(original).orElse(new HashMap<>()), original).keySet());
                enchantments.addAll(CraftingEnchantProperty.tryConvert(getData(compareTo).orElse(new HashMap<>()), compareTo).keySet());
                enchantments.forEach(enchantment -> {
                    JsonStatDisplay display = new JsonStatDisplay(
                            (stack) -> enchantment.value().description(),
                            (stack) -> getDescription(enchantment.value().description(), enchantment),
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

    public static Component getDescription(Component component, Holder<Enchantment> enchantment) {
        return enchantment.unwrapKey()
                .<Component>map(key -> Component.translatableWithFallback(
                        "enchantment." + key.location().getNamespace() + "." + key.location().getPath() + ".description",
                        component.getString()))
                .orElseGet(() -> {
                    if (component.getContents() instanceof TranslatableContents translatable) {
                        return Component.translatableWithFallback(
                                translatable.getKey() + ".description",
                                component.getString());
                    }
                    return component;
                });
    }

    @Override
    public Map<ResourceLocation, DoubleOperationResolvable> merge(Map<ResourceLocation, DoubleOperationResolvable> left, Map<ResourceLocation, DoubleOperationResolvable> right, MergeType mergeType) {
        return MergeAble.mergeMap(left, right, mergeType);
    }
}