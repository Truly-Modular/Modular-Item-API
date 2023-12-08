package smartin.miapi.modules.conditions;

import com.google.gson.JsonElement;

public class NotCondition implements ModuleCondition {
    ModuleCondition conditions;

    public NotCondition() {

    }

    public NotCondition(ModuleCondition conditions) {
        this.conditions = conditions;
    }

    @Override
    public boolean isAllowed(ConditionManager.ConditionContext conditionContext) {
        return !conditions.isAllowed(conditionContext);
    }

    @Override
    public ModuleCondition load(JsonElement element) {
        return new NotCondition(ConditionManager.get(element.getAsJsonObject().get("condition")));
    }
}
