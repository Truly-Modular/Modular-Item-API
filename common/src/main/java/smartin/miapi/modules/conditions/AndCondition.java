package smartin.miapi.modules.conditions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class AndCondition implements ModuleCondition {
    List<ModuleCondition> conditions;

    public AndCondition() {

    }

    public AndCondition(List<ModuleCondition> conditions) {
        this.conditions = conditions;
    }

    @Override
    public boolean isAllowed(ConditionManager.ConditionContext conditionContext) {
        boolean isAllowed = true;
        for (ModuleCondition condition : conditions) {
            if (!condition.isAllowed(conditionContext)) {
                isAllowed = false;
            }
        }
        return isAllowed;
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
