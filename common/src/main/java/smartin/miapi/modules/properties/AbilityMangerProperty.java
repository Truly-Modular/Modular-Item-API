package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.conditions.ConditionManager;
import smartin.miapi.modules.conditions.ModuleCondition;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.Map;

public class AbilityMangerProperty implements ModuleProperty {
    public static String KEY = "ability_context";
    public static AbilityMangerProperty property;

    public AbilityMangerProperty() {
        property = this;
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        return true;
    }

    @Override
    public JsonElement merge(JsonElement old, JsonElement toMerge, MergeType type) {
        return mergeAsMap(old, toMerge, type);
    }

    @Nullable
    public static AbilityContext getContext(ItemStack itemStack, String key) {
        AbilityContext context = null;
        for (ItemModule.ModuleInstance moduleInstance : ItemModule.getModules(itemStack).allSubModules()) {
            JsonElement element = moduleInstance.getProperties().get(property);
            if (element != null) {
                Map<String, JsonElement> jsonElementMap = element.getAsJsonObject().asMap();
                if (jsonElementMap.containsKey(key)) {
                    context = new AbilityContext(jsonElementMap.get(key).getAsJsonObject(), moduleInstance, itemStack);
                }
            }
        }
        return context;
    }


    public static class AbilityContext {
        public JsonObject contextJson;
        public ItemModule.ModuleInstance contextInstance;
        public ItemStack contextStack;
        public ModuleCondition moduleCondition;

        public AbilityContext(JsonObject element, ItemModule.ModuleInstance moduleInstance, ItemStack itemStack) {
            this.contextInstance = moduleInstance;
            this.contextJson = element;
            this.contextStack = itemStack;
            moduleCondition = ConditionManager.get(element.get("condition"));
        }

        public double getValue(String key, double defaultValue) {
            return ModuleProperty.getDouble(contextJson, key, contextInstance, defaultValue);
        }

        public boolean getValue(String key, boolean defaultValue) {
            return ModuleProperty.getBoolean(contextJson, key, contextInstance, defaultValue);
        }

        public int getValue(String key, int defaultValue) {
            return ModuleProperty.getInteger(contextJson, key, contextInstance, defaultValue);
        }
    }
}
