package smartin.miapi.stat.text;

import com.redpxnda.nucleus.util.Color;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.stat.api.StatAggregator;
import smartin.miapi.stat.api.StatGroup;
import smartin.miapi.stat.api.StatValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class TextUI {
    private static final List<TextFormatter<?, ?>> FORMATTERS = List.of(
            new BooleanTextFormatter(),
            new DoubleTextFormatter(),
            new AttributeTextFormatter(),
            new MiningTextFormatter()
    );

    private TextUI() {

    }

    public static @NotNull List<Component> getStatComponentList(ItemStack base, ItemStack compare, boolean displayOnlyDiff) {
        List<Component> list = new ArrayList<>();
        List<Tuple<StatGroup, List<StatValue.StatWithValues<?, ?>>>> stats = StatAggregator.findAllStats(base, compare);
        stats.forEach(groupTuple -> {
            boolean groupNotAdded = true;
            for (StatValue.StatWithValues<?, ?> stat : groupTuple.getB()) {
                Optional<Component> text = format(stat, base, displayOnlyDiff);
                if (text.isPresent()) {
                    if (groupNotAdded) {
                        MutableComponent groupComponent = Component.literal("");
                        groupComponent.append(groupTuple.getA().getName());
                        groupComponent.withStyle(getGroupStyle());
                        list.add(groupComponent);
                        groupNotAdded = false;
                    }
                    MutableComponent component = Component.literal("  ");
                    component.append(text.get());
                    list.add(component);
                }
            }
        });
        return list;
    }

    public static @NotNull MutableComponent getStatComponent(Component firstLine, ItemStack base, ItemStack compare, boolean displayOnlyDiff) {
        MutableComponent component = Component.literal("");
        component.append(firstLine);
        component.append("\n");
        getStatComponentList(base, compare, displayOnlyDiff).forEach(c -> {
            component.append(c);
            component.append("\n");
        });
        return component;
    }

    public static Optional<Component> format(StatValue.StatWithValues<?, ?> stat, ItemStack base, boolean displayDiff) {
        for (TextFormatter<?, ?> formatter : FORMATTERS) {
            if (formatter.type() == stat.stat().type()) {
                return Optional.ofNullable(formatTyped(formatter, stat, base, displayDiff));
            }
        }

        Object baseValue = stat.baseItemValue().value();
        Object compareValue = stat.compareItemValue().value();

        if (Objects.equals(baseValue, compareValue)) {
            return Optional.of(Component.literal(
                    stat.stat().id()
                    + ": "
                    + valueString(baseValue)
            ));
        }

        return Optional.of(Component.literal(
                stat.stat().id()
                + ": "
                + valueString(baseValue)
                + " -> "
                + valueString(compareValue)
        ));
    }

    private static Style getGroupStyle() {
        return Style.EMPTY.withBold(true).withColor(new Color(0, 170, 170, 255).argb());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Nullable
    private static Component formatTyped(
            TextFormatter formatter,
            StatValue.StatWithValues<?, ?> stat,
            ItemStack baseItem,
            boolean displayDiff
    ) {
        return formatter.format(stat, baseItem, displayDiff);
    }

    private static String valueString(Object value) {
        return value == null ? "null" : value.toString();
    }
}
