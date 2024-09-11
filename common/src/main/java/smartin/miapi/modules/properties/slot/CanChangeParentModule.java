package smartin.miapi.modules.properties.slot;

import dev.architectury.event.EventResult;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.conditions.ConditionManager;
import smartin.miapi.modules.conditions.ModuleCondition;
import smartin.miapi.modules.properties.CraftingConditionProperty;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeType;

/**
 * This property defines whether a parent module can be changed based on specific conditions.
 * It is useful when certain crafting or upgrade actions require validation before allowing a module's parent to be altered.
 *
 * @header Can Change Parent Module Property
 * @path /data_types/properties/slot/allow_change_parent
 * @description_start
 * The Can Change Parent Module Property validates if a module's parent can be changed by checking specified conditions.
 * If the conditions are not met, the change is disallowed, and a failure reason is communicated. This is particularly
 * important in crafting or upgrading scenarios where a module's placement or modification depends on parent-child
 * relationships between modules.
 * @description_end
 * @data allow_change_parent: A condition that determines whether a parent module can be changed.
 * @data `condition`: The condition that must be met to allow changing the parent module.
 *
 * @see ModuleCondition
 * @see ConditionManager
 */

public class CanChangeParentModule extends CodecProperty<ModuleCondition> {
    public static final ResourceLocation KEY = Miapi.id("allow_change_parent");

    public CanChangeParentModule() {
        super(ConditionManager.CONDITION_CODEC_DIRECT);
        CraftingConditionProperty.CAN_CRAFT_SELECT_EVENT.register((slot, module, conditionContext) -> {
            if (slot != null && slot.inSlot != null && !module.isEmpty()) {
                for (ModuleInstance moduleInstance : slot.inSlot.getSubModuleMap().values()) {
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
