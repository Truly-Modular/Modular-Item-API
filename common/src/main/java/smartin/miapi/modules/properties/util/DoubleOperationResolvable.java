package smartin.miapi.modules.properties.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import com.redpxnda.nucleus.codec.behavior.CodecBehavior;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
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
public class DoubleOperationResolvable implements SourceSetter<DoubleOperationResolvable> {
    static Codec<IndividualOperation> autoCodec = AutoCodec.of(IndividualOperation.class).codec();
    static Codec<IndividualOperation> operationCodec = Codec.withAlternative(new Codec<>() {
        @Override
        public <T> DataResult<T> encode(IndividualOperation input, DynamicOps<T> ops, T prefix) {
            return autoCodec.encode(input, ops, prefix);
        }

        @Override
        public <T> DataResult<Pair<IndividualOperation, T>> decode(DynamicOps<T> ops, T input) {
            DataResult<Pair<String, T>> result = IndividualOperation.NUMBERSTRINGCODEC.decode(ops, input);
            if (result.isError()) {
                DataResult<Pair<Double, T>> doubleResult = Codec.DOUBLE.decode(ops, input);
                if (doubleResult.isSuccess()) {
                    Pair<Double, T> doubleTPair = doubleResult.getOrThrow();
                    return DataResult.success(new Pair<>(new IndividualOperation("" + doubleTPair.getFirst()), doubleTPair.getSecond()));
                }
                return DataResult.error(() -> "is neither a string nor a boolean or a number");
            }
            Pair<String, T> stringTPair = result.getOrThrow();
            return DataResult.success(new Pair<>(new IndividualOperation(stringTPair.getFirst()), stringTPair.getSecond()));
        }
    }, autoCodec);
    static Codec<List<IndividualOperation>> listCodec = Codec.list(operationCodec);
    public static Codec<DoubleOperationResolvable> CODEC = Codec.withAlternative(new Codec<>() {
        @Override
        public <T> DataResult<T> encode(DoubleOperationResolvable input, DynamicOps<T> ops, T prefix) {
            List<IndividualOperation> opsToEncode = input.operations.isEmpty()
                    ? List.of(new IndividualOperation("" + input.fallback)) // Replace with your default
                    : input.operations;

            return listCodec.encode(opsToEncode, ops, prefix);
        }

        @Override
        public <T> DataResult<Pair<DoubleOperationResolvable, T>> decode(DynamicOps<T> ops, T input) {
            var result = operationCodec.decode(ops, input);
            if (result.isError()) {
                return DataResult.error(() -> "could not decode double operations");
            }
            Pair<IndividualOperation, T> pair = result.getOrThrow();
            return DataResult.success(new Pair<>(new DoubleOperationResolvable(List.of(pair.getFirst())), pair.getSecond()));
        }
    }, new Codec<>() {
        @Override
        public <T> DataResult<T> encode(DoubleOperationResolvable input, DynamicOps<T> ops, T prefix) {
            List<IndividualOperation> opsToEncode = input.operations.isEmpty()
                    ? List.of(new IndividualOperation("" + input.fallback)) // Replace with your default
                    : input.operations;

            return listCodec.encode(opsToEncode, ops, prefix);
        }

        @Override
        public <T> DataResult<Pair<DoubleOperationResolvable, T>> decode(DynamicOps<T> ops, T input) {
            Pair<List<IndividualOperation>, T> pair = listCodec.decode(ops, input).getOrThrow((e) -> new RuntimeException(e + "could not decode double operations"));
            return DataResult.success(new Pair<>(new DoubleOperationResolvable(pair.getFirst()), pair.getSecond()));
        }
    });

    public List<IndividualOperation> operations;
    /**
     * use the function to set this value.
     */
    protected Function<Pair<String, ModuleInstance>, String> functionTransformer = (Pair::getFirst);
    Double cachedResult = null;
    double baseValue = 0.0;
    double fallback = 0.0;
    ModuleInstance initialized;

    /**
     * @param fallback the value this will assume in resolve if no value was set at all, you can use the other resolve methods to not rely on it
     */
    public DoubleOperationResolvable(double fallback) {
        this.fallback = fallback;
        operations = new ArrayList<>();
    }

    public DoubleOperationResolvable(List<IndividualOperation> operations) {
        this.operations = operations;
    }

    protected DoubleOperationResolvable(List<IndividualOperation> operations, Function<Pair<String, ModuleInstance>, String> functionTransformer) {
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
     * returns the fallback value,
     * this value is returned if there are no operations set.
     *
     * @return
     */
    public double getFallback() {
        return fallback;
    }

    /**
     * returns the fallback value,
     * this value is returned if there are no operations set.
     *
     * @return
     */
    public double getBaseValue() {
        return baseValue;
    }


    /**
     * use this function to set a new FunctionTransformer, it resets the cachedResult as well
     *
     * @param functionTransformer the new FunctionTransformer
     */
    public void setFunctionTransformer(Function<Pair<String, ModuleInstance>, String> functionTransformer) {
        this.functionTransformer = functionTransformer;
        for (IndividualOperation operation : operations) {
            operation.transformer = this.functionTransformer;
        }
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
        List<IndividualOperation> operationList = new ArrayList<>();
        if (operations != null) {
            operations.forEach(operation -> {
                IndividualOperation copiesOperation = new IndividualOperation(operation.value);
                copiesOperation.attributeOperation = operation.attributeOperation;
                copiesOperation.instance = moduleInstance;
                copiesOperation.transformer = this.functionTransformer;
                copiesOperation.source = operation.source;
                operationList.add(copiesOperation);
            });
        }
        DoubleOperationResolvable initialized = new DoubleOperationResolvable(operationList, functionTransformer);
        initialized.fallback = this.fallback;
        initialized.getValue();
        initialized.initialized = moduleInstance;
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

    public void clearCache() {
        this.cachedResult = null;
    }


    public Optional<Double> evaluate(double baseValue) {
        if (cachedResult == null || baseValue != this.baseValue) {
            resolve(operations, baseValue).ifPresent(result -> cachedResult = result);
        }
        return Optional.ofNullable(cachedResult);
    }

    public static double resolve(List<IndividualOperation> operations, double baseValue, double fallback) {
        return resolve(operations, baseValue).orElse(fallback);
    }

    public static Optional<Double> resolve(List<IndividualOperation> operations, double baseValue) {
        double value = baseValue;
        boolean hasValue = false;
        List<Double> addition = new ArrayList<>();
        List<Double> multiplyBase = new ArrayList<>();
        List<Double> multiplyTotal = new ArrayList<>();
        List<IndividualOperation> custom = new ArrayList<>();
        for (IndividualOperation operation : operations) {
            hasValue = true;
            switch (operation.attributeOperation) {
                case ADD_VALUE -> addition.add(operation.solve());
                case ADD_MULTIPLIED_BASE -> multiplyBase.add(operation.solve());
                case ADD_MULTIPLIED_TOTAL -> multiplyTotal.add(operation.solve());
                case CUSTOM_TOTAL -> custom.add(operation);
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
            for (IndividualOperation operation : custom) {
                value = operation.solve(value);
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
        if (MergeType.REMOVE.equals(mergeType)) {
            return left;
        }
        if (MergeType.EXTEND.equals(mergeType)) {
            functionTransformer = left.functionTransformer;
        }
        List<IndividualOperation> operationList = new ArrayList<>(left.operations);
        operationList.addAll(right.operations);
        DoubleOperationResolvable resolvable = new DoubleOperationResolvable(operationList, functionTransformer);
        resolvable.fallback = MergeAble.decideLeftRight(left.fallback, right.fallback, mergeType);
        if (left.initialized != null) {
            resolvable.initialize(left.initialized);
        }
        return resolvable;
    }

    @Override
    public DoubleOperationResolvable setSource(DoubleOperationResolvable data, Component source) {
        data.operations.forEach(op -> op.source = Optional.of(source));
        return data;
    }

    public List<StatResolver.TraceNode> getResolvedTrace() {
        List<StatResolver.TraceNode> children = new ArrayList<>();

        for (IndividualOperation op : operations) {
            StatResolver.TraceNode node;
            node = op.asNode();
            children.add(
                    node
            );
        }
        return children;
    }

    public boolean isInitialized() {
        return this.initialized!=null;
    }


    public static class IndividualOperation {


        public static Codec<Operation> operationCodec = new Codec<>() {
            @Override
            public <T> DataResult<Pair<Operation, T>> decode(DynamicOps<T> ops, T input) {
                Pair<String, T> stringTPair = Codec.STRING.decode(ops, input).getOrThrow();
                Operation operations = getOperation(stringTPair.getFirst());
                return DataResult.success(new Pair<>(operations, stringTPair.getSecond()));
            }

            @Override
            public <T> DataResult<T> encode(Operation input, DynamicOps<T> ops, T prefix) {
                return Codec.STRING.encode(toCodecString(input), ops, prefix);
            }
        };
        public static Codec<String> NUMBERSTRINGCODEC = new Codec<String>() {
            @Override
            public <T> DataResult<Pair<String, T>> decode(DynamicOps<T> ops, T input) {
                var numberResult = ops.getNumberValue(input);
                if (numberResult.isSuccess()) {
                    return DataResult.success(new Pair<>(numberResult.getOrThrow().toString(), input));
                }
                var boolResult = ops.getBooleanValue(input);
                if (boolResult.isSuccess()) {
                    return DataResult.success(new Pair<>(boolResult.getOrThrow() ? "1" : "-1", input));
                }
                var stringResult = ops.getStringValue(input);
                if (stringResult.isSuccess()) {
                    if (stringResult.getOrThrow().equals("true")) {
                        return DataResult.success(new Pair<>("1", input));
                    } else if (stringResult.getOrThrow().equals("false")) {
                        return DataResult.success(new Pair<>("-1", input));
                    }
                    return DataResult.success(new Pair<>(stringResult.getOrThrow(), input));
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
                DataResult<Pair<Boolean, T>> decodeBoolean = Miapi.FIXED_BOOL_CODEC.decode(ops, input);
                if (decodeBoolean.isSuccess()) {
                    return DataResult.success(new Pair<>(decodeBoolean.getOrThrow().getFirst() ? "1" : "-1", input));
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
        public Operation attributeOperation = Operation.ADD_VALUE;
        @AutoCodec.Name("value")
        @CodecBehavior.Override("NUMBERSTRINGCODEC")
        public String value;
        @AutoCodec.Ignored
        public ModuleInstance instance;
        @AutoCodec.Ignored
        public Function<Pair<String, ModuleInstance>, String> transformer = (Pair::getFirst);
        @AutoCodec.Ignored
        public Optional<Component> source = Optional.empty();

        public IndividualOperation() {
            this.value = "1";
        }

        public IndividualOperation(double value, Operation operation) {
            this.value = String.valueOf(value);
            this.attributeOperation = operation;
        }

        public IndividualOperation(String value) {
            this.value = value;
        }

        public double solve() {
            if (instance == null) {
                var error = new IllegalAccessError("Double Resolvable was resolved before initialized!");
                Miapi.LOGGER.error("Double Resolvable was never initialized!", error);
                return 0;
            }
            String transformed = transformer.apply(new Pair<>(value, instance));
            return StatResolver.resolveDouble(transformed, instance);
        }

        public double solve(double oldValue) {
            if (instance == null) {
                var error = new IllegalAccessError("Double Resolvable was resolved before initialized!");
                Miapi.LOGGER.error("Double Resolvable was never initialized!", error);
                return 0;
            }
            String transformed = transformer.apply(new Pair<>(value, instance)).replace("[old_value]", "" + oldValue);
            return StatResolver.resolveDouble(transformed, instance);
        }

        public StatResolver.TraceNode asNode() {
            return StatResolver.resolveDoubleWithTrace(this.value, this.instance).trace();
            /*
            return this.source.map(component ->
                    (StatResolver.TraceNode) new StatResolver.TraceValueWithSource(this.solve(), Component.literal(toCodecString(attributeOperation) + " " + this.value), component)).orElseGet(() ->
                    new StatResolver.TraceValue(this.solve(), Component.literal(attributeOperation.name + " " + this.value)));
             */
        }

        public static Operation getOperation(String operationString) {
            return switch (operationString) {
                case "*" -> Operation.ADD_MULTIPLIED_BASE;
                case "**" -> Operation.ADD_MULTIPLIED_TOTAL;
                case "custom" -> Operation.CUSTOM_TOTAL;
                default -> Operation.ADD_VALUE;
            };
        }

        private static String toCodecString(Operation operation) {
            return switch (operation) {
                case ADD_MULTIPLIED_BASE -> "*";
                case ADD_MULTIPLIED_TOTAL -> "**";
                case CUSTOM_TOTAL -> "custom";
                default -> "+";
            };
        }

        public enum Operation implements StringRepresentable {
            ADD_VALUE("add_value", 0),
            ADD_MULTIPLIED_BASE("add_multiplied_base", 1),
            ADD_MULTIPLIED_TOTAL("add_multiplied_total", 2),
            CUSTOM_TOTAL("custom", 3);

            private final String name;
            private final int id;

            private Operation(final String name, final int value) {
                this.name = name;
                this.id = value;
            }

            public int id() {
                return this.id;
            }

            public String getSerializedName() {
                return this.name;
            }
        }
    }
}
