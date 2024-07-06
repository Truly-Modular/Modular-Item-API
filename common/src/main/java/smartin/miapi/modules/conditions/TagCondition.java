package smartin.miapi.modules.conditions;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import smartin.miapi.modules.properties.TagProperty;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TagCondition implements ModuleCondition {
    public static Codec<ModuleCondition> CODEC = new Codec<ModuleCondition>() {
        @Override
        public <T> DataResult<Pair<ModuleCondition, T>> decode(DynamicOps<T> ops, T input) {
            Pair<String, T> result = Codec.STRING.decode(ops, ops.getMap(input).getOrThrow().get("tag")).getOrThrow();
            Component warning = ComponentSerialization.CODEC
                    .parse(ops, ops.getMap(input)
                            .getOrThrow()
                            .get("error"))
                    .result().orElse(Component.translatable("miapi.crafting_condition.false"));
            return DataResult.success(new Pair(new TagCondition(result.getFirst(), warning), result.getSecond()));
        }

        @Override
        public <T> DataResult<T> encode(ModuleCondition input, DynamicOps<T> ops, T prefix) {
            return DataResult.error(() -> "encoding condition is not fully supported");
        }
    };
    public String tag = "";
    Component onFalse = null;

    public TagCondition() {

    }

    public TagCondition(String tag, Component component) {
        this.tag = tag;
        onFalse = component;
    }

    @Override
    public boolean isAllowed(ConditionManager.ConditionContext conditionContext) {
        Optional<Map<ModuleProperty<?>, Object>> propertyMapOptional = conditionContext.getContext(ConditionManager.MODULE_PROPERTIES);
        if (propertyMapOptional.isPresent()) {
            Map<ModuleProperty<?>, Object> propertyMap = propertyMapOptional.get();
            List<String> tags = (List<String>) propertyMap.get(TagProperty.property);
            if (tags != null) {
                if (tags.contains(tag)) {
                    return true;
                }
            }
            conditionContext.failReasons.add(onFalse);
        }
        return false;
    }
}
