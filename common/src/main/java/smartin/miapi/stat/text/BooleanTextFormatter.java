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

        return Component.empty()
                .append(Component.literal(stat.stat().getName(baseItem).getString())
                        .withStyle(getNameStyle()))
                .append(Component.literal(":")
                        .withStyle(getDoublePOintStyle()))
                .append(Component.literal(" " + value(stat, value))
                        .withStyle(getNumberStyle()));
    }


    @Override
    public Component format(
            StatValue.StatWithValues<Boolean, BooleanStatData> stat,
            Boolean baseValue,
            Boolean compareValue,
            ItemStack baseItem) {

        return Component.empty()
                .append(Component.literal(stat.stat().getName(baseItem).getString())
                        .withStyle(getNameStyle()))
                .append(Component.literal(":")
                        .withStyle(getDoublePOintStyle()))
                .append(Component.literal(" " + value(stat, baseValue))
                        .withStyle(getNumberStyle()))
                .append(Component.literal(" -> ")
                        .withStyle(getArrowStyle()))
                .append(Component.literal(value(stat, compareValue))
                        .withStyle(getNumberStyle()));
    }

    private static String value(StatValue.StatWithValues<Boolean, BooleanStatData> stat, Boolean value) {
        return value == null ? Boolean.toString(stat.stat().metaData().defaultValue()) : Boolean.toString(value);
    }
}