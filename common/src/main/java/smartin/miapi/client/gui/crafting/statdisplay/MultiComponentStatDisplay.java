package smartin.miapi.client.gui.crafting.statdisplay;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.ColorHelper;
import smartin.miapi.client.gui.HoverDescription;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.ScrollingTextWidget;
import smartin.miapi.client.gui.SimpleTextWidget;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class MultiComponentStatDisplay extends InteractAbleWidget implements SingleStatDisplay, Drawable {
    public Identifier texture = new Identifier("textures/gui/container/inventory.png");
    public ScrollingTextWidget textWidget;
    public DecimalFormat modifierFormat;
    public StatDisplay.TextGetter title;
    public StatDisplay.TextGetter hover;
    public HoverDescription hoverDescription;
    public List<ComponentHolder> components = new ArrayList<>();

    public MultiComponentStatDisplay(int x, int y, int width, int height, StatDisplay.TextGetter title, StatDisplay.TextGetter hover) {
        super(x, y, width, height, Text.empty());
        this.title = title;
        this.hover = hover;
        textWidget = new ScrollingTextWidget(x, y, 80, Text.empty(), ColorHelper.Argb.getArgb(255, 255, 255, 255));
        modifierFormat = Util.make(new DecimalFormat("##.##"), (decimalFormat) -> {
            decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
        });
        hoverDescription = new HoverDescription(x, y, List.of());
    }

    public boolean shouldRender(ItemStack original, ItemStack compareTo) {
        textWidget.setText(title.resolve(original));
        components = getComponents(original, compareTo);
        return true;
    }

    public List<ComponentHolder> getComponents(ItemStack original, ItemStack compareTo) {
        return List.of(
                new ComponentHolder(Text.literal("🧪"), Text.literal("Poison"), 30, ColorHelper.Argb.getArgb(255, 0, 200, 0), 45),
                new ComponentHolder(Text.literal("⌚"), Text.literal("20s"), 20, ColorHelper.Argb.getArgb(255, 255, 255, 255), 25)
        );
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);

        drawTextureWithEdge(drawContext, texture, getX(), getY(), 0, 166, 120, 32, width, height, 256, 256, 2);

        textWidget.setX(this.getX() + 5);
        textWidget.setY(this.getY() + 5);
        textWidget.setWidth(this.width - 8);

        drawContext.getMatrices().push();
        drawContext.getMatrices().translate(getX() + 5, getY() + 18, 0);
        for (ComponentHolder component : components) {
            component.render(drawContext, mouseX, mouseY, delta);

            drawContext.getMatrices().translate(component.spacing, 0, 0);
        }
        drawContext.getMatrices().pop();
        textWidget.render(drawContext, mouseX, mouseY, delta);
    }

    @Override
    public int getHeightDesired() {
        return 32;
    }

    @Override
    public int getWidthDesired() {
        return 160;
    }

    public InteractAbleWidget getHoverWidget() {
        return hoverDescription;
    }

    public static class ComponentHolder {
        public SimpleTextWidget prefix;
        public ScrollingTextWidget text;
        public int spacing;
        public TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        public ComponentHolder(SimpleTextWidget prefix, ScrollingTextWidget text, int spacing) {
            this.prefix = prefix;
            this.text = text;
            this.spacing = spacing;
        }
        public ComponentHolder(Text prefix, Text scrolling, int maxWidth, int color, int spacing) {
            int prefixWidth = textRenderer.getWidth(prefix);
            this.prefix = new SimpleTextWidget(0, 0, prefixWidth, 5, prefix);
            this.text = new ScrollingTextWidget(prefixWidth+2, 0, maxWidth, scrolling, color);
            this.spacing = spacing;
        }


        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            prefix.render(context, mouseX, mouseY, delta);
            text.render(context, mouseX, mouseY, delta);
        }
    }
}
