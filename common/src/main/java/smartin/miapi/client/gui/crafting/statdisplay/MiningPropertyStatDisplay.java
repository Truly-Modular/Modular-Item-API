package smartin.miapi.client.gui.crafting.statdisplay;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.client.gui.ParentHandledScreen;
import smartin.miapi.modules.properties.mining.MiningLevelProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MiningPropertyStatDisplay extends SingleStatDisplayDouble {
    public final String type;

    public MiningPropertyStatDisplay(String type) {
        super(0, 0, 51, 19, (stack -> Component.translatable("miapi.stat.miapi.mining.level." + type)), (s) -> Component.empty());
        this.type = type;
    }

    @Override
    public double getValue(ItemStack stack) {
        MiningLevelProperty.MiningRule rule = MiningLevelProperty.property.getData(stack).orElse(new HashMap<>()).get(type);
        if (rule != null) {
            return rule.speed().getValue();
        }
        return 0.0;
    }

    public boolean shouldRender(ItemStack original, ItemStack compareTo) {
        super.shouldRender(original,compareTo);
        return MiningLevelProperty.property.getData(original).orElse(new HashMap<>()).containsKey(type) &&
               MiningLevelProperty.property.getData(original).orElse(new HashMap<>()).get(type).speed().getValue() != 1 ||
               MiningLevelProperty.property.getData(compareTo).orElse(new HashMap<>()).containsKey(type) &&
               MiningLevelProperty.property.getData(compareTo).orElse(new HashMap<>()).get(type).speed().getValue() != 1;
    }

    @Override
    public void renderHover(GuiGraphics drawContext, int mouseX, int mouseY, float delta) {
        List<Component> list = new ArrayList(getHoverLines(drawContext, mouseX, mouseY, delta));
        if (this.isMouseOver(mouseX, mouseY)) {
            if (ParentHandledScreen.hasShiftDown()) {
                var rule = MiningLevelProperty.property.getData(compareTo == null ? original : compareTo).orElse(new HashMap<>()).get(type);
                if (rule != null) {
                    rule.speed().operations.forEach(operation1 -> {
                        if (operation1.solve() != 0) {
                            list.add(Component.literal(SinglePropertyStatDisplay.stringForOperation(operation1)).withStyle(ChatFormatting.GRAY));
                            if (ParentHandledScreen.hasAltDown()) {
                                list.add(Component.literal("  " + operation1.value).withStyle(ChatFormatting.DARK_GRAY));
                            }
                        }
                    });
                }
                list.add(Component.translatable("miapi.ui.stat_detail.shift_alt").withStyle(ChatFormatting.DARK_GRAY));
            } else {
                list.add(Component.translatable("miapi.ui.stat_detail.shift").withStyle(ChatFormatting.DARK_GRAY));
            }
        }
        drawContext.renderComponentTooltip(
                Minecraft.getInstance().font,
                list, mouseX, mouseY);
    }
}
