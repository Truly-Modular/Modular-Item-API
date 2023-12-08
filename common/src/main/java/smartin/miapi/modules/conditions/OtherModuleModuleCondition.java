package smartin.miapi.modules.conditions;

import com.google.gson.JsonElement;
import smartin.miapi.modules.ItemModule;

public class OtherModuleModuleCondition implements ModuleCondition {
    public ModuleCondition condition;

    public OtherModuleModuleCondition() {
    }

    private OtherModuleModuleCondition(ModuleCondition module) {
        this.condition = module;
    }

    @Override
    public boolean isAllowed(ConditionManager.ConditionContext conditionContext) {
        if (conditionContext instanceof ConditionManager.ModuleConditionContext moduleConditionContext) {
            ItemModule.ModuleInstance moduleInstance = moduleConditionContext.moduleInstance;
            if(moduleInstance!=null){
                for (ItemModule.ModuleInstance otherInstance : moduleInstance.getRoot().allSubModules()) {
                    ConditionManager.ModuleConditionContext copy = moduleConditionContext.copy();
                    copy.moduleInstance = otherInstance;
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
        return new OtherModuleModuleCondition(ConditionManager.get(element.getAsJsonObject().get("condition")));
    }
}
