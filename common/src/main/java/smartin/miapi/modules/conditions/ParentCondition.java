package smartin.miapi.modules.conditions;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import smartin.miapi.modules.ModuleInstance;

import java.util.Optional;

public class ParentCondition implements ModuleCondition {
    public static Codec<ModuleCondition> CODEC = new Codec<ModuleCondition>() {
        @Override
        public <T> DataResult<Pair<ModuleCondition, T>> decode(DynamicOps<T> ops, T input) {
            Pair<ModuleCondition, T> result = ConditionManager.CONDITION_CODEC.decode(ops, ops.getMap(input).getOrThrow().get("conditions")).getOrThrow();
            return DataResult.success(new Pair(new ParentCondition(result.getFirst()), result.getSecond()));
        }

        @Override
        public <T> DataResult<T> encode(ModuleCondition input, DynamicOps<T> ops, T prefix) {
            return null;
        }
    };
    public ModuleCondition condition;

    private ParentCondition(ModuleCondition module) {
        this.condition = module;
    }

    @Override
    public boolean isAllowed(ConditionManager.ConditionContext conditionContext) {
        Optional<ModuleInstance> optional = conditionContext.getContext(ConditionManager.MODULE_CONDITION_CONTEXT);
        if (optional.isPresent()) {
            ModuleInstance moduleInstance = optional.get();
            if (moduleInstance.parent != null) {
                ConditionManager.ConditionContext copiedContext = conditionContext.copy();
                copiedContext.setContext(ConditionManager.MODULE_CONDITION_CONTEXT, moduleInstance.parent);
                copiedContext.setContext(ConditionManager.MODULE_PROPERTIES, moduleInstance.parent.properties);
                return condition.isAllowed(copiedContext);
            }
        }
        return false;
    }
}
