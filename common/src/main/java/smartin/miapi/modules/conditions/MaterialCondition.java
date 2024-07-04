package smartin.miapi.modules.conditions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import smartin.miapi.Miapi;
import smartin.miapi.modules.material.Material;
import smartin.miapi.modules.material.MaterialProperty;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MaterialCondition implements ModuleCondition {
    public String materialKey = "";
    public Component error = Component.translatable(Miapi.MOD_ID + ".condition.material.error");

    public MaterialCondition() {

    }

    public MaterialCondition(String materialKey) {
        this.materialKey = materialKey;
    }

    @Override
    public boolean isAllowed(ConditionManager.ConditionContext conditionContext) {
        Optional<Map<ModuleProperty<?>, Object>> propertyMapOptional = conditionContext.getContext(ConditionManager.MODULE_PROPERTIES);
        if (propertyMapOptional.isPresent()) {
            Map<ModuleProperty<?>, Object> propertyMap = propertyMapOptional.get();
            List <Component> reasons = conditionContext.failReasons;
            Material material = (Material) propertyMap.get(MaterialProperty.property);
            if (material != null && material.getKey().equals(materialKey)) {
                return true;
            }
            reasons.add(error);
        }
        conditionContext.failReasons.add(Component.translatable(Miapi.MOD_ID + ".condition.material.error"));
        return false;
    }

    @Override
    public ModuleCondition load(JsonElement element) {
        JsonObject object = element.getAsJsonObject();
        MaterialCondition condition = new MaterialCondition(object.get("material").getAsString());
        condition.error = ComponentSerialization.CODEC.parse(JsonOps.INSTANCE,object.get("item")).result().orElse( Component.translatable(Miapi.MOD_ID + ".condition.material.error"));

        return condition;
    }
}
