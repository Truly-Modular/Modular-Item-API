package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;

/**
 * Allows the set Itemtags via a Properterty (relies on {@link ItemStack#is(TagKey)}
 */
public class FakeItemTagProperty implements ModuleProperty {
    public static final String KEY = "fake_item_tag";
    public static FakeItemTagProperty property;

    public FakeItemTagProperty() {
        property = this;
    }


    public static List<String> getTags(ItemStack itemStack) {
        return getTags(ItemModule.getMergedProperty(itemStack, property));
    }

    public static boolean hasTag(ResourceLocation identifier, ItemStack itemStack) {
        return getTags(itemStack).contains(identifier.toString());
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
