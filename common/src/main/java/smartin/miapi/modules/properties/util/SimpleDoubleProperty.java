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
    public ModuleProperty property;
    protected String privateKey;

    protected SimpleDoubleProperty(String key) {
        property = this;
        privateKey = key;
        ModularItemCache.setSupplier(key, (itemstack) -> {
            return createValue(itemstack, property);
        });
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

    private static Double createValue(ItemStack itemStack, ModuleProperty property) {
        double flexibility = 0;
        boolean hasFlexibility = false;
        for (ItemModule.ModuleInstance moduleInstance : ItemModule.getModules(itemStack).allSubModules()) {
            Double value = getValue(moduleInstance, property);
            if(value!=null){
                flexibility += value;
                hasFlexibility = true;
            }
        }
        if (!hasFlexibility) {
            return null;
        }
        return flexibility;
    }

    public boolean hasValue(ItemStack itemStack) {
        return getValue(itemStack) != null;
    }

    private static Double getValue(ItemModule.ModuleInstance instance, ModuleProperty property) {
        JsonElement element = instance.getProperties().get(property);
        if (element == null)
            return null;
        try {
            return element.getAsDouble();
        } catch (Exception e) {
            return StatResolver.resolveDouble(element.getAsString(), instance);
        }
    }

    @Nullable
    public Double getValue(ItemStack itemStack) {
        return (Double) ModularItemCache.get(itemStack, privateKey);
    }

    public double getValueSafe(ItemStack itemStack) {
        Double value = getValue(itemStack);
        return value == null ? 0 : value;
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
