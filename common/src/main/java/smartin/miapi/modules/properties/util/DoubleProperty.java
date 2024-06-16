package smartin.miapi.modules.properties.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.item.modular.VisualModularItem;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.cache.ModularItemCache;

import java.util.ArrayList;
import java.util.List;

public abstract class DoubleProperty implements ModuleProperty {
    public ModuleProperty property;
    protected String privateKey;
    public double baseValue = 0;
    public boolean allowVisualOnly = false;

    protected DoubleProperty(String key) {
        property = this;
        privateKey = key;
        ModularItemCache.setSupplier(key, (itemstack) -> createValue(itemstack, property));
    }

    public boolean isModularItem(ItemStack itemStack) {
        if (allowVisualOnly) {
            return itemStack.getItem() instanceof VisualModularItem;
        }
        return itemStack.getItem() instanceof ModularItem;
    }

    public abstract Double getValue(ItemStack stack);

    public abstract double getValueSafe(ItemStack stack);

    @Override
    public JsonElement merge(JsonElement old, JsonElement toMerge, MergeType type) {
        if (type == MergeType.SMART || type == MergeType.EXTEND) {
            List<JsonElement> jsonElements = new ArrayList<>();
            addToList(jsonElements, old);
            addToList(jsonElements, toMerge);
            JsonArray jsonArray = new JsonArray();
            for (JsonElement element : jsonElements) {
                jsonArray.add(element);
            }
            return jsonArray;
        } else if (type == MergeType.OVERWRITE) {
            return toMerge;
        }
        return old;
    }

    private void addToList(List<JsonElement> elements, JsonElement toAdd) {
        if (toAdd.isJsonArray()) {
            for (JsonElement element : toAdd.getAsJsonArray()) {
                elements.add(element);
            }
        } else {
            elements.add(toAdd);
        }
    }

    private Double createValue(ItemStack itemStack, ModuleProperty property) {
        double value = baseValue;
        boolean hasValue = false;
        List<Double> addition = new ArrayList<>();
        List<Double> multiplyBase = new ArrayList<>();
        List<Double> multiplyTotal = new ArrayList<>();
        if (!isModularItem(itemStack)) {
            return null;
        }
        for (ModuleInstance moduleInstance : ItemModule.getModules(itemStack).allSubModules()) {
            JsonElement element = moduleInstance.getProperties().get(property);
            if (element != null) {
                if (element.isJsonArray()) {
                    for (JsonElement innerElement : element.getAsJsonArray()) {
                        hasValue = true;
                        Operation operation = new Operation(innerElement, moduleInstance);
                        switch (operation.attributeOperation) {
                            case ADDITION -> addition.add(operation.solve());
                            case MULTIPLY_BASE -> multiplyBase.add(operation.solve());
                            case MULTIPLY_TOTAL -> multiplyTotal.add(operation.solve());
                        }
                    }
                } else {
                    hasValue = true;
                    Operation operation = new Operation(element, moduleInstance);
                    switch (operation.attributeOperation) {
                        case ADDITION -> addition.add(operation.solve());
                        case MULTIPLY_BASE -> multiplyBase.add(operation.solve());
                        case MULTIPLY_TOTAL -> multiplyTotal.add(operation.solve());
                    }
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
        if (hasValue) {
            if (Double.isNaN(value)) {
                return 0d;
            }
            return value;
        } else {
            return null;
        }
    }

    public Double getValueForModule(ModuleInstance moduleInstance, @Nullable Double fallback) {
        double value = 0;
        boolean hasValue = false;
        List<Double> addition = new ArrayList<>();
        List<Double> multiplyBase = new ArrayList<>();
        List<Double> multiplyTotal = new ArrayList<>();
        JsonElement element = moduleInstance.getProperties().get(this);
        if (element != null) {
            if (element.isJsonArray()) {
                for (JsonElement innerElement : element.getAsJsonArray()) {
                    hasValue = true;
                    Operation operation = new Operation(innerElement, moduleInstance);
                    switch (operation.attributeOperation) {
                        case ADDITION -> addition.add(operation.solve());
                        case MULTIPLY_BASE -> multiplyBase.add(operation.solve());
                        case MULTIPLY_TOTAL -> multiplyTotal.add(operation.solve());
                    }
                }
            } else {
                hasValue = true;
                Operation operation = new Operation(element, moduleInstance);
                switch (operation.attributeOperation) {
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
        if (hasValue) {
            return value;
        } else {
            return fallback;
        }
    }

    public boolean hasValue(ItemStack itemStack) {
        return getValueRaw(itemStack) != null;
    }

    /**
     * for curious support
     */
    public double getForEntityNonHand(LivingEntity living) {
        List<ItemStack> itemsNotInSecondIterable = new ArrayList<>();
        for (ItemStack item : living.getItemsEquipped()) {
            boolean found = false;
            for (ItemStack secondItem : living.getHandItems()) {
                if (item.equals(secondItem)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                itemsNotInSecondIterable.add(item);
            }
        }
        return getForItems(itemsNotInSecondIterable);
    }

    public double getForItems(Iterable<ItemStack> itemStacks) {
        double mergedValue = 0;
        for (ItemStack armorItem : itemStacks) {
            mergedValue += getValueSafe(armorItem);
        }
        return mergedValue;
    }

    @Nullable
    public Double getValueRaw(ItemStack itemStack) {
        return ModularItemCache.getRaw(itemStack, privateKey);
    }

    public double getValueSafeRaw(ItemStack itemStack) {
        if(allowVisualOnly){
            return ModularItemCache.getVisualOnlyCache(itemStack, privateKey, Double.valueOf(0));
        }
        return ModularItemCache.get(itemStack, privateKey, Double.valueOf(0));
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        new Operation(data, new ModuleInstance(ItemModule.empty));
        return true;
    }

    private static class Operation {
        public EntityAttributeModifier.Operation attributeOperation;
        public String value;
        public ModuleInstance instance;

        public Operation(JsonElement toLoad, ModuleInstance instance) {
            if (toLoad.isJsonObject()) {
                JsonObject object = toLoad.getAsJsonObject();
                this.attributeOperation = getOperation(object.get("operation").getAsString());
                this.value = object.get("value").getAsString();
            } else {
                attributeOperation = EntityAttributeModifier.Operation.ADDITION;
                if (toLoad.getAsJsonPrimitive().isBoolean()) {
                    if (toLoad.getAsBoolean()) {
                        value = "1.0";
                    } else {
                        value = "0";
                    }
                } else if (toLoad.getAsJsonPrimitive().isNumber()) {
                    value = Double.toString(toLoad.getAsDouble());
                } else {
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
        return switch (operationString) {
            case "*" -> EntityAttributeModifier.Operation.MULTIPLY_BASE;
            case "**" -> EntityAttributeModifier.Operation.MULTIPLY_TOTAL;
            default -> EntityAttributeModifier.Operation.ADDITION;
        };
    }
}
