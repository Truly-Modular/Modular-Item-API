package smartin.miapi.modules.conditions;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
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

    public static Codec<ModuleCondition> CODEC = new Codec<ModuleCondition>() {
        @Override
        public <T> DataResult<Pair<ModuleCondition, T>> decode(DynamicOps<T> ops, T input) {
            Pair<String, T> result = Codec.STRING.decode(ops, ops.getMap(input).getOrThrow().get("conditions")).getOrThrow();
            Component warning = ComponentSerialization.CODEC
                    .parse(ops, ops.getMap(input)
                            .getOrThrow()
                            .get("error"))
                    .result().orElse(Component.translatable("miapi.crafting_condition.false"));
            return DataResult.success(new Pair(new MaterialCondition(result.getFirst(), warning), result.getSecond()));
        }

        @Override
        public <T> DataResult<T> encode(ModuleCondition input, DynamicOps<T> ops, T prefix) {
            return null;
        }
    };
    public String materialKey = "";
    public Component error = Component.translatable(Miapi.MOD_ID + ".condition.material.error");

    public MaterialCondition() {

    }

    public MaterialCondition(String materialKey, Component error) {
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
            if (material != null && material.getKey().equals(materialKey)) {
                return true;
            }
            reasons.add(error);
        }
        conditionContext.failReasons.add(Component.translatable(Miapi.MOD_ID + ".condition.material.error"));
        return false;
    }
}
