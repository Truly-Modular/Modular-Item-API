package smartin.miapi.client.gui.crafting.crafter.replace.hover;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.crafting.CraftingScreen;
import smartin.miapi.material.MaterialStatIndicatorProperty;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.LoreProperty;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MaterialStatPreview extends InteractAbleWidget {
    boolean isVisiible;
    Map<String, MaterialStatIndicatorProperty.Context> map;
    List<Component> list = new ArrayList<>(List.of(Component.translatable("miapi.material_indication.header")));
    List<Component> fullList = new ArrayList<>(List.of(Component.translatable("miapi.material_indication.header")));

    public MaterialStatPreview(int x, int y, int width, int height, ItemModule context) {
        super(x, y, width, height, Component.empty());
        var optionalData = MaterialStatIndicatorProperty.property.getData(context);
        isVisiible = optionalData.isPresent();
        if (isVisiible) {
            map = MaterialStatIndicatorProperty.property.initialize(optionalData.get());
            map.forEach((stat, context1) -> {
                int value = context1.strength;
                String valueString = (value > 0) ? "+".repeat(value) : "-".repeat(-value);
                Component component = Component.literal(valueString);
                if (value > 0) {
                    component = LoreProperty.format(component, ChatFormatting.GREEN);
                } else {
                    component = LoreProperty.format(component, ChatFormatting.RED);
                }
                list.add(Component.translatable("miapi.material_stat." + stat).append(component));
                fullList.add(Component.translatable("miapi.material_stat." + stat).append(component));
                if (context1.info != null) {
                    fullList.add(context1.info);
                }
            });
        }
    }

    public void renderWidget(GuiGraphics drawContext, int mouseX, int mouseY, float delta) {
        if (isVisiible) {
            drawContext.drawString(Minecraft.getInstance().font, "i", getX() + 1, getY() + 1, Color.BLACK.getRGB(), false);
            if (isMouseOver(mouseX, mouseY)) {
                if (CraftingScreen.hasShiftDown() || CraftingScreen.hasAltDown()) {
                    drawContext.renderTooltip(Minecraft.getInstance().font, fullList, Optional.empty(), mouseX, mouseY);
                } else {
                    drawContext.renderTooltip(Minecraft.getInstance().font, list, Optional.empty(), mouseX, mouseY);
                }
            }
        }
    }
}
