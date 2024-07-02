package smartin.miapi.modules.conditions;

import com.google.gson.JsonElement;

public class TrueCondition implements ModuleCondition {

    @Override
    public boolean isAllowed(ConditionManager.ConditionContext conditionContext) {
        return true;
    }

    @Override
    public ModuleCondition load(JsonElement element) {
        return new TrueCondition();
    }
}
