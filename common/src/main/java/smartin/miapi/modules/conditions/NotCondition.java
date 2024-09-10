package smartin.miapi.modules.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import smartin.miapi.Miapi;
/**
 * @header Not Condition
 * @description_start
 * this condition inverses another condition
 * @desciption_end
 * @path /data_types/condition/not
 * @data type:not
 * @data condition:the sub condition to be inversed
 */
public class NotCondition implements ModuleCondition {
    public static Codec<NotCondition> CODEC = RecordCodecBuilder.create((instance) ->
            instance.group(
                    ConditionManager.CONDITION_CODEC.fieldOf("condition")
                            .forGetter(NotCondition::getCondition),
                    ComponentSerialization.CODEC
                            .optionalFieldOf("error", Component.translatable(Miapi.MOD_ID + ".error"))
                            .forGetter((condition) -> condition.error)
            ).apply(instance, NotCondition::new));

    ModuleCondition conditions;
    Component error = null;

    public NotCondition() {

    }

    public NotCondition(ModuleCondition conditions, Component error) {
        this.conditions = conditions;
        this.error = error;
    }

    @SuppressWarnings("unchecked")
    public <T extends ModuleCondition > T getCondition(){
        return (T) conditions;
    }

    @Override
    public boolean isAllowed(ConditionManager.ConditionContext conditionContext) {
        if (!conditions.isAllowed(conditionContext)) {
            conditionContext.failReasons.add(error);
            return true;
        }
        return false;
    }
}
