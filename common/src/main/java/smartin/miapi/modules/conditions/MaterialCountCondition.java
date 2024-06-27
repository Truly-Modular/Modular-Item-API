package smartin.miapi.modules.conditions;

import com.google.gson.JsonElement;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.material.Material;
import smartin.miapi.modules.material.MaterialProperty;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.List;
import java.util.Map;
import net.minecraft.network.chat.Component;

public class MaterialCountCondition implements ModuleCondition {
    public String material = "";
    public int count;

    public MaterialCountCondition() {

    }

    public MaterialCountCondition(String material, int count) {
        this.material = material;
        this.count = count;
    }

    @Override
    public boolean isAllowed(ConditionManager.ConditionContext conditionContext) {
        if (conditionContext instanceof ConditionManager.ModuleConditionContext moduleConditionContext) {
            Map<ModuleProperty, JsonElement> propertyMap = moduleConditionContext.propertyMap;
            List<Component> reasons = moduleConditionContext.reasons;
            JsonElement data = propertyMap.get(MaterialProperty.property);
            if (data == null) {
                reasons.add(Component.translatable(Miapi.MOD_ID + ".condition.material.error"));
                return false;
            }
            Material material1 = MaterialProperty.getMaterial(data);
            if (
                    material1 != null &&
                            MaterialProperty.getMaterial(data).getKey().equals(material) &&
                            getCount(moduleConditionContext.moduleInstance, material1) == count
            ) {
                return true;
            }
            reasons.add(Component.translatable(Miapi.MOD_ID + ".condition.material.error"));
        }
        return false;
    }

    public int getCount(ModuleInstance moduleInstance, Material material) {
        if (moduleInstance != null) {
            List<ModuleInstance> moduleInstances = moduleInstance.getRoot().allSubModules().stream().filter(moduleInstance1 -> material.equals(MaterialProperty.getMaterial(moduleInstance1))).toList();
            for (int i = 0; i < moduleInstances.size(); i++) {
                if (moduleInstance.equals(moduleInstances.get(i))) {
                    return i;
                }
            }
        }
        return 0;
    }


    @Override
    public ModuleCondition load(JsonElement element) {
        return new MaterialCountCondition(element.getAsJsonObject().get("material").getAsString(), element.getAsJsonObject().get("count").getAsInt());
    }
}
