package smartin.miapi.client.gui.crafting.statdisplay;

import com.mojang.blaze3d.systems.RenderSystem;
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
import smartin.miapi.modules.properties.util.ComponentDescriptionable;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Environment(EnvType.CLIENT)
public abstract class MultiComponentStatDisplay extends InteractAbleWidget implements SingleStatDisplay, Drawable {
    public Identifier texture = new Identifier("textures/gui/container/inventory.png");
    public ScrollingTextWidget textWidget;
    public DecimalFormat modifierFormat;
    public StatDisplay.TextGetter title;
    public StatDisplay.MultiTextGetter hover;
    public HoverDescription hoverDescription;
    public int scrollPosition;
    protected ItemStack original = ItemStack.EMPTY;
    protected ItemStack compareTo = ItemStack.EMPTY;
    public List<ComponentHolder> components = new ArrayList<>();
    public int maxScrollPositon;

    protected MultiComponentStatDisplay(int x, int y, int width, int height, StatDisplay.TextGetter title, StatDisplay.MultiTextGetter hover) {
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
        ItemStack mainStack = compareTo.isEmpty() ? original : compareTo;
        this.original = original;
        this.compareTo = compareTo;
        textWidget.setText(title.resolve(mainStack));
        if (hover != null) hoverDescription.setText(hover.resolve(mainStack));
        components = getComponents(original, compareTo);
        return true;
    }

    public abstract List<ComponentHolder> getComponents(ItemStack original, ItemStack compareTo);

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        boolean bl = super.mouseScrolled(mouseX, mouseY, amount);
        if (bl) return true;

        if (components == null || components.isEmpty()) return false;

        int inc = amount > 0 ? -1 : 1;
        scrollPosition = Math.min(maxScrollPositon, Math.max(0, scrollPosition+inc));
        shouldRender(original, compareTo);
        return true;
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);

        drawTextureWithEdge(drawContext, texture, getX(), getY(), 0, 166, 120, 32, width, height, 256, 256, 2);

        textWidget.setX(this.getX() + 5);
        textWidget.setY(this.getY() + 5);
        textWidget.setWidth(this.width - 8);

        int spacingBuffer = 0;
        for (ComponentHolder component : components) {
            int xPos = getX()+5+spacingBuffer;
            int yPos = getY()+18;
            component.move(xPos, yPos);
            component.render(drawContext, mouseX, mouseY, delta);
            component.move(-xPos, -yPos);
            spacingBuffer+=component.getSpacing();
        }
        textWidget.render(drawContext, mouseX, mouseY, delta);
    }

    public int getScrollPosition() {
        return scrollPosition;
    }

    @Override
    public int getHeightDesired() {
        return 100;
    }

    @Override
    public int getWidthDesired() {
        return 130;
    }

    public InteractAbleWidget getHoverWidget() {
        return hoverDescription;
    }

    public static class ComponentHolder {
        public SimpleTextWidget prefix;
        public ScrollingTextWidget text;
        public TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        protected ComponentHolder() {}
        public ComponentHolder(SimpleTextWidget prefix, ScrollingTextWidget text) {
            this.prefix = prefix;
            this.text = text;
        }
        public ComponentHolder(Text prefix, Text scrolling, int maxWidth) {
            int prefixWidth = textRenderer.getWidth(prefix);
            if (maxWidth < 0)
                maxWidth = textRenderer.getWidth(scrolling);
            this.prefix = new SimpleTextWidget(0, 0, prefixWidth, 9, prefix);
            this.text = new ScrollingTextWidget(prefixWidth+2, 0, maxWidth, scrolling);
        }

        public int getSpacing() {
            return prefix.getWidth() + text.getWidth() + 8;
        }

        public void move(int x, int y) {
            prefix.setX(prefix.getX()+x);
            prefix.setY(prefix.getY()+y);
            text.setX(text.getX()+x);
            text.setY(text.getY()+y);
        }

        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            prefix.render(context, mouseX, mouseY, delta);
            text.render(context, mouseX, mouseY, delta);
        }

        public static ComponentHolder fromDescHolder(ComponentDescriptionable.DescriptionHolder desc) {
            return new ComponentHolder(desc.prefix(), desc.scrolling(), desc.scrollMaxWidth());
        }
    }
}
