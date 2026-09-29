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
        AttributeStatData meta = stat.stat().metaData();

        return Component.literal(
                stat.stat().getName(baseItem)
                + " [" + meta.operation() + "]: "
                + value(value, stat.stat().metaData().format())
        );
    }

    @Override
    public Component format(
            StatValue.StatWithValues<Double, AttributeStatData> stat,
            Double baseValue,
            Double compareValue,
            ItemStack baseItem) {
        AttributeStatData meta = stat.stat().metaData();

        return Component.literal(
                stat.stat().getName(baseItem).getString()
                + " [" + meta.operation() + "]: "
                + value(baseValue, stat.stat().metaData().format())
                + " -> "
                + value(compareValue, stat.stat().metaData().format())
        );
    }

    private static String value(Double value, DecimalFormat format) {
        return value == null ? "null" : format.format(value);
    }
}