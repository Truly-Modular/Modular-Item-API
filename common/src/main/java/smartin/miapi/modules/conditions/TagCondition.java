package smartin.miapi.modules.conditions;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import smartin.miapi.modules.properties.TagProperty;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        Optional<Map<ModuleProperty<?>, Object>> propertyMapOptional = conditionContext.getContext(ConditionManager.MODULE_PROPERTIES);
        if (propertyMapOptional.isPresent()) {
            Map<ModuleProperty<?>, Object> propertyMap = propertyMapOptional.get();
            List<String> tags = (List<String>) propertyMap.get(TagProperty.property);
            if (tags != null) {
                if (tags.contains(tag)) {
                    return true;
                }
            }
            conditionContext.failReasons.add(onFalse);
        }
        return false;
    }

    @Override
    public ModuleCondition load(JsonElement element) {
        TagCondition condition = new TagCondition(element.getAsJsonObject().get("tag").getAsString());
        condition.onFalse = ComponentSerialization.CODEC.parse(JsonOps.INSTANCE, element.getAsJsonObject().get("item")).result().orElse(Component.translatable("miapi.condition.tag.error"));
        return new TagCondition(element.getAsJsonObject().get("tag").getAsString());
    }
}
