package smartin.miapi.stat.text;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;
import smartin.miapi.stat.StatTypes;
import smartin.miapi.stat.api.StatType;
import smartin.miapi.stat.api.StatValue;
import smartin.miapi.stat.api.data.number.DoubleStatData;

import java.text.DecimalFormat;

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
        return Component.literal(
                stat.stat().getName(baseItem)
                + ": "
                + value(value, stat.stat().metaData().format())
        );
    }

    @Override
    public Component format(
            StatValue.StatWithValues<DoubleOperationResolvable, DoubleStatData> stat,
            DoubleOperationResolvable baseValue,
            DoubleOperationResolvable compareValue,
            ItemStack baseItem) {
        return Component.literal(
                stat.stat().getName(baseItem).getString()
                + ": "
                + value(baseValue, stat.stat().metaData().format())
                + " -> "
                + value(compareValue, stat.stat().metaData().format())
        );
    }

    private static String value(DoubleOperationResolvable value, DecimalFormat format) {

        return value == null
                ? "null"
                : format.format(value.getValue());
    }
}