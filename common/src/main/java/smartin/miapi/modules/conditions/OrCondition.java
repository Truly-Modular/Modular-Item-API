package smartin.miapi.modules.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

/**
 * @header Or Condition
 * @description_start
 * this condition requires one sub conditions to be true
 * @desciption_end
 * @path /data_types/condition/or_condition
 * @data type:or
 * @data conditions:sub Conditions to be checked
 */
public class OrCondition implements ModuleCondition {
    public static Codec<OrCondition> CODEC = RecordCodecBuilder.create((instance) ->
            instance.group(
                    Codec.list(ConditionManager.CONDITION_CODEC).fieldOf("conditions")
                            .forGetter((condition) -> (List) condition.conditions)
            ).apply(instance, OrCondition::new));

    List<? extends ModuleCondition> conditions;

    public OrCondition() {

    }

    public OrCondition(List<? extends ModuleCondition> conditions) {
        this.conditions = conditions;
    }

    @Override
    public boolean isAllowed(ConditionManager.ConditionContext conditionContext) {
        boolean isAllowed = false;
        for (ModuleCondition condition : conditions) {
            if (condition.isAllowed(conditionContext)) {
                isAllowed = true;
            }
        }
        return isAllowed;
    }
}
