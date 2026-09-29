package smartin.miapi.stat.text;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.stat.StatTypes;
import smartin.miapi.stat.api.StatType;
import smartin.miapi.stat.api.StatValue;
import smartin.miapi.stat.api.data.BooleanStatData;

public final class BooleanTextFormatter
        implements TextFormatter<Boolean, BooleanStatData> {

    @Override
    public StatType<Boolean, BooleanStatData> type() {
        return StatTypes.BOOLEAN_TYPE;
    }

    @Override
    public Component format(
            StatValue.StatWithValues<Boolean, BooleanStatData> stat,
            Boolean value,
            ItemStack baseItem) {
        return Component.literal(
                stat.stat().getName(baseItem)
                + ": "
                + value(value)
        );
    }

    @Override
    public Component format(
            StatValue.StatWithValues<Boolean, BooleanStatData> stat,
            Boolean baseValue,
            Boolean compareValue,
            ItemStack baseItem) {
        return Component.literal(
                stat.stat().getName(baseItem).getString()
                + ": "
                + value(baseValue)
                + " -> "
                + value(compareValue)
        );
    }

    private static String value(Boolean value) {
        return value == null ? "null" : Boolean.toString(value);
    }
}