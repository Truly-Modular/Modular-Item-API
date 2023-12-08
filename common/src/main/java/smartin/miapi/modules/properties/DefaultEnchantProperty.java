package smartin.miapi.modules.properties;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.blocks.ModularWorkBenchEntity;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.properties.util.CraftingProperty;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DefaultEnchantProperty implements ModuleProperty,CraftingProperty {
    public static String KEY = "default_enchants";
    public static DefaultEnchantProperty property;
    private static Type type = new TypeToken<Map<String, Integer>>() {
    }.getType();

    public DefaultEnchantProperty() {
        ModularItemCache.setSupplier(KEY, DefaultEnchantProperty::getEnchantsCache);
        property = this;
    }

    private static List<Pair<Enchantment, Integer>> getEnchantsCache(ItemStack itemStack) {
        List<Pair<Enchantment, Integer>> enchants = new ArrayList<>();
        JsonElement list = ItemModule.getMergedProperty(itemStack, property, MergeType.SMART);
        ItemModule.getMergedProperty(ItemModule.getModules(itemStack),property);
        if (list != null) {
            Miapi.DEBUG_LOGGER.info("FOUND JSON " + list);
        }
        Map<String, Integer> map = Miapi.gson.fromJson(list, type);
        if (map != null) {
            map.forEach((id, level) -> {
                Miapi.DEBUG_LOGGER.info("found enchant " + id);
                Enchantment enchantment = Registries.ENCHANTMENT.get(new Identifier(id));
                if (enchantment != null && enchantment.isAcceptableItem(itemStack)) {
                    enchants.add(new Pair<>(enchantment, level));
                }
            });
        }
        return enchants;
    }

    public JsonElement merge(JsonElement old, JsonElement toMerge, MergeType type) {
        Miapi.DEBUG_LOGGER.info("Merging Enchants ");
        Type typeToken = new com.google.gson.reflect.TypeToken<List<JsonElement>>() {
        }.getType();
        if (old.isJsonArray() && toMerge.isJsonArray()) {
            List<JsonElement> oldList = Miapi.gson.fromJson(old, typeToken);
            List<JsonElement> newList = Miapi.gson.fromJson(toMerge, typeToken);
            if (Objects.requireNonNull(type) == MergeType.SMART || type == MergeType.EXTEND) {
                oldList.addAll(newList);
                return Miapi.gson.toJsonTree(oldList, typeToken);
            } else if (type == MergeType.OVERWRITE) {
                return toMerge;
            }
        } else {
            if (MergeType.EXTEND == type) {
                return old;
            } else {
                return toMerge;
            }
        }
        return old;
    }

    public static List<Pair<Enchantment, Integer>> getEnchants(ItemStack itemStack) {
        return ModularItemCache.get(itemStack, KEY, new ArrayList<>());
    }

    public static NbtList enchants(ItemStack itemStack, NbtList nbtList) {
        NbtList nbtElements = nbtList == null ? new NbtList() : nbtList.copy();
        getEnchants(itemStack).forEach((enchantmentIntegerPair -> {
            if (enchantmentIntegerPair.getFirst().isAcceptableItem(itemStack)) {
                Enchantment enchantment = enchantmentIntegerPair.getFirst();
                int level = enchantmentIntegerPair.getSecond();
                nbtElements.add(EnchantmentHelper.createNbt(EnchantmentHelper.getEnchantmentId(enchantment), (byte) level));
            }
        }));
        if(nbtElements.isEmpty() && nbtList==null){
            return null;
        }
        return nbtElements;
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        Miapi.DEBUG_LOGGER.info("LOAD " + moduleKey);
        Miapi.DEBUG_LOGGER.info(String.valueOf(data));
        return true;
    }

    @Override
    public ItemStack preview(ItemStack old, ItemStack crafting, PlayerEntity player, ModularWorkBenchEntity bench, ItemModule.@Nullable ModuleInstance newModule, ItemModule module, List<ItemStack> inventory, Map<String, String> data) {
        getEnchantsCache(crafting).forEach((enchantmentIntegerPair -> {
            Enchantment enchantment = enchantmentIntegerPair.getFirst();
            int level = enchantmentIntegerPair.getSecond();
            if(enchantment.isAcceptableItem(crafting)){
                int prevLevel = EnchantmentHelper.getLevel(enchantment,crafting);
                if(level>prevLevel){
                    crafting.addEnchantment(enchantmentIntegerPair.getFirst(),enchantmentIntegerPair.getSecond());
                }
                else{
                    Miapi.DEBUG_LOGGER.info("to low level");
                }
            }
            else{
                Miapi.DEBUG_LOGGER.info("not accept Item");
            }
        }));
        return crafting;
    }
}
