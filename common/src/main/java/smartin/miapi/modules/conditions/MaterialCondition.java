package smartin.miapi.modules.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.modules.material.Material;
import smartin.miapi.modules.material.MaterialProperty;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.List;
import java.util.Map;
import java.util.Optional;
/**
 * @header Material Condition
 * @description_start
 * this condition checks if this Module has a certain material
 * @desciption_end
 * @path /data_types/condition/material
 * @data type:material
 * @data material:the material to be checked
 */
public class MaterialCondition implements ModuleCondition {
    public static Codec<MaterialCondition> CODEC = RecordCodecBuilder.create((instance) ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("material")
                            .forGetter((condition) -> condition.materialKey),
                    ComponentSerialization.CODEC
                            .optionalFieldOf("error", Component.translatable(Miapi.MOD_ID + ".condition.material.error"))
                            .forGetter((condition) -> condition.error)
            ).apply(instance, MaterialCondition::new));

    public ResourceLocation materialKey;
    public Component error;

    public MaterialCondition(ResourceLocation materialKey, Component error) {
        this.materialKey = materialKey;
        this.error = error;
    }

    @Override
    public boolean isAllowed(ConditionManager.ConditionContext conditionContext) {
        Optional<Map<ModuleProperty<?>, Object>> propertyMapOptional = conditionContext.getContext(ConditionManager.MODULE_PROPERTIES);
        if (propertyMapOptional.isPresent()) {
            Map<ModuleProperty<?>, Object> propertyMap = propertyMapOptional.get();
            List<Component> reasons = conditionContext.failReasons;
            Material material = (Material) propertyMap.get(MaterialProperty.property);
            if (material != null && material.getID().equals(materialKey)) {
                return true;
            }
            reasons.add(error);
        }
        conditionContext.failReasons.add(Component.translatable(Miapi.MOD_ID + ".condition.material.error"));
        return false;
    }
}
