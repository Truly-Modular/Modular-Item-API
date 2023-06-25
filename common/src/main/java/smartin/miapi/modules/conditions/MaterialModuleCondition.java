package smartin.miapi.modules.conditions;

import com.google.gson.JsonElement;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.MaterialProperty;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.Map;

public class MaterialModuleCondition implements ModuleCondition {
    public String material = "";

    public MaterialModuleCondition() {

    }

    public MaterialModuleCondition(String material) {
        this.material = material;
    }

    @Override
    public boolean isAllowed(ItemModule.ModuleInstance moduleInstance, Map<ModuleProperty,JsonElement> propertyMap) {
        JsonElement data = propertyMap.get(MaterialProperty.property);
        if(data==null){
            return false;
        }
        MaterialProperty.Material material1 = MaterialProperty.getMaterial(data);
        if(material1!=null){
            return MaterialProperty.getMaterial(data).key.equals(material);
        }
        return false;
    }

    @Override
    public ModuleCondition load(JsonElement element) {
        return new MaterialModuleCondition(element.getAsJsonObject().get("material").getAsString());
    }
}
