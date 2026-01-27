package smartin.miapi.client.gui.crafting.statdisplay;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.client.gui.ParentHandledScreen;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public interface DoubleResolveableDisplay {
    default List<Component> getLinesForDouble(@Nullable DoubleOperationResolvable resolvable) {
        List<Component> list = new ArrayList();
        if (resolvable != null) {
            if (ParentHandledScreen.hasShiftDown()) {
                if (resolvable != null) {
                    resolvable.operations.forEach(operation1 -> {
                        if (operation1.solve() != 0) {
                            list.add(Component.literal(SinglePropertyStatDisplay.stringForOperation(getHoverFormat(), operation1)).withStyle(ChatFormatting.GRAY));
                            if (ParentHandledScreen.hasAltDown()) {
                                operation1.source.ifPresent(list::add);
                                resolvable.getResolvedTrace();
                                list.addAll(getLinesForDouble(StatResolver.resolveDoubleWithTrace(operation1.value, operation1.instance)));
                            }
                        }
                    });
                }
                list.add(Component.translatable("miapi.ui.stat_detail.shift_alt").withStyle(ChatFormatting.DARK_GRAY));
            } else {
                list.add(Component.translatable("miapi.ui.stat_detail.shift").withStyle(ChatFormatting.DARK_GRAY));
            }
        }
        return list;
    }

    DecimalFormat getHoverFormat();

    default List<Component> getLinesForDouble(@Nullable StatResolver.ResolvedDouble resolved) {
        List<Component> lines = new ArrayList<>();

        if (resolved == null || resolved.trace() == null) {
            return lines;
        }

        if (!ParentHandledScreen.hasShiftDown()) {
            lines.add(Component.translatable("miapi.ui.stat_detail.shift")
                    .withStyle(ChatFormatting.DARK_GRAY));
            return lines;
        }

        boolean deep = ParentHandledScreen.hasAltDown();

        renderTrace(
                resolved.trace(),
                lines,
                0,
                deep ? Integer.MAX_VALUE : 1
        );

        lines.add(
                Component.translatable(
                        deep
                                ? "miapi.ui.stat_detail.shift_alt_depth"
                                : "miapi.ui.stat_detail.shift_alt_depth_more"
                ).withStyle(ChatFormatting.DARK_GRAY)
        );

        return lines;
    }

    default void renderTrace(
            StatResolver.TraceNode node,
            List<Component> out,
            int depth,
            int maxDepth
    ) {
        if (depth > maxDepth) {
            return;
        }

        String indent = "  ".repeat(depth);

        if (node instanceof StatResolver.TraceValue value) {
            out.add(Component.literal(
                    indent + value.label().getString() + " = " + getHoverFormat().format(value.value())
            ).withStyle(ChatFormatting.DARK_GRAY));
            return;
        }

        if (node instanceof StatResolver.TraceValueWithSource value) {
            out.add(Component.literal(
                    indent + value.label().getString() + " = " + getHoverFormat().format(value.value()) + " " + value.source().getString()
            ).withStyle(ChatFormatting.DARK_GRAY));
            return;
        }

        if (node instanceof StatResolver.TraceReference ref) {
            out.add(Component.literal(
                    indent + ref.key() + " = " + getHoverFormat().format(ref.resolvedValue())
            ).withStyle(ChatFormatting.GRAY));

            if (ref.resolvedFrom() != null) {
                renderTrace(ref.resolvedFrom(), out, depth + 1, maxDepth);
            }
            return;
        }

        if (node instanceof StatResolver.TraceOperation op) {
            out.add(Component.literal(
                    indent + op.expression() + " = " + getHoverFormat().format(op.result())
            ).withStyle(ChatFormatting.GRAY));

            for (StatResolver.TraceNode child : op.children()) {
                renderTrace(child, out, depth + 1, maxDepth);
            }
        }
    }
}
