package smartin.miapi.modules.conditions;

import com.google.gson.JsonElement;
import smartin.miapi.modules.ModuleInstance;

public class ParentCondition implements ModuleCondition {
    public ModuleCondition condition;

    public ParentCondition() {

    }

    private ParentCondition(ModuleCondition module) {
        this.condition = module;
    }

    @Override
    public boolean isAllowed(ConditionManager.ConditionContext conditionContext) {
        if (conditionContext instanceof ConditionManager.ModuleConditionContext moduleConditionContext) {
            ModuleInstance moduleInstance = moduleConditionContext.moduleInstance;
            if (moduleInstance != null && moduleInstance.parent != null) {
                ConditionManager.ModuleConditionContext copy = moduleConditionContext.copy();
                copy.moduleInstance = moduleInstance.parent;
                copy.propertyMap = moduleInstance.parent.getProperties();
                return condition.isAllowed(copy);
            }
        }
        return false;
    }

    @Override
    public ModuleCondition load(JsonElement element) {
        return new ParentCondition(ConditionManager.get(element.getAsJsonObject().get("condition")));
    }
}
