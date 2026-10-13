package smartin.miapi.stat.text;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.stat.StatTypes;
import smartin.miapi.stat.api.StatType;
import smartin.miapi.stat.api.StatValue;
import smartin.miapi.stat.api.data.attribute.AttributeStatData;

import java.text.DecimalFormat;

public final class AttributeTextFormatter
        implements TextFormatter<Double, AttributeStatData> {

    @Override
    public StatType<Double, AttributeStatData> type() {
        return StatTypes.ATTRIBUTE_TYPE;
    }

    @Override
    public Component format(
            StatValue.StatWithValues<Double, AttributeStatData> stat,
            Double value,
            ItemStack baseItem) {

        return Component.empty()
                .append(Component.literal(stat.stat().getName(baseItem).getString())
                        .withStyle(getNameStyle()))
                .append(Component.literal(":")
                        .withStyle(getDoublePOintStyle()))
                .append(Component.literal(" " + value(stat, value, stat.stat().metaData().format()))
                        .withStyle(getNumberStyle()));
    }

    @Override
    public Component format(
            StatValue.StatWithValues<Double, AttributeStatData> stat,
            Double baseValue,
            Double compareValue,
            ItemStack baseItem) {

        boolean inverse = false;

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
                        .withStyle(getNumberStyle(baseValue, compareValue, inverse)));
    }

    private static String value(StatValue.StatWithValues<Double, AttributeStatData> stat, Double value, DecimalFormat format) {
        return value == null ? format.format(stat.stat().metaData().defaultValue()) : format.format(value);
    }
}