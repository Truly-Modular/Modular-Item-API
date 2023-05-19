package smartin.miapi.modules.properties.util;

import com.google.gson.JsonElement;
import net.minecraft.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.cache.ModularItemCache;

import javax.annotation.Nullable;
import java.util.Objects;

public abstract class SimpleDoubleProperty implements ModuleProperty {
    public static ModuleProperty property;
    protected static String privateKey;

    protected SimpleDoubleProperty(String key) {
        property = this;
        privateKey = key;
        ModularItemCache.setSupplier(key, (SimpleDoubleProperty::createValue));
    }

    @Override
    public JsonElement merge(JsonElement old, JsonElement toMerge, MergeType type) {
        if (Objects.requireNonNull(type) == MergeType.SMART || type == MergeType.EXTEND) {
            return Miapi.gson.toJsonTree(toMerge.getAsDouble() + old.getAsDouble());
        } else if (type == MergeType.OVERWRITE) {
            return toMerge;
        }
        return old;
    }

    private static Double createValue(ItemStack itemStack) {
        double flexibility = 0;
        boolean hasFlexibility = false;
        for (ItemModule.ModuleInstance moduleInstance : ItemModule.getModules(itemStack).allSubModules()) {
            flexibility += getValue(moduleInstance);
            hasFlexibility = true;
        }
        if (!hasFlexibility) {
            return null;
        }
        return flexibility;
    }

    public static boolean hasValue(ItemStack itemStack) {
        return getValue(itemStack) == null;
    }

    public static double getValue(ItemModule.ModuleInstance instance) {
        JsonElement element = instance.getProperties().get(property);
        if (element == null)
            return 0;
        try {
            return element.getAsDouble();
        } catch (Exception e) {
            return StatResolver.resolveDouble(element.getAsString(), instance);
        }
    }

    @Nullable
    public static Double getValue(ItemStack itemStack) {
        return (Double) ModularItemCache.get(itemStack, privateKey);
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        try {
            data.getAsDouble();
        } catch (Exception e) {
            StatResolver.resolveDouble(data.getAsString(), new ItemModule.ModuleInstance(ItemModule.empty));
        }
        return true;
    }
}
