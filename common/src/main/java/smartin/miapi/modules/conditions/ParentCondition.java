package smartin.miapi.modules.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ModuleInstance;

import java.util.Optional;

public class ParentCondition implements ModuleCondition {
    public static Codec<ParentCondition> CODEC = RecordCodecBuilder.create((instance) ->
            instance.group(
                    ConditionManager.CONDITION_CODEC.fieldOf("conditions")
                            .forGetter(ParentCondition::getCondition),
                    ComponentSerialization.CODEC
                            .optionalFieldOf("error", Component.translatable(Miapi.MOD_ID + ".error"))
                            .forGetter((condition) -> condition.error)
            ).apply(instance, ParentCondition::new));
    public ModuleCondition condition;
    public Component error;

    private ParentCondition(ModuleCondition module, Component error) {
        this.condition = module;
        this.error = error;
    }

    @Override
    public boolean isAllowed(ConditionManager.ConditionContext conditionContext) {
        Optional<ModuleInstance> optional = conditionContext.getContext(ConditionManager.MODULE_CONDITION_CONTEXT);
        if (optional.isPresent()) {
            ModuleInstance moduleInstance = optional.get();
            if (moduleInstance.getParent() != null) {
                ConditionManager.ConditionContext copiedContext = conditionContext.copy();
                copiedContext.setContext(ConditionManager.MODULE_CONDITION_CONTEXT, moduleInstance.getParent());
                copiedContext.setContext(ConditionManager.MODULE_PROPERTIES, moduleInstance.getParent().properties);
                return condition.isAllowed(copiedContext);
            }
        }
        return false;
    }


    @SuppressWarnings("unchecked")
    public <T extends ModuleCondition> T getCondition() {
        return (T) condition;
    }
}
