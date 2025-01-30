package smartin.miapi.modules.properties.enchanment;

import com.mojang.serialization.Codec;
import dev.architectury.event.EventResult;
import net.fabricmc.api.EnvType;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
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
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;
import smartin.miapi.modules.properties.util.EitherModuleProperty;
import smartin.miapi.modules.properties.util.MergeAble;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.*;

import static smartin.miapi.modules.properties.enchanment.FakeEnchantmentManager.ADD_ENCHANTMENT;

public class FakeEitherEnchantmentProperty extends EitherModuleProperty<
        Map<ResourceLocation, DoubleOperationResolvable>,
        Map<Holder.Reference<Enchantment>, DoubleOperationResolvable>> {
    public static final ResourceLocation KEY = Miapi.id("fake_enchants");
    public static Codec<Map<ResourceLocation, DoubleOperationResolvable>> CODEC = Codec.unboundedMap(ResourceLocation.CODEC, DoubleOperationResolvable.CODEC);

    public FakeEitherEnchantmentProperty() {
        super(CODEC);
        FakeEnchantmentManager.transformerList.add((enchantmentHolder, itemStack, oldLevel) -> {
            for (Map.Entry<Holder.Reference<Enchantment>, DoubleOperationResolvable> location : getEnchants(itemStack).entrySet()) {
                if (enchantmentHolder.is(location.getKey())) {
                    DoubleOperationResolvable resolvable = location.getValue();
                    resolvable.setFunctionTransformer((s) -> s.getFirst().replace("[old_level]", String.valueOf(oldLevel)));
                    return (int) resolvable.evaluate(0.0, oldLevel);
                }
            }
            return oldLevel;
        });
        ADD_ENCHANTMENT.register(enchantmentMap -> {
            for (Map.Entry<Holder.Reference<Enchantment>, DoubleOperationResolvable> location : getEnchants(enchantmentMap.referenceStack).entrySet()) {
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

    @Override
    protected Map<Holder.Reference<Enchantment>, DoubleOperationResolvable> initializeDecode(Map<ResourceLocation, DoubleOperationResolvable> property, ModuleInstance context) {
        Map<Holder.Reference<Enchantment>, DoubleOperationResolvable> initialized = new HashMap<>();
        property.forEach((id, resolvable) -> {
            if (context.lookup != null) {
                context.lookup.lookup(Registries.ENCHANTMENT).ifPresentOrElse(enchantmentRegistryInfo -> {
                    enchantmentRegistryInfo.getter().get(ResourceKey.create(Registries.ENCHANTMENT, id)).ifPresentOrElse(holder -> {
                                initialized.put(holder, resolvable.initialize(context));
                                Miapi.LOGGER.info("full ID "+holder.key().location());
                            }, () -> Miapi.LOGGER.warn("Could not find enchanment " + id + " skiping")
                    );
                }, () -> Miapi.LOGGER.warn("Enchantment Registries not Found - could not decode enchantments"));
            } else {
                Miapi.LOGGER.warn("could not decode enchantments - missing lookup!");
            }
        });
        return initialized;
    }

    @Override
    protected Map<Holder.Reference<Enchantment>, DoubleOperationResolvable> mergeInterpreted(Map<Holder.Reference<Enchantment>, DoubleOperationResolvable> left, Map<Holder.Reference<Enchantment>, DoubleOperationResolvable> right, MergeType mergeType) {
        return MergeAble.mergeMap(left, right, mergeType);
    }

    @Override
    protected Map<ResourceLocation, DoubleOperationResolvable> mergeRaw(Map<ResourceLocation, DoubleOperationResolvable> left, Map<ResourceLocation, DoubleOperationResolvable> right, MergeType mergeType) {
        return MergeAble.mergeMap(left, right, mergeType);
    }

    @Override
    protected Map<ResourceLocation, DoubleOperationResolvable> deInitialize(Map<Holder.Reference<Enchantment>, DoubleOperationResolvable> property) {
        Map<ResourceLocation, DoubleOperationResolvable> map = new HashMap<>();
        property.forEach((id, resolvable) -> map.put(id.key().location(),resolvable));
        return map;
    }

    public Map<Holder.Reference<Enchantment>, DoubleOperationResolvable> getEnchants(ItemStack itemStack){
        if(getData(itemStack).isPresent()){
            if(getData(itemStack).get().right().isPresent()){
                return getData(itemStack).get().right().get();
            }
        }
        return Map.of();
    }

    @net.fabricmc.api.Environment(EnvType.CLIENT)
    public void setupClient() {
        StatListWidget.addStatDisplaySupplier(new StatListWidget.StatWidgetSupplier() {
            @Override
            public <T extends InteractAbleWidget & SingleStatDisplay> List<T> currentList(ItemStack original, ItemStack compareTo) {
                List<T> displays = new ArrayList<>();
                Set<Holder<Enchantment>> enchantments = new HashSet<>();
                enchantments.addAll(getEnchants(original).keySet());
                enchantments.addAll(getEnchants(compareTo).keySet());
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
}
