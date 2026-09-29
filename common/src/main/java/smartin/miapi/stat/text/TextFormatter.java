package smartin.miapi.stat.text;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.stat.api.StatMetaData;
import smartin.miapi.stat.api.StatType;
import smartin.miapi.stat.api.StatValue;

import java.util.Objects;

public interface TextFormatter<T, M extends StatMetaData<T>> {

    StatType<T, M> type();

    default Component format(StatValue.StatWithValues<T, M> stat, ItemStack baseItem) {
        T baseValue = stat.baseItemValue().value();
        T compareValue = stat.compareItemValue().value();

        if (Objects.equals(baseValue, compareValue)) {
            return format(stat, baseValue, baseItem);
        }

        return format(stat, baseValue, compareValue,baseItem );
    }

    Component format(StatValue.StatWithValues<T, M> stat, T value, ItemStack baseItem);

    Component format(StatValue.StatWithValues<T, M> stat, T baseValue, T compareValue, ItemStack baseItem);
}