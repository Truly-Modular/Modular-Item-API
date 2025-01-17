package smartin.miapi.modules.properties.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.modules.ModuleInstance;

import java.util.List;
import java.util.Optional;

public abstract class DoubleProperty extends CodecProperty<DoubleOperationResolvable> {
    public DoubleProperty property;
    public double baseValue = 0;
    public boolean allowVisualOnly = false;
    public final ResourceLocation id;
    static Codec<List<DoubleOperationResolvable.Operation>> listCodec = Codec.list(AutoCodec.of(DoubleOperationResolvable.Operation.class).codec());
    public static Codec<List<DoubleOperationResolvable.Operation>> CODEC = Codec.withAlternative(
            new Codec<>() {
                @Override
                public <T> DataResult<T> encode(List<DoubleOperationResolvable.Operation> input, DynamicOps<T> ops, T prefix) {
                    return listCodec.encode(input, ops, prefix);
                }

                @Override
                public <T> DataResult<Pair<List<DoubleOperationResolvable.Operation>, T>> decode(DynamicOps<T> ops, T input) {
                    Pair<String, T> stringTPair = Codec.STRING.decode(ops, input).getOrThrow();
                    List<DoubleOperationResolvable.Operation> operations = List.of(new DoubleOperationResolvable.Operation(stringTPair.getFirst()));
                    return DataResult.success(new Pair<>(operations, stringTPair.getSecond()));
                }
            },
            listCodec);

    protected DoubleProperty(ResourceLocation cacheKey) {
        super(DoubleOperationResolvable.CODEC);
        property = this;
        this.id = cacheKey;
    }

    public DoubleOperationResolvable initialize(DoubleOperationResolvable property, ModuleInstance context) {
        return property.initialize(context);
    }

    public Optional<Double> getValue(ItemStack itemStack) {
        Optional<DoubleOperationResolvable> optional = getData(itemStack);
        if (optional.isPresent()) {
            return optional.get().evaluate(baseValue);
        }
        return Optional.empty();
    }

    public Optional<Double> getValue(ModuleInstance moduleInstance) {
        Optional<DoubleOperationResolvable> optional = getData(moduleInstance);
        if (optional.isPresent()) {
            return optional.get().evaluate(baseValue);
        }
        return Optional.empty();
    }

    public double getForItems(Iterable<ItemStack> itemStacks) {
        double collected = 0;
        for (ItemStack itemStack : itemStacks) {
            var optional = getValue(itemStack);
            if (optional.isPresent()) {
                collected += optional.get();
            }
        }
        return collected;
    }

    public DoubleOperationResolvable merge(DoubleOperationResolvable left, DoubleOperationResolvable right, MergeType mergeType) {
        return left.merge(right, mergeType);
    }

}
