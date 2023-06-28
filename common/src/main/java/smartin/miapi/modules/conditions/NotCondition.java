package smartin.miapi.modules.conditions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NotCondition implements ModuleCondition {
    ModuleCondition conditions;

    public NotCondition() {

    }

    public NotCondition(ModuleCondition conditions) {
        this.conditions = conditions;
    }

    @Override
    public boolean isAllowed(ItemModule.ModuleInstance moduleInstance, Map<ModuleProperty, JsonElement> propertyMap) {
        return !conditions.isAllowed(moduleInstance,propertyMap);
    }

    @Override
    public ModuleCondition load(JsonElement element) {
        return new NotCondition(ConditionManager.get(element.getAsJsonObject().get("condition")));
    }
}
