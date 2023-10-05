package smartin.miapi.modules.properties.util;

import com.google.gson.JsonElement;
import net.minecraft.item.ItemStack;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.cache.ModularItemCache;

import java.util.Objects;

public class BooleanProperty implements ModuleProperty {
    private static String KEY;
    public static BooleanProperty property;

    protected BooleanProperty(String id) {
        ModularItemCache.setSupplier(id, (BooleanProperty::isTruePrivate));
        KEY = id;
        property = this;
    }

    private static boolean isTruePrivate(ItemStack stack) {
        JsonElement element = ItemModule.getMergedProperty(stack, property);
        if (element != null) {
            return element.getAsBoolean();
        }
        return false;
    }

    public static boolean isTrue(ItemStack stack) {
        return (boolean) ModularItemCache.get(stack, KEY);
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        data.getAsBoolean();
        return true;
    }

    @Override
    public JsonElement merge(JsonElement old, JsonElement toMerge, MergeType type) {
        if (Objects.requireNonNull(type) == MergeType.SMART || type == MergeType.EXTEND) {
            return old;
        } else if (type == MergeType.OVERWRITE) {
            return toMerge;
        }
        return old;
    }
}
