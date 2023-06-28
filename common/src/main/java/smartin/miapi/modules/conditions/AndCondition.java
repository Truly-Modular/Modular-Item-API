package smartin.miapi.modules.conditions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AndCondition implements ModuleCondition {
    List<ModuleCondition> conditions;

    public AndCondition() {

    }

    public AndCondition(List<ModuleCondition> conditions) {
        this.conditions = conditions;
    }

    @Override
    public boolean isAllowed(ItemModule.ModuleInstance moduleInstance, Map<ModuleProperty, JsonElement> propertyMap) {
        boolean isAllowed = true;
        for (ModuleCondition condition : conditions) {
            if (!condition.isAllowed(moduleInstance, propertyMap)) {
                isAllowed = false;
            }
        }
        return false;
    }

    @Override
    public ModuleCondition load(JsonElement element) {
        List<ModuleCondition> conditionsToLoad = new ArrayList<>();
        JsonObject object = element.getAsJsonObject();
        object.get("conditions").getAsJsonArray().forEach(subElement -> {
            conditionsToLoad.add(ConditionManager.get(subElement));
        });
        return new AndCondition(conditionsToLoad);
    }
}
