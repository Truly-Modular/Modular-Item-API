package smartin.miapi.stat.text;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;
import smartin.miapi.stat.StatTypes;
import smartin.miapi.stat.api.StatType;
import smartin.miapi.stat.api.StatValue;
import smartin.miapi.stat.api.data.number.DoubleStatData;

import java.text.DecimalFormat;
import java.util.Objects;

public final class DoubleTextFormatter
        implements TextFormatter<DoubleOperationResolvable, DoubleStatData> {

    @Override
    public StatType<DoubleOperationResolvable, DoubleStatData> type() {
        return StatTypes.DOUBLE_RESOLVEABLE_TYPE;
    }

    @Override
    public Component format(
            StatValue.StatWithValues<DoubleOperationResolvable, DoubleStatData> stat,
            DoubleOperationResolvable value,
            ItemStack baseItem) {

        return Component.empty()
                .append(Component.literal(stat.stat().getName(baseItem).getString())
                        .withStyle(getNameStyle()))
                .append(Component.literal(":")
                        .withStyle(getDoublePOintStyle()))
                .append(Component.literal(" " + value(stat, value, stat.stat().metaData().format()))
                        .withStyle(getNumberStyle()));
    }

    public boolean isEqual(DoubleOperationResolvable baseValue, DoubleOperationResolvable compareValue) {
        if (Objects.equals(baseValue, compareValue)) {
            return true;
        }
        if (baseValue != null && compareValue != null) {
            return baseValue.getValue() == compareValue.getValue();
        }
        return false;
    }

    @Override
    public Component format(
            StatValue.StatWithValues<DoubleOperationResolvable, DoubleStatData> stat,
            DoubleOperationResolvable baseValue,
            DoubleOperationResolvable compareValue,
            ItemStack baseItem) {

        boolean inverse = stat.stat().metaData().inverse();

        return Component.empty()
                .append(Component.literal(stat.stat().getName(baseItem).getString())
                        .withStyle(getNameStyle()))
                .append(Component.literal(":")
                        .withStyle(getDoublePOintStyle()))
                .append(Component.literal(" " + value(
                                stat, baseValue, stat.stat().metaData().format()))
                        .withStyle(getNumberStyle()))
                .append(Component.literal(" -> ")
                        .withStyle(getArrowStyle()))
                .append(Component.literal(value(
                                stat, compareValue, stat.stat().metaData().format()))
                        .withStyle(getNumberStyle(getValue(baseValue, stat), getValue(compareValue, stat), inverse)));
    }

    double getValue(DoubleOperationResolvable operationResolvable, StatValue.StatWithValues<DoubleOperationResolvable, DoubleStatData> stat) {
        return operationResolvable == null ? stat.stat().metaData().defaultValue() : operationResolvable.getValue();
    }

    private static String value(StatValue.StatWithValues<DoubleOperationResolvable, DoubleStatData> stat, DoubleOperationResolvable value, DecimalFormat format) {

        return value == null
                ? format.format(stat.stat().metaData().defaultValue())
                : format.format(value.getValue());
    }
}