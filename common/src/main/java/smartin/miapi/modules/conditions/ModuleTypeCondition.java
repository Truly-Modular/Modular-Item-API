package smartin.miapi.modules.conditions;

import com.google.gson.JsonElement;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.registries.RegistryInventory;

import java.util.Optional;

public class ModuleTypeCondition implements ModuleCondition {
    public ItemModule module;

    public ModuleTypeCondition() {

    }

    public ModuleTypeCondition(ItemModule module) {
        this.module = module;
    }

    @Override
    public boolean isAllowed(ConditionManager.ConditionContext conditionContext) {
        Optional<ModuleInstance> optional = conditionContext.getContext(ConditionManager.MODULE_CONDITION_CONTEXT);
        if (optional.isPresent()) {
            ModuleInstance moduleInstance = optional.get();
            return moduleInstance.module.equals(module);
        }
        return false;
    }

    @Override
    public ModuleCondition load(JsonElement element) {
        return new ModuleTypeCondition(RegistryInventory.modules.get(element.getAsJsonObject().get("module").getAsString()));
    }
}
