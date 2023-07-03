package smartin.miapi.modules.properties.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.cache.ModularItemCache;

import java.util.ArrayList;
import java.util.List;
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

    public abstract Double getValue(ItemStack stack);

    public abstract double getValueSafe(ItemStack stack);
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
        double value = 0;
        boolean hasFlexibility = false;
        List<Double> addition = new ArrayList<>();
        List<Double> multiplyBase = new ArrayList<>();
        List<Double> multiplyTotal = new ArrayList<>();
        for (ItemModule.ModuleInstance moduleInstance : ItemModule.getModules(itemStack).allSubModules()) {
            JsonElement element = moduleInstance.getProperties().get(property);
            if (element != null) {
                hasFlexibility = true;
                Operation operation = new Operation(element, moduleInstance);
                switch (operation.operation) {
                    case ADDITION -> addition.add(operation.solve());
                    case MULTIPLY_BASE -> multiplyBase.add(operation.solve());
                    case MULTIPLY_TOTAL -> multiplyTotal.add(operation.solve());
                }
            }
        }
        for (Double currentValue : addition) {
            value += currentValue;
        }
        double multiplier = 1.0;
        for (Double currentValue : multiplyBase) {
            multiplier += currentValue;
        }
        value = value * multiplier;
        for (Double currentValue : multiplyTotal) {
            value = currentValue * value;
        }
        if (hasFlexibility) {
            return value;
        } else {
            return null;
        }
    }

    public boolean hasValue(ItemStack itemStack) {
        return getValueRaw(itemStack) != null;
    }

    @Nullable
    public Double getValueRaw(ItemStack itemStack) {
        return (Double) ModularItemCache.get(itemStack, privateKey);
    }

    public double getValueSafeRaw(ItemStack itemStack) {
        Double value = getValueRaw(itemStack);
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

    private static class Operation {
        public EntityAttributeModifier.Operation operation;
        public String value;
        public ItemModule.ModuleInstance instance;

        public Operation(JsonElement toLoad, ItemModule.ModuleInstance instance) {
            if (toLoad.isJsonObject()) {
                JsonObject object = toLoad.getAsJsonObject();
                this.operation = getOperation(object.get("operation").getAsString());
                this.value = object.get("value").getAsString();
            } else {
                operation = EntityAttributeModifier.Operation.ADDITION;
                try {
                    value = Double.toString(toLoad.getAsDouble());
                } catch (Exception surpressed) {
                    value = toLoad.getAsString();
                }
            }
            this.instance = instance;
        }

        public double solve() {
            return StatResolver.resolveDouble(value, instance);
        }

    }

    private static EntityAttributeModifier.Operation getOperation(String operationString) {
        switch (operationString) {
            case "*":
                return EntityAttributeModifier.Operation.MULTIPLY_BASE;
            case "**":
                return EntityAttributeModifier.Operation.MULTIPLY_TOTAL;
            default:
                return EntityAttributeModifier.Operation.ADDITION;
        }
    }
}
