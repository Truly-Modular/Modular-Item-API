package smartin.miapi.client.gui.crafting.crafter;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.ColorHelper;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.*;
import smartin.miapi.modules.properties.material.Material;
import smartin.miapi.modules.properties.material.MaterialProperty;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class MaterialDetailView extends InteractAbleWidget {
    private ItemStack itemStack;
    private Consumer<Object> back;
    private ScrollingTextWidget header;
    private Material material;
    private Identifier texture = new Identifier(Miapi.MOD_ID, "textures/gui/crafter/material_detail_background.png");
    private float scale = 1.9f;
    public static List<Builder> infoBarBuilders = new ArrayList<>();

    static {
        registerBuilder(
                new Builder("flexibility")
        );
        registerBuilder(
                new Builder("density")
        );
        registerBuilder(
                new Builder("hardness")
        );
        registerBuilder(
                new Builder("durability").setMax(2000).setFormat("##")
        );
        registerBuilder(
                new Builder("mining_speed")
        );
        registerBuilder(
                new Builder("mining_level").setMax(4).setFormat("##")
        );
    }

    public MaterialDetailView(int x, int y, int width, int height, ItemStack stack, Consumer<Object> back) {
        super(x, y, width, height, Text.empty());
        this.itemStack = stack;
        this.back = back;
        this.material = MaterialProperty.getMaterial(stack);
        TransformableWidget headerScaler = new TransformableWidget(this.getX(), this.getY(), this.getWidth(), this.getHeight(), scale);
        this.header = new ScrollingTextWidget((int) ((x + 5) * (1 / scale)), (int) ((y + 5) * (1 / scale)), width, Text.translatable(material.getData("translation")), ColorHelper.Argb.getArgb(255, 255, 255, 255));
        headerScaler.addChild(header);
        this.addChild(headerScaler);
        int spacer = 13;
        List<InteractAbleWidget> widgets = new ArrayList<>();
        widgets.add(new ColorWidget(this.getX(), this.getY(), this.getWidth(), spacer, material.getColor()));
        for (Builder builder : infoBarBuilders) {
            widgets.add(new InfoBar(x + 10, y, width - 10, spacer, Text.translatable(Miapi.MOD_ID + ".material_stat." + builder.key), (float) material.getDouble(builder.key), builder.min, builder.max, builder.format));
        }
        ScrollList list = new ScrollList(x + 10, y + 30, width - 10, this.getHeight() - 30, widgets);
        this.addChild(list);
    }

    public static void registerBuilder(Builder builder) {
        infoBarBuilders.add(builder);
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        drawTextureWithEdge(drawContext, texture, getX(), getY(), getWidth(), getHeight(), 160, 190, 10);
        drawTextureWithEdge(drawContext, new Identifier(material.getData("icon")), (int) (getX() + 5 + header.getRequiredWidth() * scale), getY() + 5, 16, 16, 16, 16, 0);
        super.render(drawContext, mouseX, mouseY, delta);
    }

    private static class ColorWidget extends InteractAbleWidget {
        ScrollingTextWidget textWidget;
        public static int textWidth = 70;
        int color;

        public ColorWidget(int x, int y, int width, int height, int color) {
            super(x, y, width, height, Text.empty());
            textWidget = new ScrollingTextWidget(x, y, textWidth, Text.translatable(Miapi.MOD_ID + ".material_stat.color"), ColorHelper.Argb.getArgb(255, 255, 255, 255));
            this.addChild(textWidget);
            this.color = color;
        }

        @Override
        public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
            textWidget.setX(this.getX());
            textWidget.setY(this.getY());
            super.render(drawContext, mouseX, mouseY, delta);
            color |= (0xFF << 24);
            drawContext.fill(this.getX()+textWidth, this.getY(), this.getX()+textWidth + 10, this.getY() + 10, color);

        }
    }

    public static class InfoBar extends InteractAbleWidget {
        public StatBar statBar;
        public ScrollingTextWidget textWidget;
        public static int textWidth = 70;
        public static int barWitdh = 60;
        public static int spacer = 2;
        public ScrollingTextWidget valueHolder;

        public InfoBar(int x, int y, int width, int height, Text text, float value, float min, float max) {
            this(x, y, width, height, text, value, min, max, "##.##");
        }

        public InfoBar(int x, int y, int width, int height, Text text, float value, float min, float max, String format) {
            super(x, y, width, height, Text.empty());
            max = Math.max(value, max);
            min = Math.min(value, min);
            this.textWidget = new ScrollingTextWidget(x, y, textWidth, text, ColorHelper.Argb.getArgb(255, 255, 255, 255));
            this.statBar = new StatBar(x + textWidth + spacer, y + 3, barWitdh, 2, ColorHelper.Argb.getArgb(255, 0, 0, 0));
            DecimalFormat modifierFormat = Util.make(new DecimalFormat(format), (decimalFormat) -> {
                decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
            });
            float primaryPercent = (value - min) / (max - min);
            statBar.setPrimary(primaryPercent, ColorHelper.Argb.getArgb(255, 255, 255, 255));
            statBar.setSecondary(primaryPercent, ColorHelper.Argb.getArgb(255, 255, 255, 255));
            this.valueHolder = new ScrollingTextWidget(x + textWidth + barWitdh + spacer * 2, y, width - textWidth + barWitdh, Text.literal(modifierFormat.format(value)), ColorHelper.Argb.getArgb(255, 255, 255, 255));
            this.addChild(textWidget);
            this.addChild(statBar);
            this.addChild(valueHolder);
        }

        @Override
        public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
            textWidget.setX(this.getX());
            textWidget.setY(this.getY());
            statBar.setX(getX() + textWidth);
            statBar.setY(getY() + 3);
            valueHolder.setX(getX() + textWidth + barWitdh + spacer * 2);
            valueHolder.setY(getY());
            super.render(drawContext, mouseX, mouseY, delta);

        }
    }

    public static class Builder {
        public String key;
        public int min = 0;
        public int max = 10;
        public String format = "##.##";

        public Builder(String key) {
            this.key = key;
        }

        public Builder setMax(int max) {
            this.max = max;
            return this;
        }

        public Builder setMin(int min) {
            this.min = min;
            return this;
        }

        public Builder setFormat(String format) {
            this.format = format;
            return this;
        }
    }
}
