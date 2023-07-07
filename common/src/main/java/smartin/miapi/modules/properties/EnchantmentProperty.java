package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import dev.architectury.platform.Platform;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.properties.util.CraftingProperty;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.*;

/**
 * This property allows to set allowed enchantments
 */
public class EnchantmentProperty implements CraftingProperty, ModuleProperty {
    public static String KEY = "enchantments";
    public static EnchantmentProperty property;
    public static HashMap<String, List<String>> replaceMap = new HashMap<>();

    public EnchantmentProperty() {
        property = this;
        addToReplaceMap("miapi:basic", "minecraft:mending");
        addToReplaceMap("miapi:basic", "minecraft:unbreaking");
        addToReplaceMap("miapi:basic", "minecraft:vanishing_curse");
        addToReplaceMap("miapi:weapon", "minecraft:fire_aspect");
        addToReplaceMap("miapi:weapon", "minecraft:looting");
        addToReplaceMap("miapi:weapon", "minecraft:knockback");
        addToReplaceMap("miapi:weapon", "minecraft:smite");
        addToReplaceMap("miapi:weapon", "minecraft:bane_of_arthropods");
        addToReplaceMap("miapi:edged", "minecraft:sharpness");
        addToReplaceMap("miapi:edged", "minecraft:sweeping");
        addToReplaceMap("miapi:tool", "minecraft:efficiency");
        addToReplaceMap("miapi:tool", "minecraft:fortune");
        addToReplaceMap("miapi:tool", "minecraft:silk_touch");
        addToReplaceMap("miapi:bow", "minecraft:power");
        addToReplaceMap("miapi:bow", "minecraft:infinity");
        addToReplaceMap("miapi:bow", "minecraft:punch");
        addToReplaceMap("miapi:bow", "minecraft:flame");
        addToReplaceMap("miapi:crossbow", "minecraft:quick_charge");
        addToReplaceMap("miapi:crossbow", "minecraft:piercing");
        addToReplaceMap("miapi:crossbow", "minecraft:multishot");
        addToReplaceMap("miapi:armor", "minecraft:protection");
        addToReplaceMap("miapi:armor", "minecraft:blast_protection");
        addToReplaceMap("miapi:armor", "minecraft:projectile_protection");
        addToReplaceMap("miapi:armor", "minecraft:fire_protection");
        addToReplaceMap("miapi:armor", "minecraft:thorns");
        addToReplaceMap("miapi:armor", "minecraft:binding_curse");
        addToReplaceMap("miapi:helmet", "minecraft:aqua_affinity");
        addToReplaceMap("miapi:helmet", "minecraft:respiration");
        addToReplaceMap("miapi:boots", "minecraft:feather_falling");
        addToReplaceMap("miapi:boots", "minecraft:depth_strider");
        addToReplaceMap("miapi:boots", "minecraft:frost_walking");
        addToReplaceMap("miapi:boots", "minecraft:soul_speed");
        addToReplaceMap("miapi:leggings", "minecraft:swift_sneak");
        addToReplaceMap("miapi:trident", "minecraft:riptide");
        addToReplaceMap("miapi:trident", "minecraft:loyalty");
        addToReplaceMap("miapi:trident", "minecraft:channeling");
        addToReplaceMap("miapi:trident", "minecraft:impaling");

        ModularItemCache.setSupplier(KEY, this::createAllowedList);
    }

    private List<Enchantment> createAllowedList(ItemStack itemStack) {
        JsonElement element = ItemModule.getMergedProperty(itemStack, property);
        if (element != null) {
            EnchantmentPropertyJson json = Miapi.gson.fromJson(element, EnchantmentPropertyJson.class);
            if (json.allowed == null) json.allowed = new ArrayList<>();
            if (json.forbidden == null) json.forbidden = new ArrayList<>();
            return convert(json.allowed);
        } else {
            return new ArrayList<>();
        }
    }

    public static List<Enchantment> getAllowedList(ItemStack stack) {
        return Collections.unmodifiableList((List<Enchantment>) ModularItemCache.get(stack, KEY));
    }

    public static void addToReplaceMap(String key, String enchant) {
        List<String> list = replaceMap.getOrDefault(key, new ArrayList<>());
        list.add(enchant);
        replaceMap.put(key, list);
    }

    public static boolean isAllowed(ItemStack stack, Enchantment enchantment) {
        boolean allowed = getAllowedList(stack).contains(enchantment);
        getAllowedList(stack).forEach(enchantment1 -> {
            Miapi.LOGGER.error(String.valueOf(enchantment1));
        });
        return allowed;
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        EnchantmentPropertyJson json = Miapi.gson.fromJson(data, EnchantmentPropertyJson.class);
        convert(json.allowed);
        convert(json.forbidden);
        return true;
    }

    public static List<Enchantment> convert(List<String> list) {
        List<String> replaceList = new ArrayList<>();
        for (String id : list) {
            if (replaceMap.containsKey(id)) {
                replaceList.addAll(replaceMap.get(id));
            } else {
                replaceList.add(id);
            }
        }
        List<Enchantment> enchantments = new ArrayList<>();
        for (String id : replaceList) {
            if (id.contains(":")) {
                if (Platform.isModLoaded(id.split(":")[0])) {
                    enchantments.add(Registries.ENCHANTMENT.get(new Identifier(id)));
                }
            } else {
                enchantments.add(Registries.ENCHANTMENT.get(new Identifier(id)));
            }
        }
        return enchantments;
    }

    @Override
    public JsonElement merge(JsonElement old, JsonElement toMerge, MergeType type) {
        switch (type) {
            case OVERWRITE -> {
                return toMerge;
            }
            case EXTEND -> {
                EnchantmentPropertyJson oldJson = Miapi.gson.fromJson(old, EnchantmentPropertyJson.class);
                EnchantmentPropertyJson mergeJson = Miapi.gson.fromJson(toMerge, EnchantmentPropertyJson.class);
                mergeJson.allowed.forEach(allowedEnchant -> {
                    if (!oldJson.forbidden.contains(allowedEnchant)) {
                        oldJson.allowed.add(allowedEnchant);
                    }
                });
                return Miapi.gson.toJsonTree(oldJson);
            }
            case SMART -> {
                EnchantmentPropertyJson oldJson = Miapi.gson.fromJson(old, EnchantmentPropertyJson.class);
                EnchantmentPropertyJson mergeJson = Miapi.gson.fromJson(toMerge, EnchantmentPropertyJson.class);
                mergeJson.allowed.forEach(allowedEnchant -> {
                    if (!oldJson.forbidden.contains(allowedEnchant)) {
                        oldJson.forbidden.remove(allowedEnchant);
                    }
                    oldJson.allowed.add(allowedEnchant);
                });
                mergeJson.forbidden.forEach(forbiddenEnchant -> {
                    if (oldJson.allowed.contains(forbiddenEnchant)) {
                        oldJson.allowed.remove(forbiddenEnchant);
                    }
                    if (!oldJson.forbidden.contains(forbiddenEnchant)) {
                        oldJson.forbidden.add(forbiddenEnchant);
                    }
                });
                return Miapi.gson.toJsonTree(oldJson);
            }
        }
        return toMerge;
    }

    @Override
    public boolean shouldExecuteOnCraft(ItemModule.ModuleInstance module) {
        return true;
    }

    @Override
    public ItemStack preview(ItemStack old, ItemStack crafting, PlayerEntity player, ItemModule.ModuleInstance newModule, ItemModule module, List<ItemStack> inventory, PacketByteBuf buf) {
        List<Enchantment> allowedEnchants = getAllowedList(crafting);
        Map<Enchantment, Integer> newEnchants = new HashMap<>();
        for (Map.Entry<Enchantment, Integer> entry : EnchantmentHelper.get(crafting).entrySet()) {
            if (allowedEnchants.contains(entry.getKey())) {
                newEnchants.put(entry.getKey(), entry.getValue());
            }
        }
        for (Map.Entry<Enchantment, Integer> entry : EnchantmentHelper.get(old).entrySet()) {
            if (allowedEnchants.contains(entry.getKey())) {
                newEnchants.put(entry.getKey(), entry.getValue());
            }
        }
        crafting.removeSubNbt("Enchantments");
        EnchantmentHelper.set(newEnchants, crafting);
        return crafting;
    }

    @Override
    public List<ItemStack> performCraftAction(ItemStack old, ItemStack crafting, PlayerEntity player, ItemModule.ModuleInstance newModule, ItemModule module, List<ItemStack> inventory, PacketByteBuf buf) {
        List<Enchantment> allowedEnchants = getAllowedList(crafting);
        Map<Enchantment, Integer> newEnchants = new HashMap<>();
        for (Map.Entry<Enchantment, Integer> entry : EnchantmentHelper.get(crafting).entrySet()) {
            if (allowedEnchants.contains(entry.getKey())) {
                newEnchants.put(entry.getKey(), entry.getValue());
            }
        }
        crafting.removeSubNbt("Enchantments");
        EnchantmentHelper.set(newEnchants, crafting);
        return CraftingProperty.super.performCraftAction(old, crafting, player, newModule, module, inventory, buf);
    }

    public static class EnchantmentPropertyJson {
        public List<String> allowed = new ArrayList<>();
        public List<String> forbidden = new ArrayList<>();
    }
}
