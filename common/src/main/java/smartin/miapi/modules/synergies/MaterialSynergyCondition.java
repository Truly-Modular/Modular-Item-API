package smartin.miapi.modules.synergies;

import com.google.gson.JsonElement;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.MaterialProperty;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.Map;

public class MaterialSynergyCondition implements SynergyCondition {
    public String material = "";

    public MaterialSynergyCondition() {

    }

    public MaterialSynergyCondition(String material) {
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
    public SynergyCondition load(JsonElement element) {
        return new MaterialSynergyCondition(element.getAsJsonObject().get("material").getAsString());
    }
}
