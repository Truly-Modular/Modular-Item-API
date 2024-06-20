package smartin.miapi.modules.conditions;

import com.google.gson.JsonElement;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.registries.RegistryInventory;

public class ModuleTypeCondition implements ModuleCondition {
    public ItemModule module;

    public ModuleTypeCondition() {

    }

    public ModuleTypeCondition(ItemModule module) {
        this.module = module;
    }

    @Override
    public boolean isAllowed(ConditionManager.ConditionContext conditionContext) {
        if(conditionContext instanceof ConditionManager.ModuleConditionContext moduleConditionContext) {
            ItemModule.ModuleInstance moduleInstance = moduleConditionContext.moduleInstance;
            return moduleInstance != null && moduleInstance.module.equals(module);
        }
        return false;
    }

    @Override
    public ModuleCondition load(JsonElement element) {
        return new ModuleTypeCondition(RegistryInventory.modules.get(element.getAsJsonObject().get("module").getAsString()));
    }
}
