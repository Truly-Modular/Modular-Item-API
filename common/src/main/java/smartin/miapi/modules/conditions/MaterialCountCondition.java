package smartin.miapi.modules.conditions;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.material.Material;
import smartin.miapi.modules.material.MaterialProperty;

import java.util.List;
import java.util.Optional;

public class MaterialCountCondition implements ModuleCondition {
    public static Codec<ModuleCondition> CODEC = new Codec<ModuleCondition>() {
        @Override
        public <T> DataResult<Pair<ModuleCondition, T>> decode(DynamicOps<T> ops, T input) {
            Pair<String, T> result = Codec.STRING.decode(ops, ops.getMap(input).getOrThrow().get("material")).getOrThrow();
            int count = Codec.intRange(0, 10000).parse(ops, input).getOrThrow();
            Component warning = ComponentSerialization.CODEC
                    .parse(ops, ops.getMap(input)
                            .getOrThrow()
                            .get("error"))
                    .result().orElse(Component.translatable(Miapi.MOD_ID + ".condition.material.error"));
            return DataResult.success(new Pair(new MaterialCountCondition(result.getFirst(),count, warning), result.getSecond()));
        }

        @Override
        public <T> DataResult<T> encode(ModuleCondition input, DynamicOps<T> ops, T prefix) {
            return DataResult.error(() -> "encoding condition is not fully supported");
        }
    };
    public String material = "";
    public int count;
    public Component error;

    public MaterialCountCondition(String material, int count,Component error) {
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
