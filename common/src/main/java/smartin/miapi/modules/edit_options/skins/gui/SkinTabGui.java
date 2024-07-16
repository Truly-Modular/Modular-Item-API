package smartin.miapi.modules.edit_options.skins.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.ScrollingTextWidget;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.modules.edit_options.skins.Skin;
import smartin.miapi.modules.edit_options.skins.SkinOptions;
import smartin.miapi.modules.edit_options.skins.SkinTab;

import java.util.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;

class SkinTabGui extends InteractAbleWidget implements SkinGui.SortAble {
    ResourceLocation arrowTexture = new ResourceLocation(Miapi.MOD_ID, "textures/gui/skin/arrow.png");
    boolean isOpen = true;
    List<SkinGui.SortAble> fullList = new ArrayList<>();
    List<SkinGui.SortAble> currentList;
    ScrollingTextWidget textWidget;
    String sortAble;
    int spacing = 10;
    int realHeight = 14;
    public SkinTabGui parent;
    static final int SIZE_Y = 14;
    final boolean isRoot;
    final SkinTab tabInfo;

    public SkinTabGui(SkinGui skinGui, int x, int y, int width, String currentTab, Map<String, Skin> mapsToDo) {
        super(x, y, width, SIZE_Y, Component.empty());
        this.tabInfo = SkinOptions.getTag(currentTab);
        height = realHeight;
        isRoot = currentTab.isBlank();
        if (isRoot) {
            this.height = 0;
            this.realHeight = 0;
            spacing = 0;
            SkinButton child = new SkinButton(skinGui, x + spacing, y, width - spacing, "", new Skin());
            fullList.add(child);
        }

        String[] parts = currentTab.split("/");
        int partLength = parts.length;
        if (isRoot) partLength = 0;
        Component skinName = StatResolver.translateAndResolve(Miapi.MOD_ID + ".skin.name." + parts[parts.length - 1], skinGui.instance);
        sortAble = skinName.getString();
        textWidget = new ScrollingTextWidget(x + 12, y + 1, width - 13, skinName, FastColor.ARGB32.color(255, 255, 255, 255));

        Map<String, Map<String, Skin>> toDoMap = new HashMap<>();
        for (Map.Entry<String, Skin> entry : mapsToDo.entrySet()) {
            String skinPath = entry.getKey();
            String[] pathParts = skinPath.split("/");

            // Check if the current path is under the current tab
            if (skinPath.startsWith(currentTab + "/") || isRoot) {

                // Check if there is a slash after the current part
                if (partLength + 1 == pathParts.length) {
                    // Add the skin to the direct skins map
                    fullList.add(new SkinButton(skinGui, x + spacing, y, width - spacing, skinPath, entry.getValue()));
                } else {
                    String nextPart = pathParts[partLength];
                    String nextTab = currentTab + "/" + nextPart;
                    if (isRoot) nextTab = nextPart;
                    // Create or retrieve the sub-element map for the next tab
                    Map<String, Skin> subMap = toDoMap.computeIfAbsent(nextTab, k -> new HashMap<>());

                    // Add the skin to the sub-element map
                    subMap.put(nextTab, entry.getValue());
                }
            }
        }
        toDoMap.forEach((tab, map) -> {
            SkinTabGui child = new SkinTabGui(skinGui, x + spacing, y, width - spacing, tab, mapsToDo);
            child.parent = this;
            fullList.add(child);
        });
        currentList = new ArrayList<>(fullList);
        setChildren(true);
        if (!this.isRoot) {
            toggleOpen();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseY < this.getY() + realHeight) {
            toggleOpen();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public void toggleOpen() {
        if (isOpen) {
            //close
            currentList.clear();
            height = realHeight;
            setChildren(false);
        } else {
            currentList = new ArrayList<>(fullList);
            setChildren(false);
        }
        isOpen = !isOpen;
    }

    private boolean isMouseOverReal(double mouseX, double mouseY) {
        return mouseY > this.getY() && mouseY < this.getY() + realHeight && (mouseX > this.getX() && mouseX < this.getX() + width);
    }

    public void setY(int y) {
        super.setY(y);
        setChildren(true);
    }

    @Override
    public void render(GuiGraphics drawContext, int mouseX, int mouseY, float delta) {
        if (!isRoot) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();
            RenderSystem.setShaderTexture(0, arrowTexture);
            int offset = isOpen ? 10 : 0;
            int hover = isOpen ? tabInfo.header.ySize() * 2 : 0;
            hover = this.isMouseOverReal(mouseX, mouseY) ? tabInfo.header.ySize() : hover;
            drawTextureWithEdge(drawContext, arrowTexture, getX(), getY() + 2, offset, 0, 10, 10, 10, 10, 20, 10, 3);
            //Header
            RenderSystem.setShaderTexture(0, tabInfo.header.texture());
            drawTextureWithEdgeAndScale(drawContext, tabInfo.header.texture(), getX(), getY(), 0, hover, tabInfo.header.xSize(), tabInfo.header.ySize(), this.width, realHeight, tabInfo.header.xSize(), tabInfo.header.ySize() * 3, tabInfo.header.borderSize(), tabInfo.header.scale());

            if (isOpen) {
                //Background
                if (tabInfo.background.keepScale()) {

                    drawContext.drawRepeatingTexture(tabInfo.background.texture(), getX(), getY() + realHeight, this.width, height - realHeight, 0, 0, tabInfo.background.xSize(), tabInfo.background.ySize());
                } else {
                    RenderSystem.setShaderTexture(0, tabInfo.background.texture());
                    drawTextureWithEdgeAndScale(drawContext, tabInfo.background.texture(), getX(), getY() + realHeight, 0, 0, tabInfo.background.xSize(), tabInfo.background.ySize(), this.width, height - realHeight, tabInfo.background.xSize(), tabInfo.background.ySize(), tabInfo.background.borderSize(), tabInfo.background.scale());
                }
            }
            textWidget.setY(this.getY() + 2);
            textWidget.render(drawContext, mouseX, mouseY, delta);
        }
        super.render(drawContext, mouseX, mouseY, delta);
    }

    private void setChildren(boolean updateChildren) {
        if (parent == null) {
            updateChildren = true;
        }
        this.children().clear();
        int yHeight = getY() + realHeight;
        for (SkinGui.SortAble sortableElement : currentList) {
            if (sortableElement instanceof InteractAbleWidget widget) {
                if (sortableElement.isActive()) {
                    widget.setY(yHeight);
                    yHeight += widget.getHeight();
                    this.addChild(widget);
                    if (widget instanceof SkinTabGui tab) {
                        if (updateChildren) {
                            tab.setChildren(true);
                        }
                    }
                }
            }
        }
        this.height = yHeight - getY();
        if (!updateChildren) {
            parent.setChildren(false);
        }
    }


    @Override
    public void filter(String search) {
        for (SkinGui.SortAble element : fullList) {
            element.filter(search);
        }
        setChildren(true);
    }

    @Override
    public String sortAndGetTop() {
        fullList.sort(Comparator.comparing(SkinGui.SortAble::sortAndGetTop));
        currentList = new ArrayList<>(fullList);
        setChildren(true);
        return "." + fullList.get(0).sortAndGetTop();
    }

    @Override
    public boolean isActive() {
        boolean isEnabled = false;
        for (SkinGui.SortAble element : fullList) {
            if (element.isActive()) {
                isEnabled = true;
            }
        }
        return isEnabled;
    }
}
