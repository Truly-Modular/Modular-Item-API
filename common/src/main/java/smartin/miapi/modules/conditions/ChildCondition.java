package smartin.miapi.modules.conditions;

import com.google.gson.JsonElement;
import smartin.miapi.modules.ModuleInstance;

public class ChildCondition implements ModuleCondition {
    public ModuleCondition condition;

    public ChildCondition() {

    }

    private ChildCondition(ModuleCondition condition) {
        this.condition = condition;
    }

    @Override
    public boolean isAllowed(ConditionManager.ConditionContext conditionContext) {
        if(conditionContext instanceof ConditionManager.ModuleConditionContext moduleCondition){
            if(moduleCondition.moduleInstance != null){
                ModuleInstance moduleInstance = moduleCondition.moduleInstance;
                for (ModuleInstance otherInstace : moduleInstance.subModules.values()) {
                    assert otherInstace.parent != null;
                    ConditionManager.ModuleConditionContext copy = moduleCondition.copy();
                    copy.moduleInstance = otherInstace;
                    copy.propertyMap = otherInstace.getOldProperties();
                    if (condition.isAllowed(copy)) {
                        return true;
                    }
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
