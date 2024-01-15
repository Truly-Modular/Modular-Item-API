package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import smartin.miapi.Miapi;
import smartin.miapi.item.FakeEnchantment;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class FakeEnchantmentProperty implements ModuleProperty {
    public static FakeEnchantmentProperty property;
    public static final String KEY = "fake_enchant";
    private static final Type type = new TypeToken<Map<String, Integer>>() {
    }.getType();

    public FakeEnchantmentProperty() {
        property = this;
        FakeEnchantment.enchantmentTransformers.add((enchantment, stack, level) -> {
            if (getEnchants(stack).containsKey(enchantment)) {
                return Math.max(getEnchants(stack).get(enchantment), level);
            }
            return level;
        });
        ModularItemCache.setSupplier(KEY, FakeEnchantmentProperty::getEnchantsCache);
    }

    private static Map<Enchantment, Integer> getEnchantsCache(ItemStack itemStack) {
        Map<Enchantment, Integer> enchants = new HashMap<>();

        JsonElement list = ItemModule.getMergedProperty(itemStack, property, MergeType.SMART);
        ItemModule.getMergedProperty(ItemModule.getModules(itemStack), property);
        Map<String, Integer> map = Miapi.gson.fromJson(list, type);
        if (map != null) {
            map.forEach((id, level) -> {
                Enchantment enchantment = Registries.ENCHANTMENT.get(new Identifier(id));
                if (enchantment != null && enchantment.isAcceptableItem(itemStack)) {
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
