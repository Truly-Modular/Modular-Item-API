package smartin.miapi.modules.conditions;

import com.google.gson.JsonElement;
import net.minecraft.network.chat.Component;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.material.Material;
import smartin.miapi.modules.material.MaterialProperty;

import java.util.List;
import java.util.Optional;

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
        Optional<ModuleInstance> optional = conditionContext.getContext(ConditionManager.MODULE_CONDITION_CONTEXT);
        if (optional.isPresent()) {
            ModuleInstance moduleInstance = optional.get();
            Material material1 = MaterialProperty.materials.get(material);
            if (material1 != null && count >= getCount(moduleInstance, material1)) {
                return true;
            }
        }
        conditionContext.failReasons.add(Component.translatable(Miapi.MOD_ID + ".condition.material.error"));
        return false;
    }

    public int getCount(ModuleInstance moduleInstance, Material material) {
        if (moduleInstance != null) {
            List<ModuleInstance> moduleInstances = moduleInstance.getRoot().allSubModules().stream().filter(moduleInstance1 -> material.equals(MaterialProperty.getMaterial(moduleInstance1))).toList();
            return moduleInstances.size();
        }
        return 0;
    }


    @Override
    public ModuleCondition load(JsonElement element) {
        return new MaterialCountCondition(element.getAsJsonObject().get("material").getAsString(), element.getAsJsonObject().get("count").getAsInt());
    }
}
