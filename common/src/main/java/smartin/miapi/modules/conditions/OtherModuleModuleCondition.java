package smartin.miapi.modules.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ModuleInstance;

import java.util.Optional;

/**
 * @header Other Module
 * @description_start
 * this condition moves the context to any other module of the item
 * @desciption_end
 * @path /data_types/condition/other_module
 * @data type:other_module
 * @data condition:sub Condition to be tested on all other modules of the item
 */
public class OtherModuleModuleCondition implements ModuleCondition {
    public static Codec<OtherModuleModuleCondition> CODEC = RecordCodecBuilder.create((instance) ->
            instance.group(
                    ConditionManager.CONDITION_CODEC.fieldOf("condition")
                            .forGetter(OtherModuleModuleCondition::getCondition),
                    ComponentSerialization.CODEC
                            .optionalFieldOf("error", Component.translatable(Miapi.MOD_ID + ".error"))
                            .forGetter((condition) -> condition.error)
            ).apply(instance, OtherModuleModuleCondition::new));
    public ModuleCondition condition;
    public Component error;

    private OtherModuleModuleCondition(ModuleCondition module, Component error) {
        this.condition = module;
        this.error = error;
    }

    @Override
    public boolean isAllowed(ConditionManager.ConditionContext conditionContext) {
        Optional<ModuleInstance> optional = conditionContext.getContext(ConditionManager.MODULE_CONDITION_CONTEXT);
        if (optional.isPresent()) {
            ModuleInstance moduleInstance = optional.get();
            for (ModuleInstance otherInstance : moduleInstance.getRoot().allSubModules()) {
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

    @SuppressWarnings("unchecked")
    public <T extends ModuleCondition> T getCondition() {
        return (T) condition;
    }

}
