package smartin.miapi.modules.properties.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import com.redpxnda.nucleus.codec.behavior.CodecBehavior;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.modules.ModuleInstance;

import java.util.ArrayList;
import java.util.List;

public class DoubleOperationResolvable {
    static Codec<List<Operation>> listCodec = Codec.list(AutoCodec.of(Operation.class).codec());
    public static Codec<DoubleOperationResolvable> CODEC = Codec.withAlternative(
            new Codec<>() {
                @Override
                public <T> DataResult<T> encode(DoubleOperationResolvable input, DynamicOps<T> ops, T prefix) {
                    return listCodec.encode(input.operations, ops, prefix);
                }

                @Override
                public <T> DataResult<Pair<DoubleOperationResolvable, T>> decode(DynamicOps<T> ops, T input) {
                    Pair<String, T> stringTPair = Codec.STRING.decode(ops, input).getOrThrow();
                    List<Operation> operationList = List.of(new Operation(stringTPair.getFirst()));
                    return DataResult.success(new Pair<>(new DoubleOperationResolvable(operationList), stringTPair.getSecond()));
                }
            },
            new Codec<>() {
                @Override
                public <T> DataResult<T> encode(DoubleOperationResolvable input, DynamicOps<T> ops, T prefix) {
                    return listCodec.encode(input.operations, ops, prefix);
                }

                @Override
                public <T> DataResult<Pair<DoubleOperationResolvable, T>> decode(DynamicOps<T> ops, T input) {
                    Pair<List<Operation>, T> pair = listCodec.decode(ops, input).getOrThrow((e) -> new RuntimeException(e + "could not decode double operations"));
                    return DataResult.success(new Pair<>(new DoubleOperationResolvable(pair.getFirst()), pair.getSecond()));
                }
            });

    public List<Operation> operations;
    Double cachedResult = null;

    public DoubleOperationResolvable(List<Operation> operations) {
        this.operations = operations;
    }

    public DoubleOperationResolvable initialize(ModuleInstance moduleInstance) {
        List<Operation> operationList = new ArrayList<>();
        operations.forEach(operation -> {
            Operation copiesOperation = new Operation(operation.value);
            copiesOperation.attributeOperation = operation.attributeOperation;
            copiesOperation.instance = moduleInstance;
            operationList.add(copiesOperation);
        });
        return new DoubleOperationResolvable(operationList);
    }

    public double evaluate(double baseValue, double fallback) {
        Double value = evaluate(baseValue);
        if (value == null) {
            return fallback;
        }
        return value;
    }

    public Double evaluate(double baseValue) {
        if (cachedResult == null) {
            cachedResult = resolve(operations, baseValue);
        }
        return cachedResult;
    }

    public static Double resolve(List<Operation> operations, double baseValue, double fallback) {
        Double value = resolve(operations, baseValue);
        if (value == null) {
            return value;
        }
        return fallback;
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

    public DoubleOperationResolvable merge(DoubleOperationResolvable left, MergeType mergeType) {
        return merge(this, left, mergeType);
    }

    public static DoubleOperationResolvable merge(DoubleOperationResolvable left, DoubleOperationResolvable right, MergeType mergeType) {
        if (MergeType.OVERWRITE.equals(mergeType)) {
            return right;
        }
        List<Operation> operationList = new ArrayList<>(left.operations);
        operationList.addAll(right.operations);
        return new DoubleOperationResolvable(operationList);
    }

    public static class Operation {
        @AutoCodec.Name("operation")
        @CodecBehavior.Override("operationCode")
        public AttributeModifier.Operation attributeOperation = AttributeModifier.Operation.ADD_VALUE;
        @AutoCodec.Name("value")
        public String value;
        @AutoCodec.Ignored
        public ModuleInstance instance;

        @SuppressWarnings("")
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
