package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import dev.architectury.utils.Env;
import net.fabricmc.api.EnvType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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
import smartin.miapi.item.FakeEnchantment;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FakeEnchantmentProperty implements ModuleProperty {
    public static FakeEnchantmentProperty property;
    public static final String KEY = "fake_enchant";
    private static final Type type = new TypeToken<Map<String, Integer>>() {
    }.getType();

    public FakeEnchantmentProperty() {
        property = this;
        ModularItemCache.setSupplier(KEY, FakeEnchantmentProperty::getEnchantsCache);
        FakeEnchantment.enchantmentTransformers.add((enchantment, stack, level) -> {
            if (getEnchants(stack).containsKey(enchantment)) {
                return Math.max(getEnchants(stack).get(enchantment), level);
            }
            return level;
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

                Map<Enchantment, Integer> enchantments = new HashMap<>(getEnchants(original));
                getEnchants(original).forEach((enchantment, integer) -> {
                    if (enchantments.containsKey(enchantment)) {
                        enchantments.put(enchantment, Math.max(integer, enchantments.get(enchantment)));
                    }
                    enchantments.put(enchantment, integer);
                });
                enchantments.keySet().forEach(enchantment -> {
                    JsonStatDisplay display = new JsonStatDisplay((stack) -> Component.translatable(enchantment.getTranslationKey()),
                            (stack) -> Component.translatable(enchantment.getTranslationKey()),
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
                            enchantment.getMaxLevel());
                    if(enchantment.isCursed()){
                        display.inverse = true;
                    }
                    displays.add((T) display);
                });
                return displays;
            }
        });
    }

    private static Map<Enchantment, Integer> getEnchantsCache(ItemStack itemStack) {
        Map<Enchantment, Integer> enchants = new HashMap<>();

        JsonElement list = ItemModule.getMergedProperty(itemStack, property, MergeType.SMART);
        ItemModule.getMergedProperty(ItemModule.getModules(itemStack), property);
        Map<String, Integer> map = Miapi.gson.fromJson(list, type);
        if (map != null) {
            map.forEach((id, level) -> {
                Enchantment enchantment = Registries.ENCHANTMENT.get(new ResourceLocation(id));
                if (enchantment != null && enchantment.canEnchant(itemStack)) {
                    enchants.put(enchantment, level);
                }
            });
        }
        return enchants;
    }

    public static Map<Enchantment, Integer> getEnchants(ItemStack itemStack) {
        return ModularItemCache.get(itemStack, KEY, new HashMap<>());
    }

    public JsonElement merge(JsonElement old, JsonElement toMerge, MergeType mergeType) {
        if (old != null && toMerge != null) {
            Map<String, Integer> mapOld = Miapi.gson.fromJson(old, type);
            Map<String, Integer> mapToMerge = Miapi.gson.fromJson(toMerge, type);
            if (mergeType.equals(MergeType.OVERWRITE)) {
                return toMerge;
            }
            mapOld.forEach((key, level) -> {
                if (mapToMerge.containsKey(key)) {
                    mapToMerge.put(key, Math.max(mapOld.get(key), mapToMerge.get(key)));
                } else {
                    mapToMerge.put(key, level);
                }
            });
            return Miapi.gson.toJsonTree(mapToMerge);
        }
        if (old == null && toMerge != null) {
            return toMerge;
        }
        return old;
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        Miapi.gson.fromJson(data, type);
        return true;
    }
}
