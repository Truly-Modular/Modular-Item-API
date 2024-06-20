package smartin.miapi.modules.conditions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class OrCondition implements ModuleCondition {
    List<ModuleCondition> conditions;

    public OrCondition() {

    }

    public OrCondition(List<ModuleCondition> conditions) {
        this.conditions = conditions;
    }

    @Override
    public boolean isAllowed(ConditionManager.ConditionContext conditionContext) {
        boolean isAllowed = false;
        for (ModuleCondition condition : conditions) {
            if (condition.isAllowed(conditionContext)) {
                isAllowed = true;
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
        return new OrCondition(conditionsToLoad);
    }
}
