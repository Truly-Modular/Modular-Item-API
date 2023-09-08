package smartin.miapi.client.gui.crafting.crafter;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.ScrollList;
import smartin.miapi.client.gui.ScrollingTextWidget;
import smartin.miapi.client.gui.rework.CraftingScreen;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.SlotProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * This Widget displays the Treelike Structure of the modular Item
 */
@Environment(EnvType.CLIENT)
public class DetailView extends InteractAbleWidget {
    final Consumer<SlotProperty.ModuleSlot> selectConsumer;
    final Map<SlotProperty.ModuleSlot, DetailView.SlotButton> buttonMap = new HashMap<>();
    SlotProperty.ModuleSlot selectedSlot;
    ScrollList scrollList;


    public DetailView(int x, int y, int width, int height, SlotProperty.ModuleSlot baseSlot, SlotProperty.ModuleSlot selected, Consumer<SlotProperty.ModuleSlot> edit, Consumer<SlotProperty.ModuleSlot> replace) {
        super(x, y, width, height, Text.empty());
        selectedSlot = selected;
        if (baseSlot != null) {
            scrollList = new ScrollList(x, y, width, height, getButtons(baseSlot, new ArrayList<>(), 0));
            this.addChild(scrollList);
        }
        this.selectConsumer = edit;
    }

    public List<InteractAbleWidget> getButtons(SlotProperty.ModuleSlot slot, List<InteractAbleWidget> buttons, int level) {
        buttons.add(new SlotButton(0, 0, 50, 18, slot, level));
        if (slot.inSlot != null) {
            SlotProperty.getSlots(slot.inSlot).forEach((id, childSlot) -> {
                getButtons(childSlot, buttons, level + 1);
            });
        }

        return buttons;
    }

    public void select(SlotProperty.ModuleSlot slot) {
        selectedSlot = slot;
        selectConsumer.accept(slot);
    }

    public void scrollTo(int y) {
        if (scrollList != null) {
            scrollList.setScrollAmount(y - this.getY());
        }
    }

    class SlotButton extends InteractAbleWidget {
        private final Identifier textureIcon;
        private final List<SlotButton> subSlots = new ArrayList<>();
        private final ScrollingTextWidget moduleName;
        private final ScrollingTextWidget materialName;
        private final SlotProperty.ModuleSlot slot;
        private final int level;
        private boolean isOpened = false;
        private boolean isSelected = false;


        public SlotButton(int x, int y, int width, int height, SlotProperty.ModuleSlot slot, int level) {
            super(x, y, width, height, Text.empty());
            this.slot = slot;
            this.level = level;
            ItemModule.ModuleInstance moduleInstance = slot.inSlot;
            if (moduleInstance == null) {
                moduleInstance = new ItemModule.ModuleInstance(ItemModule.empty);
            }
            Text materialNameText = StatResolver.translateAndResolve("[translation.[material.translation]]", moduleInstance);
            String path = StatResolver.resolveString("[material.icon]", moduleInstance);
            if (path.isEmpty()) {
                textureIcon = new Identifier(Miapi.MOD_ID, "textures/missing.png");
            } else {
                textureIcon = new Identifier(path);
            }

            Text displayText = StatResolver.translateAndResolve(Miapi.MOD_ID + ".module." + moduleInstance.module.getName(), moduleInstance);
            moduleName = new ScrollingTextWidget(this.getX() + 10, this.getY(), this.width - 20, displayText, ColorHelper.Argb.getArgb(255, 255, 255, 255));
            materialName = new ScrollingTextWidget(this.getX() + 10, this.getY(), this.width - 20, materialNameText, ColorHelper.Argb.getArgb(255, 255, 255, 255));
            materialName.setOrientation(ScrollingTextWidget.Orientation.RIGHT);
            buttonMap.put(slot, this);
        }

        public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
            super.render(drawContext, mouseX, mouseY, delta);
            int hoverOffset = 0;
            if (isMouseOver(mouseX, mouseY)) {
                hoverOffset = 1;
            }
            if (isSelected()) {
                hoverOffset = 2;
            }
            drawTextureWithEdge(drawContext, CraftingScreen.BACKGROUND_TEXTURE, getX() - 6 + level * 6, getY(), 404, 18 * hoverOffset, 108, 18, getWidth() - level * 6 + 6, 18, 512, 512, 4);
            int nameStart = getX() + 5 + level * 6;
            if (textureIcon != null) {
                nameStart += 16;
                drawContext.drawTexture(textureIcon, this.getX() + 4 + level * 6, this.getY()+1, 0, 0, 16, 16, 16, 16);
            }

            moduleName.setX(nameStart);
            moduleName.setY(this.getY() + 5);
            moduleName.setWidth(this.getWidth() + getX() - nameStart - 4);
            moduleName.render(drawContext, mouseX, mouseY, delta);
        }

        public boolean isSelected() {
            if (this.slot == selectedSlot) {
                return true;
            }
            if(this.slot != null && slot.equals(selectedSlot)){
                return true;
            }
            return false;
        }

        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0) {
                if (isMouseOver(mouseX, mouseY)) {
                    select(slot);
                }
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }
}
