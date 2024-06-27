package smartin.miapi.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
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
    public List<FormattedCharSequence> lines;
    public ClientTooltipPositioner positioner = AbsoluteTooltipPositioner.INSTANCE;
    public int maxWidth = -1;

    public HoverDescription(int x, int y, Component unified) {
        super(x, y, 0, 0, Component.empty());
        lines = new ArrayList<>();
        for (String s : unified.getString().split("\\r?\\n")) {
            lines.add(Component.literal(s).getVisualOrderText());
        }
        updateSize(lines);
    }
    public HoverDescription(int x, int y, List<Component> text) {
        super(x, y, 0, 0, Component.empty());
        lines = toOrderedText(text);
        updateSize(lines);
    }
    public List<FormattedCharSequence> toOrderedText(List<? extends FormattedText> list) {
        return list.stream().map(this::toOrderedText).flatMap(Collection::stream).collect(Collectors.toCollection(ArrayList::new));
    }
    public List<FormattedCharSequence> toOrderedText(FormattedText visitable) {
        return Minecraft.getInstance().font.split(visitable, maxWidth == -1 ? 10000 : maxWidth);
    }
    public void updateSize(List<FormattedCharSequence> lines) {
        int w = 0;
        int h = lines.size() == 1 ? -2 : 0;
        for (int e = 0; e < lines.size(); e++) {
            FormattedCharSequence text = lines.get(e);
            ClientTooltipComponent tooltipComponent = ClientTooltipComponent.create(text);
            int compWidth = tooltipComponent.getWidth(Minecraft.getInstance().font);
            if (compWidth > w) {
                w = compWidth;
            }
            h += tooltipComponent.getHeight();
        }
        width = w;
        height = h;
    }

    public void setText(Component unified) {
        lines = new ArrayList<>();
        for (String s : unified.getString().split("\\r?\\n")) {
            lines.addAll(toOrderedText(Component.literal(s)));
        }
        updateSize(lines);
    }
    public void setText(List<Component> text) {
        List<FormattedCharSequence> ordered = toOrderedText(text);
        lines = ordered;
        updateSize(ordered);
    }
    public void addText(Component text) {
        List<FormattedCharSequence> orderedText = toOrderedText(text);
        lines.addAll(orderedText);
        updateSize(orderedText);
    }
    public void addText(List<Component> text) {
        List<FormattedCharSequence> ordered = toOrderedText(text);
        lines.addAll(ordered);
        updateSize(ordered);
    }
    public void reset() {
        lines.clear();
    }

    @Override
    public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
        context.renderTooltip(
                Minecraft.getInstance().font,
                lines, positioner,
                this.getX(), this.getY()
                );
    }

    public static class AbsoluteTooltipPositioner implements ClientTooltipPositioner {
        public static final AbsoluteTooltipPositioner INSTANCE = new AbsoluteTooltipPositioner();

        protected AbsoluteTooltipPositioner(){

        }

        @Override
        public Vector2ic positionTooltip(int screenWidth, int screenHeight, int x, int y, int width, int height) {
            return new Vector2i(x, y).add(4, 4);
        }
    }
}
