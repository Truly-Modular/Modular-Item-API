package smartin.miapi.stat.text;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;
import smartin.miapi.stat.api.StatAggregator;
import smartin.miapi.stat.api.StatGroup;
import smartin.miapi.stat.api.StatValue;

import java.util.List;
import java.util.Objects;

public final class StatLineFormatters {

    private static final List<TextFormatter<?, ?>> FORMATTERS = List.of(
            new BooleanTextFormatter(),
            new DoubleTextFormatter(),
            new AttributeTextFormatter(),
            new MiningTextFormatter()
    );

    private StatLineFormatters() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> handStats = Commands.literal("miapi")
                .then(Commands.literal("getStats")
                        .executes(StatLineFormatters::executeStat));

        LiteralArgumentBuilder<CommandSourceStack> compareStatsCommand = Commands.literal("miapi")
                .then(Commands.literal("compareStats")
                        .executes(StatLineFormatters::executeCompare));

        dispatcher.register(handStats);
        dispatcher.register(compareStatsCommand);
    }

    private static int executeStat(CommandContext<CommandSourceStack> context) {
        if (context.getSource().isPlayer()) {
            Player player = context.getSource().getPlayer();
            context.getSource().sendSuccess(() -> {
                MutableComponent component = Component.literal("");
                List<Tuple<StatGroup, List<StatValue.StatWithValues<?, ?>>>> stats = StatAggregator.findAllStats(player.getMainHandItem(), player.getMainHandItem());
                stats.forEach(groupTuple -> {
                    component.append(groupTuple.getA().getName());
                    component.append("\n");
                    groupTuple.getB().forEach(stat -> {
                        component.append("   ");
                        component.append(format(stat));
                        component.append("\n");
                    });
                });
                return component;
            }, true);
            return 0;
        } else {
            return 1;
        }
    }

    private static int executeCompare(CommandContext<CommandSourceStack> context) {
        if (context.getSource().isPlayer()) {
            Player player = context.getSource().getPlayer();
            context.getSource().sendSuccess(() -> {
                MutableComponent component = Component.literal("");
                List<Tuple<StatGroup, List<StatValue.StatWithValues<?, ?>>>> stats = StatAggregator.findAllStats(player.getMainHandItem(), player.getOffhandItem());
                stats.forEach(groupTuple -> {
                    component.append(groupTuple.getA().getName());
                    component.append("\n");
                    groupTuple.getB().forEach(stat -> {
                        component.append("   ");
                        component.append(format(stat));
                        component.append("\n");
                    });
                });
                return component;
            }, true);
            return 0;
        } else {
            return 1;
        }
    }


    public static Component format(StatValue.StatWithValues<?, ?> stat) {
        for (TextFormatter<?, ?> formatter : FORMATTERS) {
            if (formatter.type() == stat.stat().type()) {
                return formatTyped(formatter, stat);
            }
        }

        Object baseValue = stat.baseItemValue().value();
        Object compareValue = stat.compareItemValue().value();

        if (Objects.equals(baseValue, compareValue)) {
            return Component.literal(
                    stat.stat().id()
                    + ": "
                    + valueString(baseValue)
            );
        }

        return Component.literal(
                stat.stat().id()
                + ": "
                + valueString(baseValue)
                + " -> "
                + valueString(compareValue)
        );
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Component formatTyped(
            TextFormatter formatter,
            StatValue.StatWithValues<?, ?> stat
    ) {
        return formatter.format(stat);
    }

    private static String valueString(Object value) {
        return value == null ? "null" : value.toString();
    }
}