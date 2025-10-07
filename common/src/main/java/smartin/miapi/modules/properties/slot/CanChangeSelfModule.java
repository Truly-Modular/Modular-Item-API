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
 * This property defines whether a module can be changed itself based on specific conditions.
 * It is useful when certain crafting or upgrade actions require validation before allowing a module
 * to be modified directly.
 *
 * @header Can Change Self Module Property
 * @path /data_types/properties/slot/allow_change_self
 * @description_start
 * The Can Change Self Module Property validates if a module itself can be changed by checking specified conditions.
 * If the conditions are not met, the change is disallowed, and a failure reason is communicated. This is particularly
 * important in crafting or upgrading scenarios where a module’s direct modification is restricted.
 * @description_end
 * @data allow_change_self: A condition that determines whether a module can be changed.
 * @data `condition`: The condition that must be met to allow changing the module.
 *
 * @see ModuleCondition
 * @see ConditionManager
 */
public class CanChangeSelfModule extends CodecProperty<ModuleCondition> {
    public static final ResourceLocation KEY = Miapi.id("allow_change_self");

    public CanChangeSelfModule() {
        super(ConditionManager.CONDITION_CODEC_DIRECT);

        // Register event to check if the module itself can be changed
        CraftingConditionProperty.CAN_CRAFT_SELECT_EVENT.register((slot, module, conditionContext) -> {
            if (slot != null && slot.inSlot!=null) {
                // Check the selected module directly, instead of its parent or submodules
                if (!canChangeSelf(slot.inSlot, conditionContext)) {
                    conditionContext.failReasons.add(Component.translatable("miapi.crafting_condition.cant_change_self"));
                    return EventResult.interruptFalse();
                }
            }
            return EventResult.pass();
        });
    }

    public boolean canChangeSelf(ModuleInstance moduleInstance, ConditionManager.ConditionContext context) {
        return getData(moduleInstance).map(condition -> condition.isAllowed(context)).orElse(true);
    }

    @Override
    public ModuleCondition merge(ModuleCondition left, ModuleCondition right, MergeType mergeType) {
        if (MergeType.EXTEND.equals(mergeType)) {
            return left;
        }
        return right;
    }
}
