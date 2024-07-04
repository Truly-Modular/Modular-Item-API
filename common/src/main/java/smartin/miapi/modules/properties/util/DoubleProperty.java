package smartin.miapi.modules.properties.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import com.redpxnda.nucleus.codec.behavior.CodecBehavior;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.cache.ModularItemCache;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class DoubleProperty extends CodecBasedProperty<List<DoubleProperty.Operation>> {
    public ModuleProperty<List<DoubleProperty.Operation>> property;
    public double baseValue = 0;
    public boolean allowVisualOnly = false;
    private String cacheKey;
    static Codec<List<Operation>> listCodec = Codec.list(AutoCodec.of(Operation.class).codec());
    static Codec<List<Operation>> codec = Codec.withAlternative(
            new Codec<>() {
                @Override
                public <T> DataResult<T> encode(List<Operation> input, DynamicOps<T> ops, T prefix) {
                    return listCodec.encode(input, ops, prefix);
                }

                @Override
                public <T> DataResult<Pair<List<Operation>, T>> decode(DynamicOps<T> ops, T input) {
                    Pair<String, T> stringTPair = Codec.STRING.decode(ops, input).getOrThrow();
                    List<Operation> operations = List.of(new Operation(stringTPair.getFirst()));
                    return DataResult.success(new Pair<>(operations, stringTPair.getSecond()));
                }
            },
            listCodec);

    protected DoubleProperty(String cacheKey) {
        super(codec);
        this.cacheKey = cacheKey + "_internal_double";
        property = this;
        ModularItemCache.setSupplier(cacheKey, (itemStack) ->
                Optional.ofNullable(resolve(getProperty(itemStack), baseValue)));
    }

    public List<DoubleProperty.Operation> initialize(List<DoubleProperty.Operation> property, ModuleInstance context) {
        property.forEach(operation -> operation.instance = context);
        return property;
    }

    public Optional<Double> getValue(ItemStack itemStack) {
        return ModularItemCache.get(itemStack, cacheKey, Optional.empty());
    }

    public static double resolve(List<Operation> operations, double baseValue, double fallback) {
        Double resolve = resolve(operations, baseValue);
        return resolve != null ? resolve : fallback;
    }

    public double getForItems(Iterable<ItemStack> itemStacks) {
        List<Operation> operations = new ArrayList<>();
        itemStacks.forEach(itemStack -> operations.addAll(getProperty(itemStack)));
        return resolve(operations, baseValue, baseValue);
    }

    @Nullable
    public static Double resolve(List<Operation> operations, double baseValue) {
        double value = baseValue;
        boolean hasValue = false;
        List<Double> addition = new ArrayList<>();
        List<Double> multiplyBase = new ArrayList<>();
        List<Double> multiplyTotal = new ArrayList<>();
        for (Operation operation : operations) {
            hasValue = true;
            switch (operation.attributeOperation) {
                case ADD_VALUE -> addition.add(operation.solve());
                case ADD_MULTIPLIED_BASE -> multiplyBase.add(operation.solve());
                case ADD_MULTIPLIED_TOTAL -> multiplyTotal.add(operation.solve());
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
                Miapi.LOGGER.error("could not correctly resolve Double Operations. this indicates a serious issue");
                return 0d;
            }
            return value;
        } else {
            return null;
        }
    }

    public List<Operation> merge(List<Operation> left, List<Operation> right, MergeType mergeType) {
        if (MergeType.OVERWRITE.equals(mergeType)) {
            return right;
        } else {
            List<Operation> operations = new ArrayList<>(left);
            operations.addAll(right);
            return operations;
        }
    }

    public static class Operation {
        @AutoCodec.Name("operation")
        @CodecBehavior.Override("operationCode")
        public AttributeModifier.Operation attributeOperation = AttributeModifier.Operation.ADD_VALUE;
        @AutoCodec.Name("value")
        public String value;
        @AutoCodec.Ignored
        public ModuleInstance instance;

        public static Codec<AttributeModifier.Operation> operationCodec = new Codec<>() {
            @Override
            public <T> DataResult<Pair<AttributeModifier.Operation, T>> decode(DynamicOps<T> ops, T input) {
                Pair<String, T> stringTPair = Codec.STRING.decode(ops, input).getOrThrow();
                AttributeModifier.Operation operations = getOperation(stringTPair.getFirst());
                return DataResult.success(new Pair<>(operations, stringTPair.getSecond()));
            }

            @Override
            public <T> DataResult<T> encode(AttributeModifier.Operation input, DynamicOps<T> ops, T prefix) {
                return Codec.STRING.encode(toCodecString(input), ops, prefix);
            }
        };

        public Operation(String value) {
            this.value = value;
        }

        public double solve() {
            return StatResolver.resolveDouble(value, instance);
        }

        private static AttributeModifier.Operation getOperation(String operationString) {
            return switch (operationString) {
                case "*" -> AttributeModifier.Operation.ADD_MULTIPLIED_BASE;
                case "**" -> AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;
                default -> AttributeModifier.Operation.ADD_VALUE;
            };
        }

        private static String toCodecString(AttributeModifier.Operation operation) {
            return switch (operation) {
                case ADD_MULTIPLIED_BASE -> "*";
                case ADD_MULTIPLIED_TOTAL -> "**";
                default -> "+";
            };
        }
    }
}
