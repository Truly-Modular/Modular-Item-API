package smartin.miapi.stat.text;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.stat.StatTypes;
import smartin.miapi.stat.api.StatType;
import smartin.miapi.stat.api.StatValue;
import smartin.miapi.stat.api.data.mining.MiningStat;
import smartin.miapi.stat.api.data.mining.MiningStatData;

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
        return Component.literal(
                stat.stat().getName(baseItem)
                + ": "
                + value(value)
        );
    }

    @Override
    public Component format(
            StatValue.StatWithValues<MiningStat, MiningStatData> stat,
            MiningStat baseValue,
            MiningStat compareValue,
            ItemStack baseItem) {
        return Component.literal(
                stat.stat().getName(baseItem).getString()
                + ": "
                + value(baseValue)
                + " -> "
                + value(compareValue)
        );
    }

    private static String value(MiningStat value) {
        if (value == null) {
            return "null";
        }

        return value.level()
               + " / speed="
               + value.speed();
    }
}