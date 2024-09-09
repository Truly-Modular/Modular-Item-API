package smartin.miapi.modules.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
/**
 * @header And Condition
 * @description_start
 * this condition requires all sub conditions to be true
 * @desciption_end
 * @path /data_types/condition/and_condition
 * @data type:and
 * @data conditions:sub Conditions to be checked
 */
public class AndCondition implements ModuleCondition {
    public static Codec<AndCondition> CODEC = RecordCodecBuilder.create((instance) ->
            instance.group(
                    Codec.list(ConditionManager.CONDITION_CODEC).fieldOf("conditions")
                            .forGetter((condition) -> (List) condition.conditions)
            ).apply(instance,  AndCondition::new));

    List<? extends ModuleCondition> conditions;

    public AndCondition(List<? extends ModuleCondition> conditions) {
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
