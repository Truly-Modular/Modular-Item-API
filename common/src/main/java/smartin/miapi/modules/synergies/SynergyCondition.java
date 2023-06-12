package smartin.miapi.modules.synergies;

import com.google.gson.JsonElement;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.Map;

public interface SynergyCondition {

    boolean isAllowed(ItemModule.ModuleInstance moduleInstance, Map<ModuleProperty,JsonElement> propertyMap);

    SynergyCondition load(JsonElement element);
}
