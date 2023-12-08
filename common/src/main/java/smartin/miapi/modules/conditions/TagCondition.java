package smartin.miapi.modules.conditions;

import com.google.gson.JsonElement;
import net.minecraft.text.Text;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.TagProperty;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.List;
import java.util.Map;

public class TagCondition implements ModuleCondition {
    public String tag = "";

    public TagCondition() {

    }

    public TagCondition(String tag) {
        this.tag = tag;
    }

    @Override
    public boolean isAllowed(ConditionManager.ConditionContext conditionContext) {
        if(conditionContext instanceof ConditionManager.ModuleConditionContext moduleConditionContext) {
            Map<ModuleProperty, JsonElement> propertyMap = moduleConditionContext.propertyMap;
            List<Text> reasons = moduleConditionContext.reasons;
            if (TagProperty.getTags(propertyMap).contains(tag)) {
                return true;
            }
            reasons.add(Text.translatable(Miapi.MOD_ID + ".condition.tag.error"));
        }
        return false;
    }

    @Override
    public ModuleCondition load(JsonElement element) {
        return new TagCondition(element.getAsJsonObject().get("tag").getAsString());
    }
}
