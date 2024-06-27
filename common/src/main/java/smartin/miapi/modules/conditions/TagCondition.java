package smartin.miapi.modules.conditions;

import com.google.gson.JsonElement;
import smartin.miapi.modules.properties.TagProperty;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.List;
import java.util.Map;
import net.minecraft.network.chat.Component;

public class TagCondition implements ModuleCondition {
    public String tag = "";
    Component onFalse = null;

    public TagCondition() {

    }

    public TagCondition(String tag) {
        this.tag = tag;
    }

    @Override
    public boolean isAllowed(ConditionManager.ConditionContext conditionContext) {
        if(conditionContext instanceof ConditionManager.ModuleConditionContext moduleConditionContext) {
            Map<ModuleProperty, JsonElement> propertyMap = moduleConditionContext.propertyMap;
            List<Component> reasons = moduleConditionContext.reasons;
            reasons.add(onFalse);
            if (TagProperty.getTags(propertyMap).contains(tag)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ModuleCondition load(JsonElement element) {
        TagCondition condition = new TagCondition(element.getAsJsonObject().get("tag").getAsString());
        condition.onFalse = ModuleProperty.getText(element.getAsJsonObject(), "error", Component.translatable("miapi.condition.tag.error"));
        return new TagCondition(element.getAsJsonObject().get("tag").getAsString());
    }
}
