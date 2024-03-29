package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import dev.architectury.event.EventResult;
import net.minecraft.text.Text;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.conditions.ConditionManager;
import smartin.miapi.modules.properties.util.ModuleProperty;


public class CanChangeParentModule implements ModuleProperty {
    public static final String KEY = "allowChangeParent";

    public CanChangeParentModule() {
        super();
        CraftingConditionProperty.CRAFT_CONDITION_EVENT.register((slot, module, conditionContext) -> {
            if (slot != null && slot.inSlot != null && !module.isEmpty()) {
                for (ItemModule.ModuleInstance moduleInstance : slot.inSlot.subModules.values()) {
                    if (!canChangeParent(moduleInstance, conditionContext)) {
                        conditionContext.reasons.add(Text.translatable("miapi.crafting_condition.cant_change_parent"));
                        return EventResult.interruptFalse();
                    }
                }
            }
            return EventResult.pass();
        });
    }

    public boolean canChangeParent(ItemModule.ModuleInstance moduleInstance, ConditionManager.ModuleConditionContext context) {
        JsonElement element = this.getJsonElement(moduleInstance);
        if (element != null) {
            return ConditionManager.get(element).isAllowed(context);
        }
        return true;
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        return true;
    }
}
