package smartin.miapi.client.gui.crafting.statdisplay.attributes;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.ColorHelper;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.ScrollingTextWidget;
import smartin.miapi.client.gui.StatBar;
import smartin.miapi.client.gui.crafting.statdisplay.SingleStatDisplay;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class AttributeSingleDisplay extends InteractAbleWidget implements SingleStatDisplay {
    final EntityAttribute attribute;
    final EquipmentSlot slot;
    final Identifier texture = new Identifier("textures/gui/container/inventory.png");
    ItemStack original = ItemStack.EMPTY;
    ItemStack compareTo = ItemStack.EMPTY;
    final StatBar statBar;
    final ScrollingTextWidget currentValue;
    final ScrollingTextWidget compareValue;
    final ScrollingTextWidget centerValue;
    final ScrollingTextWidget textWidget;
    public final DecimalFormat modifierFormat;
    Text text;
    double defaultValue;

    private AttributeSingleDisplay(EntityAttribute attribute, EquipmentSlot slot, Text text, Text hover, double defaultValue, String format) {
        super(0, 0, 80, 32, Text.empty());
        this.slot = slot;
        this.attribute = attribute;
        this.defaultValue = defaultValue;
        this.text = text;
        modifierFormat = Util.make(new DecimalFormat(format), (decimalFormat) -> {
            decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
        });
        textWidget = new ScrollingTextWidget(x, y, 80, text, ColorHelper.Argb.getArgb(255, 255, 255, 255));
        currentValue = new ScrollingTextWidget(x, y, 50, text, ColorHelper.Argb.getArgb(255, 255, 255, 255));
        centerValue = new ScrollingTextWidget(x, y, (int) ((80 - 10) * (1.0 / 1.2)), text, ColorHelper.Argb.getArgb(255, 255, 255, 255));
        centerValue.setOrientation(ScrollingTextWidget.ORIENTATION.CENTERED);
        compareValue = new ScrollingTextWidget(x, y, (int) ((80 - 10) * (1.0 / 1.2)), text, ColorHelper.Argb.getArgb(255, 255, 255, 255));
        compareValue.setOrientation(ScrollingTextWidget.ORIENTATION.RIGHT);
        statBar = new StatBar(0, 0, width, 10, ColorHelper.Argb.getArgb(255, 0, 0, 0));
    }

    @Override
    public boolean shouldRender(ItemStack original, ItemStack compareTo) {
        this.original = original;
        this.compareTo = compareTo;
        if (original.getAttributeModifiers(slot).containsKey(attribute)) {
            return true;
        }
        if (compareTo.getAttributeModifiers(slot).containsKey(attribute)) {
            return true;
        }
        return false;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        double oldValue = AttributeRegistry.getAttribute(original, attribute, slot, defaultValue);
        double compareToValue = AttributeRegistry.getAttribute(compareTo, attribute, slot, defaultValue);
        double higher = Math.max(oldValue, compareToValue) * 1.2;


        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.setShaderTexture(0, texture);

        drawTextureWithEdge(matrices, x, y, 0, 166, 120, 32, width, height, 256, 256, 2);

        textWidget.x = this.x + 5;
        textWidget.y = this.y + 5;
        textWidget.setWidth(this.width - 8);

        statBar.x = this.x + 5;
        statBar.y = this.y + 25;
        statBar.setWidth(this.width - 10);
        statBar.setHeight(3);
        String currentValueString = String.valueOf(oldValue);
        String centerText = "";
        if (oldValue < compareToValue) {
            statBar.setPrimary(oldValue / higher, ColorHelper.Argb.getArgb(255, 255, 255, 255));
            statBar.setSecondary(compareToValue / higher, ColorHelper.Argb.getArgb(255, 0, 255, 0));
            compareValue.textColor = ColorHelper.Argb.getArgb(255, 0, 255, 0);
        } else {
            statBar.setPrimary(compareToValue / higher, ColorHelper.Argb.getArgb(255, 255, 255, 255));
            statBar.setSecondary(oldValue / higher, ColorHelper.Argb.getArgb(255, 255, 0, 0));
            compareValue.textColor = ColorHelper.Argb.getArgb(255, 255, 0, 0);
        }
        if (oldValue == compareToValue) {
            compareValue.textColor = ColorHelper.Argb.getArgb(0, 255, 0, 0);
        } else {
            compareValue.x = this.x + 5;
            compareValue.y = this.y + 15;
            compareValue.setText(Text.of(modifierFormat.format(compareToValue)));
            compareValue.render(matrices, mouseX, mouseY, delta);
            centerText = "→";
        }
        currentValue.x = this.x + 5;
        currentValue.y = this.y + 15;
        currentValue.setText(Text.literal(modifierFormat.format(oldValue)));
        currentValue.render(matrices, mouseX, mouseY, delta);
        centerValue.x = this.x + 5;
        centerValue.y = this.y + 15;
        centerValue.setText(Text.literal(centerText));
        centerValue.render(matrices, mouseX, mouseY, delta);
        statBar.render(matrices, mouseX, mouseY, delta);
        textWidget.render(matrices, mouseX, mouseY, delta);
    }

    public static Builder Builder(EntityAttribute attribute){
        return new Builder(attribute);
    }

    public static class Builder {
        EntityAttribute attribute;
        public double defaultValue = 1;
        public Text name;
        public Text hoverDescription;
        public EquipmentSlot slot = EquipmentSlot.MAINHAND;
        public String format = "##.##";

        private Builder(EntityAttribute attribute) {
            this.attribute = attribute;
        }

        public Builder setDefault(double defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder setName(Text name) {
            this.name = name;
            return this;
        }

        public Builder setSlot(EquipmentSlot slot) {
            this.slot = slot;
            return this;
        }

        public Builder setHoverDescription(Text hoverDescription) {
            this.hoverDescription = hoverDescription;
            return this;
        }

        public Builder setFormat(String format) {
            this.format = format;
            return this;
        }

        public AttributeSingleDisplay build() {
            // Validate the required fields
            if (name == null) {
                throw new IllegalStateException("Name is required");
            }
            if (attribute == null) {
                throw new IllegalStateException("Attribute is required");
            }

            // Create an instance of AttributeProperty with the builder values
            return new AttributeSingleDisplay(attribute, slot, name, hoverDescription, defaultValue, format);
        }
    }
}
