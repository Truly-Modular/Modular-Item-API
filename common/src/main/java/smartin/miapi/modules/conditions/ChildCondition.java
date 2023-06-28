package smartin.miapi.modules.conditions;

import com.google.gson.JsonElement;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.registries.RegistryInventory;

import java.util.Map;

public class ChildCondition implements ModuleCondition {
    public ModuleCondition condition;

    public ChildCondition() {

    }

    private ChildCondition(ModuleCondition condition) {
        this.condition = condition;
    }

    @Override
    public boolean isAllowed(ItemModule.ModuleInstance moduleInstance, Map<ModuleProperty, JsonElement> propertyMap) {
        for (ItemModule.ModuleInstance otherInstace : moduleInstance.subModules.values()) {
            if (condition.isAllowed(otherInstace, moduleInstance.parent.module.getKeyedProperties())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ModuleCondition load(JsonElement element) {
        return new ChildCondition(ConditionManager.get(element.getAsJsonObject().get("condition")));
    }
}
