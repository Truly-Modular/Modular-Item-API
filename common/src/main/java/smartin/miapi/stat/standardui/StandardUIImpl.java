package smartin.miapi.stat.standardui;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;
import smartin.miapi.client.MiapiClient;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.crafting.statdisplay.*;
import smartin.miapi.mixin.client.KeyMappingAccessor;
import smartin.miapi.modules.properties.LoreProperty;
import smartin.miapi.stat.api.StatAggregator;
import smartin.miapi.stat.api.StatGroup;
import smartin.miapi.stat.api.StatValue;
import smartin.miapi.stat.api.data.attribute.AttributeStatData;
import smartin.miapi.stat.api.data.mining.MiningStatData;
import smartin.miapi.stat.api.data.number.DoubleStatData;
import smartin.miapi.stat.text.TextUI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public final class StandardUIImpl {

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
                                            .setName((item) -> stat.stat().getNameWithPrefix(item))
                                            .setInverse(doubleStatData.inverse())
                                            .setFormat(doubleStatData.format().toPattern())
                                            .setCondition(doubleStatData::shouldBeVisible)
                                            .setMax(doubleStatData.max())
                                            .setMin(doubleStatData.min())
                                            .build());
                        }
                        if (stat.stat().metaData() instanceof BooleanStatDisplay booleanData) {
                            list.add((T)
                                    SimpleBooleanStatDisplay
                                            .builder((stack) -> booleanData.getValue(stack) == 0)
                                            .setHoverDescription((item) -> stat.stat().getDescription(item))
                                            .setName((item) -> stat.stat().getNameWithPrefix(item))
                                            .build());
                        }
                        if (stat.stat().metaData() instanceof AttributeStatData attributeStatData) {
                            Arrays.stream(AttributeSingleDisplay
                                    .builder(attributeStatData.attribute())
                                    .setHoverDescription((item) -> stat.stat().getDescription(item))
                                    .setName((item) -> stat.stat().getNameWithPrefix(item))
                                    .setFormat(attributeStatData.format().toPattern())
                                    .setMax(attributeStatData.max())
                                    .setMin(attributeStatData.min())
                                    .setDefault(attributeStatData.defaultValue())
                                    .setFallback(attributeStatData.fallbackValue())
                                    .setSlot(attributeStatData.slot())
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
        setupClientKeybind();
    }

    public static void setupClientKeybind() {
        LoreProperty.bottomLoreSuppliers.add(new LoreProperty.LoreSupplier() {
            @Override
            public List<Component> getLore(ItemStack itemStack) {
                long window = Minecraft.getInstance().getWindow().getWindow();
                if (GLFW.glfwGetKey(window, ((KeyMappingAccessor) MiapiClient.HOVER_COMPARE_BINDING).getMiapiKey().getValue()) == GLFW.GLFW_PRESS) {
                    Player player = Minecraft.getInstance().player;
                    EquipmentSlot slot = Minecraft.getInstance().player.getEquipmentSlotForItem(itemStack);
                    ItemStack equiped = player.getItemBySlot(slot);
                    List<Component> lines = new ArrayList<>();
                    lines.add(Component.translatable("miapi.ui.stat.compare", equiped.getDisplayName(), itemStack.getDisplayName()));
                    lines.addAll(TextUI.getStatComponentList(equiped, itemStack, false));
                    return lines;
                }
                if (GLFW.glfwGetKey(window, ((KeyMappingAccessor) MiapiClient.HOVER_DETAIL_BINDING).getMiapiKey().getValue()) == GLFW.GLFW_PRESS) {
                    return TextUI.getStatComponentList(itemStack, itemStack, false);
                }
                return List.of();
            }
        });
    }

    private static EquipmentSlot fromGroup(EquipmentSlotGroup group) {
        List<EquipmentSlot> slots = List.of(EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD);
        for (EquipmentSlot slot : slots) {
            if (group.test(slot)) {
                return slot;
            }
        }
        return EquipmentSlot.MAINHAND;
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> handStats = Commands.literal("miapi")
                .then(Commands.literal("stat")
                        .then(Commands.literal("mainHand")
                                .executes(StandardUIImpl::executeStat)));

        LiteralArgumentBuilder<CommandSourceStack> compareStatsCommand = Commands.literal("miapi")
                .then(Commands.literal("stat")
                        .then(Commands.literal("compareHands")
                                .executes(StandardUIImpl::executeCompare)));

        LiteralArgumentBuilder<CommandSourceStack> compareStatsCommandDiff = Commands.literal("miapi")
                .then(Commands.literal("stat")
                        .then(Commands.literal("compareHandsOnlyDiff")
                                .executes(StandardUIImpl::executeCompareDiff)));

        dispatcher.register(handStats);
        dispatcher.register(compareStatsCommand);
        dispatcher.register(compareStatsCommandDiff);
    }

    private static int executeStat(CommandContext<CommandSourceStack> context) {
        if (context.getSource().isPlayer()) {
            Player player = context.getSource().getPlayer();
            context.getSource().sendSuccess(() -> TextUI.getStatComponent(Component.literal("Mainhand -> Offhand"), player.getMainHandItem(), player.getMainHandItem(), false), true);
            return 0;
        } else {
            return 1;
        }
    }

    private static int executeCompare(CommandContext<CommandSourceStack> context) {
        if (context.getSource().isPlayer()) {
            Player player = context.getSource().getPlayer();
            context.getSource().sendSuccess(() -> TextUI.getStatComponent(Component.literal("Mainhand -> Offhand"), player.getMainHandItem(), player.getOffhandItem(), false), true);
            return 0;
        } else {
            return 1;
        }
    }

    private static int executeCompareDiff(CommandContext<CommandSourceStack> context) {
        if (context.getSource().isPlayer()) {
            Player player = context.getSource().getPlayer();
            context.getSource().sendSuccess(() -> TextUI.getStatComponent(Component.literal("Mainhand -> Offhand"), player.getMainHandItem(), player.getOffhandItem(), true), true);
            return 0;
        } else {
            return 1;
        }
    }
}