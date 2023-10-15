package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import smartin.miapi.Miapi;
import smartin.miapi.blocks.ModularWorkBenchEntity;
import smartin.miapi.datapack.ReloadEvents;
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
    public static Map<String, Set<String>> replaceMap = new HashMap<>();

    public EnchantmentProperty() {
        property = this;
        ModularItemCache.setSupplier(KEY, this::createAllowedList);

        Miapi.registerReloadHandler(ReloadEvents.MAIN, "enchantment_categories", (isClient) -> {
            replaceMap.clear();
            fillMapDefault();
        }, (isClient, path, data) -> {
            JsonObject obj = JsonHelper.deserialize(data);
            String id = obj.getAsJsonPrimitive("id").getAsString();

            if (obj.has("add")) {
                obj.getAsJsonArray("add").forEach(element -> {
                    addToReplaceMap(
                            id,
                            element.getAsJsonPrimitive().getAsString()
                    );
                });
            }
        }, 1);
        ReloadEvents.END.subscribe((isClient -> {
            int size = 0;
            for (Set<String> entries : replaceMap.values()) {
                size += entries.size();
            }
            Miapi.LOGGER.info("Found " + size + " Enchantments");
        }));
    }

    private static void fillMapDefault() {
        addToReplaceMap("miapi:armor", EnchantmentTarget.ARMOR);
        addToReplaceMap("miapi:basic", EnchantmentTarget.BREAKABLE);
        addToReplaceMap("miapi:weapon", EnchantmentTarget.WEAPON);
        addToReplaceMap("miapi:tool", EnchantmentTarget.DIGGER);
        addToReplaceMap("miapi:fishing_rod", EnchantmentTarget.FISHING_ROD); // miapi doesn't have fishing rods as of making this, but whatever
        // no edged, it has no category, so it is done only in json.
        addToReplaceMap("miapi:bow", EnchantmentTarget.BOW);
        addToReplaceMap("miapi:crossbow", EnchantmentTarget.CROSSBOW);
        addToReplaceMap("miapi:helmet", EnchantmentTarget.ARMOR_HEAD);
        addToReplaceMap("miapi:chestplate", EnchantmentTarget.ARMOR_CHEST); // does nothing in vanilla, has potential with other mods installed though
        addToReplaceMap("miapi:leggings", EnchantmentTarget.ARMOR_LEGS);
        addToReplaceMap("miapi:boots", EnchantmentTarget.ARMOR_FEET);
        addToReplaceMap("miapi:trident", EnchantmentTarget.TRIDENT);
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
        return ModularItemCache.get(stack, KEY, Collections.emptyList());
    }

    public static void addToReplaceMap(String key, String enchant) {
        Set<String> list = replaceMap.getOrDefault(key, new HashSet<>());
        list.add(enchant);
        replaceMap.put(key, list);
    }

    public static void addToReplaceMap(String key, EnchantmentTarget target) {
        Set<String> list = replaceMap.getOrDefault(key, new HashSet<>());
        Registries.ENCHANTMENT.forEach(ench -> {
            if (ench.target == target) {
                list.add(Objects.requireNonNull(Registries.ENCHANTMENT.getId(ench)).toString());
            }
        });
        replaceMap.put(key, list);
    }

    public static boolean isAllowed(ItemStack stack, Enchantment enchantment) {
        return getAllowedList(stack).contains(enchantment);
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
            Enchantment enchantment = Registries.ENCHANTMENT.get(new Identifier(id));
            if(enchantment!=null && !enchantments.contains(enchantment)){
                enchantments.add(enchantment);
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
    public boolean shouldExecuteOnCraft(ItemModule.ModuleInstance module, ItemModule.ModuleInstance root, ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack preview(ItemStack old, ItemStack crafting, PlayerEntity player, ModularWorkBenchEntity bench, ItemModule.ModuleInstance newModule, ItemModule module, List<ItemStack> inventory, PacketByteBuf buf) {
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
    public List<ItemStack> performCraftAction(ItemStack old, ItemStack crafting, PlayerEntity player, ModularWorkBenchEntity bench, ItemModule.ModuleInstance newModule, ItemModule module, List<ItemStack> inventory, PacketByteBuf buf) {
        List<Enchantment> allowedEnchants = getAllowedList(crafting);
        Map<Enchantment, Integer> newEnchants = new HashMap<>();
        for (Map.Entry<Enchantment, Integer> entry : EnchantmentHelper.get(crafting).entrySet()) {
            if (allowedEnchants.contains(entry.getKey())) {
                newEnchants.put(entry.getKey(), entry.getValue());
            }
        }
        crafting.removeSubNbt("Enchantments");
        EnchantmentHelper.set(newEnchants, crafting);
        return CraftingProperty.super.performCraftAction(old, crafting, player, bench, newModule, module, inventory, buf);
    }

    public static class EnchantmentPropertyJson {
        public List<String> allowed = new ArrayList<>();
        public List<String> forbidden = new ArrayList<>();
    }
}
