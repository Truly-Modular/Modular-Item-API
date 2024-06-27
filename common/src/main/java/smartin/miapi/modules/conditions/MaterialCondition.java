package smartin.miapi.modules.conditions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import smartin.miapi.Miapi;
import smartin.miapi.modules.material.Material;
import smartin.miapi.modules.material.MaterialProperty;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.List;
import java.util.Map;
import net.minecraft.network.chat.Component;

public class MaterialCondition implements ModuleCondition {
    public String material = "";
    public Component error = Component.translatable(Miapi.MOD_ID + ".condition.material.error");

    public MaterialCondition() {

    }

    public MaterialCondition(String material) {
        this.material = material;
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
            if (material1 != null && MaterialProperty.getMaterial(data).getKey().equals(material)) {
                return true;
            }
            reasons.add(error);
        }
        return false;
    }

    @Override
    public ModuleCondition load(JsonElement element) {
        JsonObject object = element.getAsJsonObject();
        MaterialCondition condition = new MaterialCondition(object.get("material").getAsString());
        condition.error = ModuleProperty.getText(object, "error", Component.translatable(Miapi.MOD_ID + ".condition.material.error"));

        return condition;
    }
}
