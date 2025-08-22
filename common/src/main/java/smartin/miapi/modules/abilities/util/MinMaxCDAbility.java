package smartin.miapi.modules.abilities.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.Optional;

/**
 * This should be the default for most abilities,
 * it has support for Minholdtime, MaxHoldTime and Cooldown
 *
 * @param <T> the ability Context needed for your ability.
 */
public abstract class MinMaxCDAbility<T> implements
        ItemUseMinHoldAbility<MinMaxCDAbility.MinMaxCDData<T>>,
        ItemUseDefaultCooldownAbility<MinMaxCDAbility.MinMaxCDData<T>>,
        ItemUseAbility<MinMaxCDAbility.MinMaxCDData<T>> {
    public int defaultMinHoldTime;
    public int defaultMaxHoldTime;
    public int defaultCoolDown;

    protected MinMaxCDAbility(int defaultMin, int defaultCoolDown) {
        this(defaultMin, 72000, defaultCoolDown);
    }

    protected MinMaxCDAbility(int defaultMin, int defaultMax, int defaultCoolDown) {
        this.defaultMinHoldTime = defaultMin;
        this.defaultCoolDown = defaultCoolDown;
        this.defaultMaxHoldTime = defaultMax;
    }

    private Optional<MinMaxCDData<T>> getContext(ItemStack stack) {
        return Optional.ofNullable(getSpecialContext(stack));
    }

    public int getMinHoldTime(ItemStack itemStack) {
        return getContext(itemStack).map(a -> a.min().evaluate(0.0, defaultMinHoldTime)).orElse((double) defaultMinHoldTime).intValue();
    }

    public int getCooldown(ItemStack itemStack) {
        return getContext(itemStack).map(a -> a.cooldown().evaluate(0.0, defaultCoolDown)).orElse((double) defaultCoolDown).intValue();
    }

    public int getMaxUseTime(ItemStack itemStack, LivingEntity livingEntity, MinMaxCDData<T> context) {
        return getContext(itemStack).map(a -> a.max().evaluate(0.0, defaultMaxHoldTime)).orElse((double) defaultMaxHoldTime).intValue();
    }

    public MinMaxCDData<T> merge(MinMaxCDData<T> left, MinMaxCDData<T> right, MergeType mergeType) {
        return new MinMaxCDData<>(
                mergeData(left.data(), right.data(), mergeType),
                DoubleOperationResolvable.merge(left.min(), left.min(), mergeType),
                DoubleOperationResolvable.merge(left.max(), left.max(), mergeType),
                DoubleOperationResolvable.merge(left.cooldown(), left.cooldown(), mergeType)
        );
    }

    public MinMaxCDData<T> initialize(MinMaxCDData<T> property, ModuleInstance context) {
        return new MinMaxCDData<>(
                initializeData(property.data(), context),
                property.min().initialize(context),
                property.max().initialize(context),
                property.cooldown().initialize(context));
    }

    protected abstract T mergeData(T left, T right, MergeType mergeType);

    protected abstract T initializeData(T property, ModuleInstance context);

    protected abstract MapCodec<T> getMapCodec();

    public Optional<T> getData(ItemStack itemStack) {
        return getContext(itemStack).map(c -> c.data);
    }

    @Override
    public Codec<MinMaxCDAbility.MinMaxCDData<T>> getCodec() {
        MapCodec<T> dataCodec = getMapCodec();

        return RecordCodecBuilder.create(instance -> instance.group(
                dataCodec.forGetter(MinMaxCDData::data),
                DoubleOperationResolvable.CODEC.optionalFieldOf("min_hold", new DoubleOperationResolvable(defaultMinHoldTime)).forGetter(MinMaxCDData::min),
                DoubleOperationResolvable.CODEC.optionalFieldOf("max_hold", new DoubleOperationResolvable(defaultMaxHoldTime)).forGetter(MinMaxCDData::max),
                DoubleOperationResolvable.CODEC.optionalFieldOf("cooldown", new DoubleOperationResolvable(defaultCoolDown)).forGetter(MinMaxCDData::cooldown)
        ).apply(instance, MinMaxCDAbility.MinMaxCDData::new));
    }

    public record MinMaxCDData<T>(T data, DoubleOperationResolvable min, DoubleOperationResolvable max,
                                  DoubleOperationResolvable cooldown) {
    }
}
