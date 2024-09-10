package smartin.miapi.modules.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import smartin.miapi.Miapi;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.modules.ModuleInstance;

import java.util.Optional;

/**
 * @header Number Condition
 * @description_start
 * this condition inverses another condition
 * @desciption_end
 * @path /data_types/condition/number
 * @data type:number
 * @data condition:a Double Resolvable, if the result is higher than 0 is considered true
 */
public record NumberCondition(String condition, Component error) implements ModuleCondition {
    public static Codec<NumberCondition> CODEC = RecordCodecBuilder.create((instance) ->
            instance.group(
                    Codec.STRING.fieldOf("condition")
                            .forGetter(NumberCondition::condition),
                    ComponentSerialization.CODEC
                            .optionalFieldOf("error", Component.translatable(Miapi.MOD_ID + ".error"))
                            .forGetter((condition) -> condition.error)
            ).apply(instance, NumberCondition::new));

    @Override
    public boolean isAllowed(ConditionManager.ConditionContext conditionContext) {
        Optional<ModuleInstance> moduleInstance = conditionContext.getContext(ConditionManager.MODULE_CONDITION_CONTEXT);
        if (moduleInstance.isPresent()) {
            double result = StatResolver.resolveDouble(condition, moduleInstance.get());
            moduleInstance.get().clearCachesSoftOnlyThis();
            if (result > 0) {
                return true;
            }
            conditionContext.failReasons.add(error);
            return false;
        }
        return false;
    }
}
