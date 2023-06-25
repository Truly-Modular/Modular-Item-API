package smartin.miapi.modules.conditions;

import com.google.gson.JsonElement;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.Map;

public class OtherModuleModuleCondition implements ModuleCondition {
    public ItemModule module;
    public OtherModuleModuleCondition(){

    }

    private OtherModuleModuleCondition(ItemModule module){
        this.module = module;
    }

    @Override
    public boolean isAllowed(ItemModule.ModuleInstance moduleInstance, Map<ModuleProperty, JsonElement> propertyMap) {
        for(ItemModule.ModuleInstance otherInstace:moduleInstance.getRoot().allSubModules()){
            if(otherInstace.module.equals(module)){
                return true;
            }
        }
        return false;
    }

    @Override
    public ModuleCondition load(JsonElement element) {
        return new OtherModuleModuleCondition(Miapi.moduleRegistry.get(element.getAsJsonObject().get("module").getAsString()));
    }
}
