package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import net.minecraft.item.ItemStack;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.registries.RegistryInventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Allows the Modules to define tags to identify eachother
 */
public class TagProperty implements ModuleProperty {
    public static final String KEY = "tag";
    public static TagProperty property;

    public TagProperty() {
        property = this;
    }


    public static List<String> getTags(ItemStack itemStack) {
        return getTags(ItemModule.getMergedProperty(itemStack, property));
    }

    public static List<String> getTags(ModuleInstance instance) {
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

    public static List<ItemModule> getModulesWithTag(String tag) {
        List<ItemModule> modules = new ArrayList<>();
        RegistryInventory.modules.getFlatMap().forEach((key, module) -> {
            if (getTags(module.getKeyedProperties()).contains(tag)) modules.add(module);
        });
        return modules;
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        data.getAsJsonArray().forEach(JsonElement::getAsString);
        return true;
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
