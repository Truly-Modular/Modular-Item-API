package smartin.miapi.modules.conditions;

import com.google.gson.JsonElement;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.Map;

public class ParentCondition implements ModuleCondition {
    public ModuleCondition condition;

    public ParentCondition() {

    }

    private ParentCondition(ModuleCondition module) {
        this.condition = module;
    }

    @Override
    public boolean isAllowed(ItemModule.ModuleInstance moduleInstance, Map<ModuleProperty, JsonElement> propertyMap) {
        if (moduleInstance.parent != null && condition.isAllowed(moduleInstance.parent, moduleInstance.parent.module.getKeyedProperties())) {
            return true;
        }
        return false;
    }

    @Override
    public ModuleCondition load(JsonElement element) {
        return new ParentCondition(ConditionManager.get(element.getAsJsonObject().get("condition")));
    }
}
