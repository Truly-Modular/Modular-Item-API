package smartin.miapi.modules.conditions;

import com.google.gson.JsonElement;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.Map;

public interface ModuleCondition {

    boolean isAllowed(ItemModule.ModuleInstance moduleInstance, Map<ModuleProperty,JsonElement> propertyMap);

    ModuleCondition load(JsonElement element);
}
