package smartin.miapi.client.gui.crafting.crafter;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.ScrollList;
import smartin.miapi.client.gui.ScrollingTextWidget;
import smartin.miapi.client.gui.crafting.CraftingScreen;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.material.Material;
import smartin.miapi.modules.material.MaterialProperty;
import smartin.miapi.modules.properties.slot.SlotProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * This Widget displays the Treelike Structure of the modular Item
 *
 */
@Environment(EnvType.CLIENT)
public class DetailView extends InteractAbleWidget {
    final Consumer<SlotProperty.ModuleSlot> selectConsumer;
    final Map<SlotProperty.ModuleSlot, SlotButton> buttonMap = new HashMap<>();
    SlotProperty.ModuleSlot selectedSlot;
    ScrollList scrollList;
    public static int scrollPos = 0;


    public DetailView(int x, int y, int width, int height, SlotProperty.ModuleSlot baseSlot, SlotProperty.ModuleSlot selected, Consumer<SlotProperty.ModuleSlot> edit, Consumer<SlotProperty.ModuleSlot> replace, String slotType) {
        super(x, y, width, height, Component.empty());
        selectedSlot = selected;
        if (baseSlot != null) {
            scrollList = new ScrollList(x, y, width, height, getButtons(baseSlot, new ArrayList<>(), 0, slotType));
            this.addChild(scrollList);
            scrollList.setScrollAmount(scrollPos);
        }
        this.selectConsumer = edit;
    }

    public List<InteractAbleWidget> getButtons(SlotProperty.ModuleSlot slot, List<InteractAbleWidget> buttons, int level, String slotType) {
        int nextLevel;
        if (slot.slotType.equals(slotType)) {
            buttons.add(new SlotButton(0, 0, 50, 22, slot, level));
            nextLevel = level + 1;
        } else {
            nextLevel = level;
        }
        if (slot.inSlot != null) {
            SlotProperty.getSlots(slot.inSlot).forEach((id, childSlot) -> {
                if (childSlot.slotType.equals(slotType)) {
                    getButtons(childSlot, buttons, nextLevel, slotType);
                }
            });
        }

        return buttons;
    }

    public void select(SlotProperty.ModuleSlot slot) {
        scrollPos = scrollList.getScrollAmount();
        selectedSlot = slot;
        selectConsumer.accept(slot);
    }

    public void scrollTo(int y) {
        if (scrollList != null) {
            scrollList.setScrollAmount(y - this.getY());
        }
    }

    class SlotButton extends InteractAbleWidget {
        private final List<SlotButton> subSlots = new ArrayList<>();
        private final ScrollingTextWidget moduleName;
        private final ScrollingTextWidget materialName;
        private final SlotProperty.ModuleSlot slot;
        private final Material material;
        private final int level;
        private final boolean isOpened = false;
        private final boolean isSelected = false;


        public SlotButton(int x, int y, int width, int height, SlotProperty.ModuleSlot slot, int level) {
            super(x, y, width, height, Component.empty());
            this.slot = slot;
            this.level = level;
            ModuleInstance moduleInstance = slot.inSlot;
            boolean hasNoModule = moduleInstance == null;
            if (hasNoModule) {
                moduleInstance = new ModuleInstance(ItemModule.empty);
            }
            Component materialNameText = moduleInstance.getModuleName();
            material = MaterialProperty.getMaterial(moduleInstance);

            Component displayText = moduleInstance.getModuleName();
            if (hasNoModule && slot.translationKey != null) {
                displayText = Component.translatable(slot.translationKey);
            }
            moduleName = new ScrollingTextWidget(this.getX() + 10, this.getY(), this.width - 20, displayText, FastColor.ARGB32.color(255, 255, 255, 255));
            materialName = new ScrollingTextWidget(this.getX() + 10, this.getY(), this.width - 20, materialNameText, FastColor.ARGB32.color(255, 255, 255, 255));
            materialName.setOrientation(ScrollingTextWidget.Orientation.RIGHT);
            buttonMap.put(slot, this);
        }

        public void renderWidget(GuiGraphics drawContext, int mouseX, int mouseY, float delta) {
            super.renderWidget(drawContext, mouseX, mouseY, delta);
            int hoverOffset = 0;
            if (isMouseOver(mouseX, mouseY)) {
                hoverOffset = 1;
            }
            if (isHoveredOrFocused()) {
                hoverOffset = 2;
            }
            drawTextureWithEdge(drawContext, CraftingScreen.BACKGROUND_TEXTURE, getX() + (level - 1) * 2, getY(), 404, 18 * hoverOffset, 108, 18, getWidth() - (level - 1) * 2, getHeight(), 512, 512, 4);
            int nameStart = getX() + 5 + (level - 1) * 2;
            if (material != null && material.hasIcon()) {
                nameStart += 1 + material.renderIcon(drawContext, this.getX() + 5 + (level - 1) * 2, this.getY() + 3);
            }

            moduleName.setX(nameStart);
            moduleName.setY(this.getY() + 7);
            moduleName.setWidth(this.getWidth() + getX() - nameStart - 4);
            moduleName.render(drawContext, mouseX, mouseY, delta);
        }

        public boolean isHoveredOrFocused() {
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
                    playClickedSound();
                    select(slot);
                }
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }
}
