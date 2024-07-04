package smartin.miapi.modules.conditions;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

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
            conditionContext.failReasons.add(onFalse);
            return true;
        }
        return false;
    }

    @Override
    public ModuleCondition load(JsonElement element) {
        NotCondition notCondition = new NotCondition(ConditionManager.get(element.getAsJsonObject().get("condition")));
        notCondition.onFalse = ComponentSerialization.CODEC.parse(JsonOps.INSTANCE,element.getAsJsonObject().get("error")).result().orElse(Component.translatable("miapi.crafting_condition.false"));
        return notCondition;
    }
}
