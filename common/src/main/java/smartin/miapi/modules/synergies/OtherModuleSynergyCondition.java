package smartin.miapi.modules.synergies;

import com.google.gson.JsonElement;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.Map;

public class OtherModuleSynergyCondition implements SynergyCondition{
    public ItemModule module;
    public OtherModuleSynergyCondition(){

    }

    private OtherModuleSynergyCondition(ItemModule module){
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
    public SynergyCondition load(JsonElement element) {
        return new OtherModuleSynergyCondition(Miapi.moduleRegistry.get(element.getAsJsonObject().get("module").getAsString()));
    }
}
