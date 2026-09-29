package smartin.miapi.stat.text;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.stat.StatTypes;
import smartin.miapi.stat.api.StatType;
import smartin.miapi.stat.api.StatValue;
import smartin.miapi.stat.api.data.mining.MiningStat;
import smartin.miapi.stat.api.data.mining.MiningStatData;

import java.text.DecimalFormat;
import java.util.Objects;

public final class MiningTextFormatter
        implements TextFormatter<MiningStat, MiningStatData> {

    @Override
    public StatType<MiningStat, MiningStatData> type() {
        return StatTypes.PICKAXE_MINING;
    }

    @Override
    public Component format(
            StatValue.StatWithValues<MiningStat, MiningStatData> stat,
            MiningStat value,
            ItemStack baseItem) {

        return Component.empty()
                .append(Component.literal(stat.stat().getName(baseItem).getString())
                        .withStyle(getNameStyle()))
                .append(Component.literal(":")
                        .withStyle(getDoublePOintStyle()))
                .append(Component.literal(" " + value(value))
                        .withStyle(getNumberStyle()));
    }

    public boolean isEqual(MiningStat baseValue, MiningStat compareValue) {
        if (Objects.equals(baseValue, compareValue)) {
            return true;
        }
        if (baseValue != null && compareValue != null) {
            return baseValue.speed() == compareValue.speed();
        }
        return false;
    }

    @Override
    public Component format(
            StatValue.StatWithValues<MiningStat, MiningStatData> stat,
            MiningStat baseValue,
            MiningStat compareValue,
            ItemStack baseItem) {

        return Component.empty()
                .append(Component.literal(stat.stat().getName(baseItem).getString())
                        .withStyle(getNameStyle()))
                .append(Component.literal(":")
                        .withStyle(getDoublePOintStyle()))
                .append(Component.literal(" " + value(baseValue))
                        .withStyle(getNumberStyle()))
                .append(Component.literal(" -> ")
                        .withStyle(getArrowStyle()))
                .append(Component.literal(value(compareValue))
                        .withStyle(getNumberStyle()));
    }

    private static String value(MiningStat value) {
        if (value == null) {
            return "null";
        }

        return new DecimalFormat("##.##").format(value.speed());
    }
}