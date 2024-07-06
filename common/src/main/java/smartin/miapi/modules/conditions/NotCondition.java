package smartin.miapi.modules.conditions;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

public class NotCondition implements ModuleCondition {
    public static Codec<ModuleCondition> CODEC = new Codec<ModuleCondition>() {
        @Override
        public <T> DataResult<Pair<ModuleCondition, T>> decode(DynamicOps<T> ops, T input) {
            Pair<ModuleCondition, T> result = ConditionManager.CONDITION_CODEC.decode(ops, ops.getMap(input).getOrThrow().get("conditions")).getOrThrow();
            Component warning = ComponentSerialization.CODEC
                    .parse(ops, ops.getMap(input)
                            .getOrThrow()
                            .get("error"))
                    .result().orElse(Component.translatable("miapi.crafting_condition.false"));
            return DataResult.success(new Pair(new NotCondition(result.getFirst(), warning), result.getSecond()));
        }

        @Override
        public <T> DataResult<T> encode(ModuleCondition input, DynamicOps<T> ops, T prefix) {
            return DataResult.error(() -> "encoding condition is not fully supported");
        }
    };
    ModuleCondition conditions;
    Component onFalse = null;

    public NotCondition() {

    }

    public NotCondition(ModuleCondition conditions, Component error) {
        this.conditions = conditions;
        this.onFalse = error;
    }

    @Override
    public boolean isAllowed(ConditionManager.ConditionContext conditionContext) {
        if (!conditions.isAllowed(conditionContext)) {
            conditionContext.failReasons.add(onFalse);
            return true;
        }
        return false;
    }
}
