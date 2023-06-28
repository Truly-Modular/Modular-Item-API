package smartin.miapi.modules.conditions;

import com.google.gson.JsonElement;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.MaterialProperty;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.registries.RegistryInventory;

import java.util.Map;

public class ModuleTypeCondition implements ModuleCondition {
    public ItemModule module;

    public ModuleTypeCondition() {

    }

    public ModuleTypeCondition(ItemModule module) {
        this.module = module;
    }

    @Override
    public boolean isAllowed(ItemModule.ModuleInstance moduleInstance, Map<ModuleProperty, JsonElement> propertyMap) {
        if (moduleInstance != null) {
            return moduleInstance.module.equals(module);
        }
        return false;
    }

    @Override
    public ModuleCondition load(JsonElement element) {
        return new ModuleTypeCondition(RegistryInventory.modules.get(element.getAsJsonObject().get("module").getAsString()));
    }
}
