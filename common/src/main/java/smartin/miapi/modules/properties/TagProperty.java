package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import net.minecraft.item.ItemStack;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TagProperty implements ModuleProperty {
    public static final String KEY = "tag";
    public static TagProperty property;

    public TagProperty() {
        property = this;
    }


    public static List<String> getTags(ItemStack itemStack) {
        return getTags(ItemModule.getMergedProperty(itemStack, property));
    }

    public static List<String> getTags(ItemModule.ModuleInstance instance) {
        return getTags(instance.getProperties());
    }

    public static List<String> getTags(Map<ModuleProperty, JsonElement> map) {
        return getTags(map.get(property));
    }

    public static List<String> getTags(JsonElement data) {
        List<String> tags = new ArrayList<>();
        if (data != null) {
            data.getAsJsonArray().forEach(element -> {
                tags.add(element.getAsString());
            });
        }
        return tags;
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        data.getAsJsonArray().forEach(element -> {
            element.getAsString();
        });
        return false;
    }

    @Override
    public JsonElement merge(JsonElement old, JsonElement toMerge, MergeType type) {
        switch (type) {
            case SMART, EXTEND -> {
                JsonElement element = old.deepCopy();
                element.getAsJsonArray().addAll(toMerge.getAsJsonArray());
                return element;
            }
            case OVERWRITE -> {
                return toMerge;
            }
        }
        return old;
    }
}
