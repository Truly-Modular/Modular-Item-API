package smartin.miapi.modules.conditions;

import com.google.gson.JsonElement;
import net.minecraft.network.chat.Component;
import smartin.miapi.modules.properties.util.ModuleProperty;

public class NotCondition implements ModuleCondition {
    ModuleCondition conditions;
    Component onFalse = null;

    public NotCondition() {

    }

    public NotCondition(ModuleCondition conditions) {
        this.conditions = conditions;
    }

    @Override
    public boolean isAllowed(ConditionManager.ConditionContext conditionContext) {
        if (!conditions.isAllowed(conditionContext)) {
            conditionContext.getReasons().add(onFalse);
            return true;
        }
        return false;
    }

    @Override
    public ModuleCondition load(JsonElement element) {
        NotCondition notCondition = new NotCondition(ConditionManager.get(element.getAsJsonObject().get("condition")));
        notCondition.onFalse = ModuleProperty.getText(element.getAsJsonObject(), "error", Component.translatable("miapi.crafting_condition.false"));
        return notCondition;
    }
}
