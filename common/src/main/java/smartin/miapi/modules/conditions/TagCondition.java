package smartin.miapi.modules.conditions;

import com.google.gson.JsonElement;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.TagProperty;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.Map;

public class TagCondition implements ModuleCondition {
    public String tag = "";

    public TagCondition() {

    }

    public TagCondition(String tag) {
        this.tag = tag;
    }

    @Override
    public boolean isAllowed(ItemModule.ModuleInstance moduleInstance, Map<ModuleProperty,JsonElement> propertyMap) {
        return TagProperty.getTags(propertyMap).contains(tag);
    }

    @Override
    public ModuleCondition load(JsonElement element) {
        return new TagCondition(element.getAsJsonObject().get("tag").getAsString());
    }
}
