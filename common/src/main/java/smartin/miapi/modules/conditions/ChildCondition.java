package smartin.miapi.modules.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import smartin.miapi.modules.ModuleInstance;

import java.util.Optional;

public class ChildCondition implements ModuleCondition {
    public static Codec<ChildCondition> CODEC = RecordCodecBuilder.create((instance) ->
            instance.group(
                    ConditionManager.CONDITION_CODEC.fieldOf("condition")
                            .forGetter(ChildCondition::getCondition)
            ).apply(instance, ChildCondition::new));
    public ModuleCondition condition;

    public ChildCondition() {

    }

    @SuppressWarnings("unchecked")
    public <T extends ModuleCondition > T getCondition(){
        return (T) condition;
    }

    private ChildCondition(ModuleCondition condition) {
        this.condition = condition;
    }

    @Override
    public boolean isAllowed(ConditionManager.ConditionContext conditionContext) {
        Optional<ModuleInstance> optional = conditionContext.getContext(ConditionManager.MODULE_CONDITION_CONTEXT);
        if (optional.isPresent()) {
            ModuleInstance moduleInstance = optional.get();
            for (ModuleInstance otherInstance : moduleInstance.getSubModuleMap().values()) {
                assert otherInstance.getParent() != null;
                ConditionManager.ConditionContext copiedContext = conditionContext.copy();
                copiedContext.setContext(ConditionManager.MODULE_CONDITION_CONTEXT, otherInstance);
                copiedContext.setContext(ConditionManager.MODULE_PROPERTIES, otherInstance.properties);
                if (condition.isAllowed(copiedContext)) {
                    return true;
                }
            }
        }
        return false;
    }
}
