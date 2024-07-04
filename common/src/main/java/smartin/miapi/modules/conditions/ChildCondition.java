package smartin.miapi.modules.conditions;

import com.google.gson.JsonElement;
import smartin.miapi.modules.ModuleInstance;

import java.util.Optional;

public class ChildCondition implements ModuleCondition {
    public ModuleCondition condition;

    public ChildCondition() {

    }

    private ChildCondition(ModuleCondition condition) {
        this.condition = condition;
    }

    @Override
    public boolean isAllowed(ConditionManager.ConditionContext conditionContext) {
        Optional<ModuleInstance> optional = conditionContext.getContext(ConditionManager.MODULE_CONDITION_CONTEXT);
        if (optional.isPresent()) {
            ModuleInstance moduleInstance = optional.get();
            for (ModuleInstance otherInstance : moduleInstance.subModules.values()) {
                assert otherInstance.parent != null;
                ConditionManager.ConditionContext copiedContext = conditionContext.copy();
                copiedContext.setContext(ConditionManager.MODULE_CONDITION_CONTEXT, otherInstance);
                copiedContext.setContext(ConditionManager.MODULE_PROPERTIES, otherInstance.properties);
                if (condition.isAllowed(copiedContext)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public ModuleCondition load(JsonElement element) {
        return new ChildCondition(ConditionManager.get(element.getAsJsonObject().get("condition")));
    }
}
