package smartin.miapi.modules.properties.enchanment;

import com.mojang.serialization.Codec;
import dev.architectury.event.EventResult;
import net.fabricmc.api.EnvType;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import smartin.miapi.Environment;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.crafting.statdisplay.DoubleResolvableStatDisplay;
import smartin.miapi.client.gui.crafting.statdisplay.SingleStatDisplay;
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
                    oldLevel = (int) resolvable.evaluate(oldLevel, oldLevel);
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
        property.forEach((id, resolvable) -> getWithRegistry(id, resolvable, initialized, context));
        return initialized;
    }

    private static void getWithRegistry(ResourceLocation id, DoubleOperationResolvable resolvable, Map<Holder.Reference<Enchantment>, DoubleOperationResolvable> initialized, ModuleInstance context) {
        context.getter().lookup(Registries.ENCHANTMENT).ifPresentOrElse(enchantmentRegistryInfo -> {
            enchantmentRegistryInfo.getter().get(ResourceKey.create(Registries.ENCHANTMENT, id)).ifPresentOrElse(holder -> {
                        resolvable.setFunctionTransformer((s) -> s.getFirst().replace("[old_level]", "0"));
                        initialized.put(holder, resolvable.initialize(context));
                    }, () -> Miapi.LOGGER.warn("Could not find enchantment " + id + " skiping")
            );
        }, () -> Miapi.LOGGER.warn("Enchantment Registries not Found - could not decode enchantments"));
    }


    @Override
    protected Map<Holder.Reference<Enchantment>, DoubleOperationResolvable> mergeInterpreted(Map<Holder.Reference<Enchantment>, DoubleOperationResolvable> left, Map<Holder.Reference<Enchantment>, DoubleOperationResolvable> right, MergeType mergeType) {
        return MergeAble.mergeMap(left, right, mergeType, (e, l, r) -> DoubleOperationResolvable.merge(l, r, mergeType));
    }

    @Override
    protected Map<ResourceLocation, DoubleOperationResolvable> mergeRaw(Map<ResourceLocation, DoubleOperationResolvable> left, Map<ResourceLocation, DoubleOperationResolvable> right, MergeType mergeType) {
        return MergeAble.mergeMap(left, right, mergeType, (e, l, r) -> DoubleOperationResolvable.merge(l, r, mergeType));
    }

    @Override
    protected Map<ResourceLocation, DoubleOperationResolvable> deInitialize(Map<Holder.Reference<Enchantment>, DoubleOperationResolvable> property) {
        Map<ResourceLocation, DoubleOperationResolvable> map = new HashMap<>();
        property.forEach((id, resolvable) -> map.put(id.key().location(), resolvable));
        return map;
    }

    public Map<Holder.Reference<Enchantment>, DoubleOperationResolvable> getEnchants(ItemStack itemStack) {
        return getData(itemStack).flatMap(c -> c.right()).orElse(Map.of());
    }

    @net.fabricmc.api.Environment(EnvType.CLIENT)
    @SuppressWarnings("unchecked")
    public void setupClient() {
        StatListWidget.addStatDisplaySupplier(new StatListWidget.StatWidgetSupplier() {
            @Override
            public <T extends InteractAbleWidget & SingleStatDisplay> List<T> currentList(ItemStack original, ItemStack compareTo) {
                List<T> displays = new ArrayList<>();
                Map<Holder.Reference<Enchantment>, DoubleOperationResolvable> enchantments = new HashMap<>();
                enchantments.putAll(getEnchants(original));
                enchantments.putAll(getEnchants(compareTo));
                enchantments.forEach((enchantment, data) -> {
                    if (Miapi.clientRegistryAccess != null && enchantment.canSerializeIn(Miapi.clientRegistryAccess.lookupOrThrow(Registries.ENCHANTMENT))) {
                        Component desc = Component
                                .translatableWithFallback(
                                        "enchantment." + enchantment.key().location().getNamespace() + "." + enchantment.key().location().getPath() + ".desc",
                                        "");
                        displays.add((T) DoubleResolvableStatDisplay.builder(
                                        (s) -> Optional.ofNullable(getEnchants(s).get(enchantment)))
                                .setHoverDescription(Component.translatable("miapi.fake_enchant.desc", enchantment.value().description(), desc))
                                .setName(enchantment.value().description())
                                .setMax(enchantment.value().getMaxLevel())
                                .setFormat("#")
                                .setInverse(enchantment.is(EnchantmentTags.CURSE))
                                .setMin(0).build());
                    }
                });
                return displays;
            }
        });
    }
}
