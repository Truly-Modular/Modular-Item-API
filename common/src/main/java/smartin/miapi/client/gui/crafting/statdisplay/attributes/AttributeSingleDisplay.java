package smartin.miapi.client.gui.crafting.statdisplay.attributes;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.crafting.statdisplay.SingleStatDisplay;

public class AttributeSingleDisplay extends InteractAbleWidget implements SingleStatDisplay {
    final EntityAttribute attribute;
    final EquipmentSlot slot;
    final Identifier texture = new Identifier("textures/gui/container/inventory.png");
    ItemStack original = ItemStack.EMPTY;
    ItemStack compareTo = ItemStack.EMPTY;

    public AttributeSingleDisplay(EntityAttribute attribute, EquipmentSlot slot) {
        super(0, 0, 20, 40, Text.empty());
        this.slot = slot;
        this.attribute = attribute;
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
        double oldValue = AttributeRegistry.getAttribute(original, attribute, slot);
        double compareToValue = AttributeRegistry.getAttribute(compareTo, attribute, slot);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.setShaderTexture(0, texture);

        drawTextureWithEdge(matrices, x, y, 0, 166, 120, 32, width, height, 256, 256, 2);

        drawTextWithShadow(matrices, MinecraftClient.getInstance().textRenderer, Text.literal(oldValue + " " + compareToValue), this.x, this.y, ColorHelper.Argb.getArgb(255, 255, 255, 255));
    }
}
