package smartin.miapi.modules.properties.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import com.redpxnda.nucleus.codec.behavior.CodecBehavior;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import smartin.miapi.Miapi;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.modules.ModuleInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * An Attribute like Resolvable Double
 * !!REMINDER!! to call {@link DoubleOperationResolvable#initialize(ModuleInstance)} with its context ModuleInstance!
 * otherwise statresolving will crash out!
 * if the usage of {@link DoubleOperationResolvable#functionTransformer} is desired it should be set during initialize, as setting it in the constructor
 * would be negated by an Autocodec
 */
public class DoubleOperationResolvable {
    static Codec<Operation> autoCodec = AutoCodec.of(Operation.class).codec();
    static Codec<Operation> operationCodec = Codec.withAlternative(new Codec<Operation>() {
        @Override
        public <T> DataResult<T> encode(Operation input, DynamicOps<T> ops, T prefix) {
            return autoCodec.encode(input, ops, prefix);
        }

        @Override
        public <T> DataResult<Pair<Operation, T>> decode(DynamicOps<T> ops, T input) {
            DataResult<Pair<Boolean, T>> decodeBoolean = Codec.BOOL.decode(ops, input);
            if (decodeBoolean.isSuccess()) {
                Operation operation = new Operation(decodeBoolean.getOrThrow().getFirst() ? "1" : "-1");
                return DataResult.success(new Pair<>(operation, input));
            }
            DataResult<Pair<String, T>> result = Codec.STRING.decode(ops, input);
            if (result.isError()) {
                DataResult<Pair<Double, T>> doubleResult = Codec.DOUBLE.decode(ops, input);
                if (doubleResult.isSuccess()) {
                    Pair<Double, T> doubleTPair = doubleResult.getOrThrow();
                    return DataResult.success(new Pair<>(new Operation("" + doubleTPair.getFirst()), doubleTPair.getSecond()));
                }
                return DataResult.error(() -> "is neither a string nor a boolean or a number");
            }
            Pair<String, T> stringTPair = result.getOrThrow();
            return DataResult.success(new Pair<>(new Operation(stringTPair.getFirst()), stringTPair.getSecond()));
        }
    }, autoCodec);
    static Codec<List<Operation>> listCodec = Codec.list(operationCodec);
    public static Codec<DoubleOperationResolvable> CODEC = Codec.withAlternative(new Codec<>() {
        @Override
        public <T> DataResult<T> encode(DoubleOperationResolvable input, DynamicOps<T> ops, T prefix) {
            return listCodec.encode(input.operations, ops, prefix);
        }

        @Override
        public <T> DataResult<Pair<DoubleOperationResolvable, T>> decode(DynamicOps<T> ops, T input) {
            var result = operationCodec.decode(ops, input);
            if (result.isError()) {
                return DataResult.error(() -> "could not decode double operations");
            }
            Pair<Operation, T> pair = result.getOrThrow();
            return DataResult.success(new Pair<>(new DoubleOperationResolvable(List.of(pair.getFirst())), pair.getSecond()));
        }
    }, new Codec<>() {
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
    /**
     * use the function to set this value.
     */
    protected Function<Pair<String, ModuleInstance>, String> functionTransformer = (Pair::getFirst);
    Double cachedResult = null;
    double baseValue = 0.0;
    double fallback = 0.0;

    /**
     * @param fallback the value this will assume in resolve if no value was set at all, you can use the other resolve methods to not rely on it
     */
    public DoubleOperationResolvable(double fallback) {
        this.fallback = fallback;
        operations = new ArrayList<>();
    }

    protected DoubleOperationResolvable(List<Operation> operations) {
        this.operations = operations;
    }

    protected DoubleOperationResolvable(List<Operation> operations, Function<Pair<String, ModuleInstance>, String> functionTransformer) {
        this.operations = operations;
        this.functionTransformer = functionTransformer;
    }

    /**
     * resolves the value with preset basevalue and Fallback
     * The function internally caches to improve performance
     *
     * @return the resolved value.
     */
    public double getValue() {
        return evaluate(baseValue, fallback);
    }

    /**
     * use this function to set a new FunctionTransformer, it resets the cachedResult as well
     *
     * @param functionTransformer the new FunctionTransformer
     */
    public void setFunctionTransformer(Function<Pair<String, ModuleInstance>, String> functionTransformer) {
        this.functionTransformer = functionTransformer;
        this.cachedResult = null;
    }

    public Function<Pair<String, ModuleInstance>, String> getFunctionTransformer() {
        return functionTransformer;
    }

    /**
     * initializes this Resolvable with its Context ModuleInstance.
     *
     * @param moduleInstance
     * @return
     */
    public DoubleOperationResolvable initialize(ModuleInstance moduleInstance) {
        List<Operation> operationList = new ArrayList<>();
        if(operations!=null){
            operations.forEach(operation -> {
                Operation copiesOperation = new Operation(operation.value);
                copiesOperation.attributeOperation = operation.attributeOperation;
                copiesOperation.instance = moduleInstance;
                operation.value = functionTransformer.apply(new Pair<>(operation.value, moduleInstance));
                operationList.add(copiesOperation);
            });
        }
        DoubleOperationResolvable initialized = new DoubleOperationResolvable(operationList, functionTransformer);
        initialized.getValue();
        return initialized;
    }

    /**
     * @param baseValue this value is added at the start of the Operations
     * @param fallback  this value will be returned if no value was set
     * @return
     */
    public double evaluate(double baseValue, double fallback) {
        return evaluate(baseValue).orElse(fallback);
    }

    public Optional<Double> evaluate(double baseValue) {
        if (cachedResult == null) {
            resolve(operations, baseValue).ifPresent(result -> cachedResult = result);
        }
        return Optional.ofNullable(cachedResult);
    }

    public static double resolve(List<Operation> operations, double baseValue, double fallback) {
        return resolve(operations, baseValue).orElse(fallback);
    }

    public static Optional<Double> resolve(List<Operation> operations, double baseValue) {
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
            value = (currentValue + 1) * value;
        }
        if (hasValue) {
            if (Double.isNaN(value)) {
                Miapi.LOGGER.error("could not correctly resolve Double Operations. this indicates a serious issue");
                return Optional.empty();
            }
            return Optional.of(value);
        } else {
            return Optional.empty();
        }
    }

    public boolean isTrue() {
        return getValue() > 0;
    }

    public DoubleOperationResolvable merge(DoubleOperationResolvable left, MergeType mergeType) {
        return merge(this, left, mergeType);
    }

    public static DoubleOperationResolvable merge(DoubleOperationResolvable left, DoubleOperationResolvable right, MergeType mergeType) {
        Function<Pair<String, ModuleInstance>, String> functionTransformer = right.functionTransformer;
        if (MergeType.OVERWRITE.equals(mergeType)) {
            return right;
        }
        if (MergeType.EXTEND.equals(mergeType)) {
            functionTransformer = left.functionTransformer;
        }
        List<Operation> operationList = new ArrayList<>(left.operations);
        operationList.addAll(right.operations);
        return new DoubleOperationResolvable(operationList, functionTransformer);
    }

    public static class Operation {
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
        public static Codec<String> NUMBERSTRINGCODEC = new Codec<String>() {
            @Override
            public <T> DataResult<Pair<String, T>> decode(DynamicOps<T> ops, T input) {
                DataResult<Pair<Boolean, T>> decodeBoolean = Codec.BOOL.decode(ops, input);
                if (decodeBoolean.isSuccess()) {
                    return DataResult.success(new Pair<>(decodeBoolean.getOrThrow().getFirst() ? "1" : "-1", input));
                }
                DataResult<Pair<Double, T>> decodeDouble = Codec.DOUBLE.decode(ops, input);
                if (decodeDouble.isSuccess()) {
                    Pair<Double, T> doubleTPair = decodeDouble.getOrThrow();
                    return DataResult.success(new Pair<>("" + doubleTPair.getFirst(), input));
                }
                DataResult<Pair<String, T>> decodeString = Codec.STRING.decode(ops, input);
                if (decodeString.isSuccess()) {
                    Pair<String, T> pair = decodeString.getOrThrow();
                    return DataResult.success(new Pair<>(pair.getFirst(), input));
                }
                return DataResult.error(() -> "is neither a string nor a boolean or a number");
            }

            @Override
            public <T> DataResult<T> encode(String input, DynamicOps<T> ops, T prefix) {
                return Codec.STRING.encode(input, ops, prefix);
            }
        };
        @AutoCodec.Name("operation")
        @CodecBehavior.Override("operationCodec")
        public AttributeModifier.Operation attributeOperation = AttributeModifier.Operation.ADD_VALUE;
        @AutoCodec.Name("value")
        @CodecBehavior.Override("NUMBERSTRINGCODEC")
        public String value;
        @AutoCodec.Ignored
        public ModuleInstance instance;

        public Operation() {
            this.value = "1";
        }

        public Operation(String value) {
            this.value = value;
        }

        public double solve() {
            if (instance == null) {
                var error = new IllegalAccessError("Double Resolvable was resolved before initialized!");
                Miapi.LOGGER.error("Double Resolvable was never initialized!", error);
                return 0;
            }
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
