package smartin.miapi.modules.properties;

import dev.architectury.event.EventResult;
import net.minecraft.network.chat.Component;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.conditions.ConditionManager;
import smartin.miapi.modules.conditions.ModuleCondition;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeType;


public class CanChangeParentModule extends CodecProperty<ModuleCondition> {
    public static final String KEY = "allow_change_parent";

    public CanChangeParentModule() {
        super(ConditionManager.CONDITION_CODEC_DIRECT);
        CraftingConditionProperty.CAN_CRAFT_SELECT_EVENT.register((slot, module, conditionContext) -> {
            if (slot != null && slot.inSlot != null && !module.isEmpty()) {
                for (ModuleInstance moduleInstance : slot.inSlot.subModules.values()) {
                    if (!canChangeParent(moduleInstance, conditionContext)) {
                        conditionContext.failReasons.add(Component.translatable("miapi.crafting_condition.cant_change_parent"));
                        return EventResult.interruptFalse();
                    }
                }
            }
            return EventResult.pass();
        });
    }

    public boolean canChangeParent(ModuleInstance moduleInstance, ConditionManager.ConditionContext context) {
        return getData(moduleInstance).map(moduleCondition -> moduleCondition.isAllowed(context)).orElse(true);
    }

    @Override
    public ModuleCondition merge(ModuleCondition left, ModuleCondition right, MergeType mergeType) {
        if (MergeType.EXTEND.equals(mergeType)) {
            return left;
        }
        return right;
    }
}
