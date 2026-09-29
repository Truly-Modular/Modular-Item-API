package smartin.miapi.stat.standardui;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.crafting.statdisplay.*;
import smartin.miapi.stat.api.StatAggregator;
import smartin.miapi.stat.api.StatGroup;
import smartin.miapi.stat.api.StatValue;
import smartin.miapi.stat.api.data.attribute.AttributeStatData;
import smartin.miapi.stat.api.data.mining.MiningStatData;
import smartin.miapi.stat.api.data.number.DoubleStatData;
import smartin.miapi.stat.text.*;

import java.util.*;

public final class StandardUIImpl {

    private static final List<TextFormatter<?, ?>> FORMATTERS = List.of(
            new BooleanTextFormatter(),
            new DoubleTextFormatter(),
            new AttributeTextFormatter(),
            new MiningTextFormatter()
    );

    private StandardUIImpl() {
    }

    public static void setup() {
        StatListWidget.addStatDisplaySupplierNew(new StatListWidget.StatWidgetSupplier() {
            @Override
            public <T extends InteractAbleWidget & SingleStatDisplay> List<T> currentList(ItemStack original, ItemStack compareTo) {
                List<T> list = new ArrayList<>();
                List<Tuple<StatGroup, List<StatValue.StatWithValues<?, ?>>>> stats = StatAggregator.findAllStats(original, compareTo);
                stats.forEach(groupTuple -> {
                    groupTuple.getB().forEach(stat -> {
                        if (stat.stat().metaData() instanceof DoubleStatData doubleStatData) {
                            list.add(
                                    (T) DoubleResolvableStatDisplay
                                            .builder((stack) -> Optional.ofNullable(doubleStatData.getData(stack)))
                                            .setHoverDescription((item) -> stat.stat().getDescription(item))
                                            .setName((item) -> stat.stat().getName(item))
                                            .setInverse(doubleStatData.inverse())
                                            .setFormat(doubleStatData.format().toPattern())
                                            .setCondition(doubleStatData::shouldBeVisible)
                                            .build());
                        }
                        if (stat.stat().metaData() instanceof BooleanStatDisplay booleanData) {
                            list.add((T)
                                    SimpleBooleanStatDisplay
                                            .builder((stack) -> booleanData.getValue(stack) == 0)
                                            .setHoverDescription((item) -> stat.stat().getDescription(item))
                                            .setName((item) -> stat.stat().getName(item))
                                            .build());
                        }
                        if (stat.stat().metaData() instanceof AttributeStatData attributeStatData) {
                            Arrays.stream(AttributeSingleDisplay
                                    .builder(attributeStatData.attribute())
                                    .setHoverDescription((item) -> stat.stat().getDescription(item))
                                    .setName((item) -> stat.stat().getName(item))
                                    .setFormat(attributeStatData.format().toPattern())
                                    .build()).forEach(attributeSingleDisplay -> {
                                list.add((T) attributeSingleDisplay);
                            });
                        }

                        if (stat.stat().metaData() instanceof MiningStatData miningStatData) {
                            list.add(
                                    (T) new MiningPropertyStatDisplay(miningStatData.type()));
                        }
                    });
                });
                return list;
            }
        });
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> handStats = Commands.literal("miapi")
                .then(Commands.literal("stat"))
                .then(Commands.literal("mainHand")
                        .executes(StandardUIImpl::executeStat));

        LiteralArgumentBuilder<CommandSourceStack> compareStatsCommand = Commands.literal("miapi")
                .then(Commands.literal("stat"))
                .then(Commands.literal("compareHands")
                        .executes(StandardUIImpl::executeCompare));

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
                        component.append(format(stat, player.getMainHandItem()));
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
                    component.append(groupTuple.getA().getName().getString());
                    component.append("\n");
                    groupTuple.getB().forEach(stat -> {
                        component.append("   ");
                        component.append(format(stat, player.getMainHandItem()));
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


    public static Component format(StatValue.StatWithValues<?, ?> stat, ItemStack base) {
        for (TextFormatter<?, ?> formatter : FORMATTERS) {
            if (formatter.type() == stat.stat().type()) {
                return formatTyped(formatter, stat, base);
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
            StatValue.StatWithValues<?, ?> stat,
            ItemStack baseItem
    ) {
        return formatter.format(stat, baseItem);
    }

    private static String valueString(Object value) {
        return value == null ? "null" : value.toString();
    }
}