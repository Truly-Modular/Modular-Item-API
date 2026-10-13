package smartin.miapi.stat.text;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.stat.api.StatMetaData;
import smartin.miapi.stat.api.StatType;
import smartin.miapi.stat.api.StatValue;

import java.awt.*;
import java.util.Objects;

public interface TextFormatter<T, M extends StatMetaData<T>> {

    StatType<T, M> type();

    @Nullable
    default Component format(StatValue.StatWithValues<T, M> stat, ItemStack baseItem, boolean displayOnlyDiff) {
        T baseValue = stat.baseItemValue().value();
        T compareValue = stat.compareItemValue().value();

        boolean hasDifference = !isEqual(baseValue, compareValue);
        if (!hasDifference && displayOnlyDiff) {
            return null;
        }

        if (!hasDifference) {
            return format(stat, baseValue, baseItem);
        }

        return format(stat, baseValue, compareValue, baseItem);
    }

    default boolean isEqual(T baseValue, T compareValue) {
        return Objects.equals(baseValue, compareValue);
    }

    default Style getNameStyle() {
        return Style.EMPTY.withBold(true).withColor(Color.LIGHT_GRAY.getRGB());
    }

    default Style getDoublePOintStyle() {
        return Style.EMPTY.withBold(false).withColor(Color.LIGHT_GRAY.getRGB());
    }

    default Style getNumberStyle() {
        return Style.EMPTY.withBold(false).withColor(Color.WHITE.getRGB());
    }

    default Style getNumberStyleBetter() {
        return Style.EMPTY.withBold(true).withColor(Color.GREEN.getRGB());
    }

    default Style getNumberStyleWorse() {
        return Style.EMPTY.withBold(true).withColor(Color.RED.getRGB());
    }

    default Style getNumberStyle(double baseValue, double compareValue, boolean inverse) {
        boolean better = inverse
                ? compareValue < baseValue
                : compareValue > baseValue;

        return better ? getNumberStyleBetter() : getNumberStyleWorse();
    }


    default Style getArrowStyle() {
        return Style.EMPTY.withBold(false).withColor(Color.LIGHT_GRAY.getRGB());
    }

    Component format(StatValue.StatWithValues<T, M> stat, T value, ItemStack baseItem);

    Component format(StatValue.StatWithValues<T, M> stat, T baseValue, T compareValue, ItemStack baseItem);
}