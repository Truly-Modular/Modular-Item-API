package smartin.miapi.modules.conditions;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

import java.util.List;

public class AndCondition implements ModuleCondition {
    List<ModuleCondition> conditions;
    public static Codec<ModuleCondition> CODEC = new Codec<ModuleCondition>() {
        @Override
        public <T> DataResult<Pair<ModuleCondition, T>> decode(DynamicOps<T> ops, T input) {
            Pair<List<ModuleCondition>, T> result = Codec.list(ConditionManager.CONDITION_CODEC).decode(ops, ops.getMap(input).getOrThrow().get("conditions")).getOrThrow();
            return DataResult.success(new Pair(new AndCondition(result.getFirst()), result.getSecond()));
        }

        @Override
        public <T> DataResult<T> encode(ModuleCondition input, DynamicOps<T> ops, T prefix) {
            return null;
        }
    };

    public AndCondition() {

    }

    public AndCondition(List<ModuleCondition> conditions) {
        this.conditions = conditions;
    }

    @Override
    public boolean isAllowed(ConditionManager.ConditionContext conditionContext) {
        boolean isAllowed = true;
        for (ModuleCondition condition : conditions) {
            if (!condition.isAllowed(conditionContext)) {
                isAllowed = false;
            }
        }
        return isAllowed;
    }
}
