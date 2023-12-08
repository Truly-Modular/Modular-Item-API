package smartin.miapi.modules.conditions;

import com.google.gson.JsonElement;
import net.minecraft.text.Text;
import smartin.miapi.Miapi;
import smartin.miapi.modules.material.Material;
import smartin.miapi.modules.material.MaterialProperty;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.List;
import java.util.Map;

public class MaterialCondition implements ModuleCondition {
    public String material = "";

    public MaterialCondition() {

    }

    public MaterialCondition(String material) {
        this.material = material;
    }

    @Override
    public boolean isAllowed(ConditionManager.ConditionContext conditionContext) {
        if(conditionContext instanceof ConditionManager.ModuleConditionContext moduleConditionContext) {
            Map<ModuleProperty, JsonElement> propertyMap = moduleConditionContext.propertyMap;
            List<Text> reasons = moduleConditionContext.reasons;
            JsonElement data = propertyMap.get(MaterialProperty.property);
            if (data == null) {
                reasons.add(Text.translatable(Miapi.MOD_ID + ".condition.material.error"));
                return false;
            }
            Material material1 = MaterialProperty.getMaterial(data);
            if (material1 != null && MaterialProperty.getMaterial(data).getKey().equals(material)) {
                return true;
            }
            reasons.add(Text.translatable(Miapi.MOD_ID + ".condition.material.error"));
        }
        return false;
    }

    @Override
    public ModuleCondition load(JsonElement element) {
        return new MaterialCondition(element.getAsJsonObject().get("material").getAsString());
    }
}
