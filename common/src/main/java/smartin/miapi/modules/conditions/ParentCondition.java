package smartin.miapi.modules.conditions;

import com.google.gson.JsonElement;
import smartin.miapi.modules.ModuleInstance;

import java.util.Optional;

public class ParentCondition implements ModuleCondition {
    public ModuleCondition condition;

    public ParentCondition() {

    }

    private ParentCondition(ModuleCondition module) {
        this.condition = module;
    }

    @Override
    public boolean isAllowed(ConditionManager.ConditionContext conditionContext) {
        Optional<ModuleInstance> optional = conditionContext.getContext(ConditionManager.MODULE_CONDITION_CONTEXT);
        if (optional.isPresent()) {
            ModuleInstance moduleInstance = optional.get();
            if (moduleInstance.parent != null) {
                ConditionManager.ConditionContext copiedContext = conditionContext.copy();
                copiedContext.setContext(ConditionManager.MODULE_CONDITION_CONTEXT, moduleInstance.parent);
                copiedContext.setContext(ConditionManager.MODULE_PROPERTIES, moduleInstance.parent.properties);
                return condition.isAllowed(copiedContext);
            }
        }
        return false;
    }

    @Override
    public ModuleCondition load(JsonElement element) {
        return new ParentCondition(ConditionManager.get(element.getAsJsonObject().get("condition")));
    }
}
