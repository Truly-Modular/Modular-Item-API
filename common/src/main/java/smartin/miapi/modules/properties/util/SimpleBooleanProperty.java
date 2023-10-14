package smartin.miapi.modules.properties.util;

import com.google.gson.JsonElement;
import net.minecraft.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.cache.ModularItemCache;

public class SimpleBooleanProperty implements ModuleProperty {
    private final String KEY_PRIVATE;
    public SimpleBooleanProperty property;
    private final boolean defaultValueSaved;

    protected SimpleBooleanProperty(String id, boolean defaultValue) {
        KEY_PRIVATE = id;
        defaultValueSaved = defaultValue;
        property = this;
        ModularItemCache.setSupplier(KEY_PRIVATE, stack -> property.isTruePrivate(stack));
    }

    private Boolean isTruePrivate(ItemStack stack) {
        JsonElement element = ItemModule.getMergedProperty(stack, property);
        if (element != null) {
            try {
                return element.getAsBoolean();
            } catch (Exception e) {
                Miapi.LOGGER.warn("Error during PropertyResolve ", e);
            }
        }
        return defaultValueSaved;
    }

    public boolean isTrue(ItemStack stack) {
        Boolean value = (Boolean) ModularItemCache.get(stack, KEY_PRIVATE);
        if (value != null) {
            return value.booleanValue();
        }
        return defaultValueSaved;
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        data.getAsBoolean();
        return true;
    }

    @Override
    public JsonElement merge(JsonElement old, JsonElement toMerge, MergeType type) {
        if (type == MergeType.SMART || type == MergeType.EXTEND) {
            return old;
        } else if (type == MergeType.OVERWRITE) {
            return toMerge;
        }
        return old;
    }
}
