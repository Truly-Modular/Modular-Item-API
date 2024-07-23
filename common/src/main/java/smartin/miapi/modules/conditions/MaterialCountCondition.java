package smartin.miapi.modules.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.material.Material;
import smartin.miapi.modules.material.MaterialProperty;

import java.util.List;
import java.util.Optional;

public class MaterialCountCondition implements ModuleCondition {
    public static Codec<MaterialCountCondition> CODEC = RecordCodecBuilder.create((instance) ->
            instance.group(
                    Codec.STRING
                            .fieldOf("material")
                            .forGetter((condition) -> condition.material),
                    Codec.INT
                            .optionalFieldOf("count", 1)
                            .forGetter((condition) -> condition.count),
                    ComponentSerialization.CODEC
                            .optionalFieldOf("error", Component.translatable(Miapi.MOD_ID + ".condition.material.error"))
                            .forGetter((condition) -> condition.error)
            ).apply(instance, MaterialCountCondition::new));

    public String material = "";
    public int count;
    public Component error;

    public MaterialCountCondition(String material, int count, Component error) {
        this.material = material;
        this.count = count;
        this.error = error;
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
}
