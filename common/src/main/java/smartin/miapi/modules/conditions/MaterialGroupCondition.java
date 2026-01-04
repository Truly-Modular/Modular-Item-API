package smartin.miapi.modules.conditions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.text.Text;
import smartin.miapi.Miapi;
import smartin.miapi.modules.material.Material;
import smartin.miapi.modules.material.MaterialProperty;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.List;
import java.util.Map;

public class MaterialGroupCondition implements ModuleCondition {
    public String material = "";
    public Text error = Text.translatable(Miapi.MOD_ID + ".condition.material.error");

    public MaterialGroupCondition() {

    }

    public MaterialGroupCondition(String material) {
        this.material = material;
    }

    @Override
    public boolean isAllowed(ConditionManager.ConditionContext conditionContext) {
        if (conditionContext instanceof ConditionManager.ModuleConditionContext moduleConditionContext) {
            Map<ModuleProperty, JsonElement> propertyMap = moduleConditionContext.propertyMap;
            List<Text> reasons = moduleConditionContext.reasons;
            JsonElement data = propertyMap.get(MaterialProperty.property);
            if (data == null) {
                reasons.add(Text.translatable(Miapi.MOD_ID + ".condition.material.error"));
                return false;
            }
            Material material1 = MaterialProperty.getMaterial(data);
            if (material1 != null && material1.getGroups().contains(material)) {
                return true;
            }
            reasons.add(error);
        }
        return false;
    }

    @Override
    public ModuleCondition load(JsonElement element) {
        JsonObject object = element.getAsJsonObject();
        MaterialGroupCondition condition = new MaterialGroupCondition(object.get("material_group").getAsString());
        condition.error = ModuleProperty.getText(object, "error", Text.translatable(Miapi.MOD_ID + ".condition.material.error"));

        return condition;
    }
}
