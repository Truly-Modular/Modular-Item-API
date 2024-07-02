package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.abilities.util.ItemAbilityManager;
import smartin.miapi.modules.abilities.util.ItemUseAbility;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.conditions.ConditionManager;
import smartin.miapi.modules.conditions.ModuleCondition;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * This property manages the active {@link ItemUseAbility}
 */
public class AbilityMangerProperty implements ModuleProperty {
    public static String KEY = "ability_context";
    public static AbilityMangerProperty property;

    public AbilityMangerProperty() {
        property = this;
        ModularItemCache.setSupplier(KEY, AbilityMangerProperty::getForCache);
    }

    private static List<ItemUseAbility> getForCache(ItemStack itemStack) {
        List<ItemUseAbility> abilities = new ArrayList<>();
        JsonElement element = ItemModule.getMergedProperty(itemStack, property);
        if (element != null) {
            Map<String, JsonElement> jsonElementMap = element.getAsJsonObject().asMap();
            jsonElementMap.keySet().forEach(s -> {
                ItemUseAbility useAbility = ItemAbilityManager.useAbilityRegistry.get(s);
                if (useAbility != null) {
                    abilities.add(useAbility);
                }
            });
        }
        abilities.addAll(AbilityProperty.get(itemStack));
        abilities.sort(Comparator.comparingDouble(a -> a.getAbilityContext(itemStack).getPriority()));
        return abilities;
    }

    public static List<ItemUseAbility> get(ItemStack itemStack) {
        return ModularItemCache.get(itemStack, KEY, new ArrayList<>());
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        data.getAsJsonObject().asMap();
        return true;
    }

    public static boolean isPrimaryAbility(ItemUseAbility itemUseAbility, ItemStack itemStack) {
        List<ItemUseAbility> abilities = get(itemStack);
        return !abilities.isEmpty() && itemUseAbility == abilities.get(0);
    }

    @Override
    public JsonElement merge(JsonElement old, JsonElement toMerge, MergeType type) {
        return ModuleProperty.mergeAsMap(old, toMerge, type);
    }

    @Nullable
    public static AbilityContext getContext(ItemStack itemStack, String key) {
        AbilityContext context = null;
        for (ModuleInstance moduleInstance : ItemModule.getModules(itemStack).allSubModules()) {
            JsonElement element = moduleInstance.getOldProperties().get(property);
            if (element != null) {
                Map<String, JsonElement> jsonElementMap = element.getAsJsonObject().asMap();
                if (jsonElementMap.containsKey(key)) {
                    context = new AbilityContext(jsonElementMap.get(key).getAsJsonObject(), moduleInstance, itemStack);
                    context.isFullContext = true;
                }
            }
        }
        return context;
    }

    @Nullable
    public static AbilityContext getContext(ItemStack itemStack, ItemUseAbility ability) {
        return getContext(itemStack, ItemAbilityManager.useAbilityRegistry.findKey(ability));
    }


    public static class AbilityContext {
        public JsonObject contextJson;
        public ModuleInstance contextInstance;
        public ItemStack contextStack;
        public ModuleCondition moduleCondition;
        public double priority = 0.0f;
        public boolean isFullContext = false;
        private Map<String, Double> doubleCache = new HashMap<>();
        private Map<String, Boolean> booleanCache = new HashMap<>();
        private Map<String, Integer> integerCache = new HashMap<>();
        private Map<String, String> stringCache = new HashMap<>();
        private Map<String, Component> textCache = new HashMap<>();

        public AbilityContext(JsonObject element, ModuleInstance moduleInstance, ItemStack itemStack) {
            this.contextInstance = moduleInstance;
            this.contextJson = element;
            this.contextStack = itemStack;
            moduleCondition = ConditionManager.get(element.get("condition"));
            priority = getDouble("priority", 0.0);
        }

        public double getPriority() {
            return priority;
        }

        public double getDouble(String key, double defaultValue) {
            return doubleCache.computeIfAbsent(key, k -> ModuleProperty.getDouble(contextJson, key, contextInstance, defaultValue));
        }

        public boolean getBoolean(String key, boolean defaultValue) {
            return booleanCache.computeIfAbsent(key, k -> ModuleProperty.getBoolean(contextJson, key, contextInstance, defaultValue));
        }

        public int getInt(String key, int defaultValue) {
            return integerCache.computeIfAbsent(key, k -> ModuleProperty.getInteger(contextJson, key, contextInstance, defaultValue));
        }

        public String getString(String key, String defaultValue) {
            return stringCache.computeIfAbsent(key, k -> ModuleProperty.getString(contextJson, key, contextInstance, defaultValue));
        }

        public Component getText(String key, Component defaultValue) {
            return textCache.computeIfAbsent(key, k -> ModuleProperty.getText(contextJson, key, contextInstance, defaultValue));
        }
    }
}
