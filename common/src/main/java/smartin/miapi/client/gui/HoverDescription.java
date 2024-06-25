package smartin.miapi.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A hover Element blueprint, rendering in the Hover Layer
 */
@Environment(EnvType.CLIENT)
public class HoverDescription extends InteractAbleWidget {
    public List<OrderedText> lines;
    public TooltipPositioner positioner = AbsoluteTooltipPositioner.INSTANCE;
    public int maxWidth = -1;

    public HoverDescription(int x, int y, Text unified) {
        super(x, y, 0, 0, Text.empty());
        lines = new ArrayList<>();
        for (String s : unified.getString().split("\\r?\\n")) {
            lines.add(Text.literal(s).asOrderedText());
        }
        updateSize(lines);
    }
    public HoverDescription(int x, int y, List<Text> text) {
        super(x, y, 0, 0, Text.empty());
        lines = toOrderedText(text);
        updateSize(lines);
    }
    public List<OrderedText> toOrderedText(List<? extends StringVisitable> list) {
        return list.stream().map(this::toOrderedText).flatMap(Collection::stream).collect(Collectors.toCollection(ArrayList::new));
    }
    public List<OrderedText> toOrderedText(StringVisitable visitable) {
        return MinecraftClient.getInstance().textRenderer.wrapLines(visitable, maxWidth == -1 ? 10000 : maxWidth);
    }
    public void updateSize(List<OrderedText> lines) {
        int w = 0;
        int h = lines.size() == 1 ? -2 : 0;
        for (int e = 0; e < lines.size(); e++) {
            OrderedText text = lines.get(e);
            TooltipComponent tooltipComponent = TooltipComponent.of(text);
            int compWidth = tooltipComponent.getWidth(MinecraftClient.getInstance().textRenderer);
            if (compWidth > w) {
                w = compWidth;
            }
            h += tooltipComponent.getHeight();
        }
        width = w;
        height = h;
    }

    public void setText(Text unified) {
        lines = new ArrayList<>();
        for (String s : unified.getString().split("\\r?\\n")) {
            lines.addAll(toOrderedText(Text.literal(s)));
        }
        updateSize(lines);
    }
    public void setText(List<Text> text) {
        List<OrderedText> ordered = toOrderedText(text);
        lines = ordered;
        updateSize(ordered);
    }
    public void addText(Text text) {
        List<OrderedText> orderedText = toOrderedText(text);
        lines.addAll(orderedText);
        updateSize(orderedText);
    }
    public void addText(List<Text> text) {
        List<OrderedText> ordered = toOrderedText(text);
        lines.addAll(ordered);
        updateSize(ordered);
    }
    public void reset() {
        lines.clear();
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        context.drawTooltip(
                MinecraftClient.getInstance().textRenderer,
                lines, positioner,
                this.getX(), this.getY()
                );
    }

    public static class AbsoluteTooltipPositioner implements TooltipPositioner {
        public static final AbsoluteTooltipPositioner INSTANCE = new AbsoluteTooltipPositioner();

        protected AbsoluteTooltipPositioner(){

        }

        @Override
        public Vector2ic getPosition(int screenWidth, int screenHeight, int x, int y, int width, int height) {
            return new Vector2i(x, y).add(4, 4);
        }
    }
}
