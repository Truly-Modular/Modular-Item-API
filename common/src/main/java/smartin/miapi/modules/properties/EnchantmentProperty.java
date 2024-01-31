package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import smartin.miapi.Miapi;
import smartin.miapi.blocks.ModularWorkBenchEntity;
import smartin.miapi.craft.CraftAction;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.properties.util.CraftingProperty;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.*;

/**
 * This property allows to set allowed enchantments
 */
public class EnchantmentProperty implements CraftingProperty, ModuleProperty {
    public static final String KEY = "enchantments";
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
        addDefault("miapi:axe", new String[]{"miapi:basic", "miapi:tool"}, Items.WOODEN_AXE, Items.STONE_AXE, Items.GOLDEN_AXE, Items.DIAMOND_AXE, Items.NETHERITE_AXE);
        addDefault("miapi:pickaxe", new String[]{"miapi:basic", "miapi:tool"}, Items.WOODEN_PICKAXE, Items.STONE_PICKAXE, Items.GOLDEN_PICKAXE, Items.DIAMOND_PICKAXE, Items.NETHERITE_PICKAXE);
        addDefault("miapi:shovel", new String[]{"miapi:basic", "miapi:tool"}, Items.WOODEN_SHOVEL, Items.STONE_SHOVEL, Items.GOLDEN_SHOVEL, Items.DIAMOND_SHOVEL, Items.NETHERITE_SHOVEL);
        addDefault("miapi:hoe", new String[]{"miapi:basic", "miapi:tool"}, Items.WOODEN_HOE, Items.STONE_HOE, Items.GOLDEN_HOE, Items.DIAMOND_HOE, Items.NETHERITE_HOE);
    }

    public static void addDefault(String addToID, String[] removeIDs, Item... items) {
        Registries.ENCHANTMENT.stream().filter(enchant -> {
            for (Item item : items) {
                if (!enchant.isAcceptableItem(item.getDefaultStack())) {
                    return false;
                }
            }
            return true;
        }).filter(enchant -> {
            for (String remove : removeIDs) {
                Set<String> replacements = replaceMap.get(remove);
                String enchantId = Registries.ENCHANTMENT.getId(enchant).toString();
                if (replacements.contains(enchantId)) {
                    return false;
                }
            }
            return true;
        }).forEach(enchant -> {
            addToReplaceMap(addToID, Registries.ENCHANTMENT.getId(enchant).toString());
        });
    }

    private List<Enchantment> createAllowedList(ItemStack itemStack) {
        JsonElement element = ItemModule.getMergedProperty(itemStack, property);
        if (element != null) {
            EnchantmentPropertyJson json = Miapi.gson.fromJson(element, EnchantmentPropertyJson.class);
            if (json.allowed == null) json.allowed = new ArrayList<>();
            if (json.forbidden == null) json.forbidden = new ArrayList<>();
            List<Enchantment> enchantments = convert(json.allowed);
            enchantments.removeAll(convert(json.forbidden));
            return enchantments;
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
            if (enchantment != null && !enchantments.contains(enchantment)) {
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
    public boolean shouldExecuteOnCraft(ModuleInstance module, ModuleInstance root, ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack preview(ItemStack old, ItemStack crafting, PlayerEntity player, ModularWorkBenchEntity bench, CraftAction craftAction, ItemModule module, List<ItemStack> inventory, Map<String, String> data) {
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
    public List<ItemStack> performCraftAction(ItemStack old, ItemStack crafting, PlayerEntity player, ModularWorkBenchEntity bench, CraftAction craftAction, ItemModule module, List<ItemStack> inventory, Map<String, String> data) {
        List<Enchantment> allowedEnchants = getAllowedList(crafting);
        Map<Enchantment, Integer> newEnchants = new HashMap<>();
        for (Map.Entry<Enchantment, Integer> entry : EnchantmentHelper.get(crafting).entrySet()) {
            if (allowedEnchants.contains(entry.getKey())) {
                newEnchants.put(entry.getKey(), entry.getValue());
            }
        }
        crafting.removeSubNbt("Enchantments");
        EnchantmentHelper.set(newEnchants, crafting);
        return CraftingProperty.super.performCraftAction(old, crafting, player, bench, craftAction, module, inventory, data);
    }

    public static class EnchantmentPropertyJson {
        public List<String> allowed = new ArrayList<>();
        public List<String> forbidden = new ArrayList<>();
    }
}
