package smartin.miapi.client.gui.crafting.crafter;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.*;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.SlotProperty;
import smartin.miapi.item.modular.StatResolver;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class DetailView extends InteractAbleWidget {
    final Consumer<SlotProperty.ModuleSlot> edit;
    final Consumer<SlotProperty.ModuleSlot> replace;
    final Map<SlotProperty.ModuleSlot, DetailView.SlotButton> buttonMap = new HashMap<>();
    SlotProperty.ModuleSlot selectedSlot;
    ScrollList scrollList;


    public DetailView(int x, int y, int width, int height, SlotProperty.ModuleSlot baseSlot, SlotProperty.ModuleSlot selected, Consumer<SlotProperty.ModuleSlot> edit, Consumer<SlotProperty.ModuleSlot> replace) {
        super(x, y, width, height, Text.empty());
        selectedSlot = selected;
        if (baseSlot != null) {
            scrollList = new ScrollList(x, y, width, height, Collections.singletonList(new SlotButton(this.x, this.y, this.width, 16, baseSlot)));
            this.addChild(scrollList);
        }
        this.edit = edit;
        this.replace = replace;
    }

    public void scrollTo(int y) {
        //TODO:fix this scrolling
        //Miapi.LOGGER.warn("trying scrolling to" + (y - this.y));
        if (scrollList != null) {
            scrollList.setScrollAmount(y - this.y);
        }
    }

    class SlotButton extends InteractAbleWidget {
        private final Identifier texture = new Identifier(Miapi.MOD_ID, "textures/gui/crafter/module_button.png");
        private final Identifier editTexture = new Identifier(Miapi.MOD_ID, "textures/gui/crafter/edit_button.png");
        private final Identifier plusTexture = new Identifier(Miapi.MOD_ID, "textures/gui/crafter/plus_minus_button.png");
        private final Identifier textureIcon;
        private final List<SlotButton> subSlots = new ArrayList<>();
        private final SlotButton parent;
        private final ModuleDetail detail;
        private final ScrollingTextWidget moduleName;
        private final ScrollingTextWidget materialName;
        private final SlotProperty.ModuleSlot slot;
        private final int level;
        private final int trueHeight;
        private int currentHeight;
        private boolean isOpened = false;
        private boolean isSelected = false;


        public SlotButton(int x, int y, int width, int height, SlotProperty.ModuleSlot slot) {
            this(x, y, width, height, slot, 0, null);
        }

        public SlotButton(int x, int y, int width, int height, SlotProperty.ModuleSlot slot, int level, SlotButton parent) {
            super(x, y, width, height, Text.empty());
            trueHeight = height;
            currentHeight = height;
            this.parent = parent;
            this.slot = slot;
            this.level = level;
            if (slot.inSlot != null) {
                AtomicInteger yOffset = new AtomicInteger();
                SlotProperty.getSlots(slot.inSlot).forEach((id, childSlot) -> {
                    yOffset.addAndGet(this.height);
                    subSlots.add(new SlotButton(this.x, this.y + yOffset.get(), this.width, this.height, childSlot, level + 1, this));
                });
            }
            ItemModule.ModuleInstance moduleInstance = slot.inSlot;
            if (moduleInstance == null) {
                moduleInstance = new ItemModule.ModuleInstance(ItemModule.empty);
            }
            Text materialNameText = StatResolver.translateAndResolve("[translation.[material.translation]]", moduleInstance);
            String path = StatResolver.resolveString("[material.icon]", moduleInstance);
            if (path.isEmpty()) {
                textureIcon = new Identifier("minecraft", "missing");
            } else {
                Miapi.LOGGER.warn(path);
                //textureIcon = new Identifier("minecraft", "missing");
                textureIcon = new Identifier(path);
            }

            Text displayText = StatResolver.translateAndResolve(Miapi.MOD_ID + ".module." + moduleInstance.module.getName(), moduleInstance);
            moduleName = new ScrollingTextWidget(this.x + 10, this.y, this.width - 20, displayText, ColorHelper.Argb.getArgb(255, 255, 255, 255));
            materialName = new ScrollingTextWidget(this.x + 10, this.y, this.width - 20, materialNameText, ColorHelper.Argb.getArgb(255, 255, 255, 255));
            materialName.setOrientation(ScrollingTextWidget.ORIENTATION.RIGHT);
            detail = new ModuleDetail(x, y, width, slot);
            open();
            buttonMap.put(slot, this);
            if (this.slot.equals(selectedSlot)) {
                select();
            }
        }

        public void open() {
            if (this.isOpened) {
                this.close();
            }
            if (this.parent != null) {
                parent.open();
            }
            updateChildren();
            this.children.addAll(subSlots);
            this.isOpened = true;
            scrollTo(this.y);
        }

        public void updateChildren() {
            AtomicInteger yOffset = new AtomicInteger();
            yOffset.addAndGet(trueHeight);
            if (isSelected) {
                detail.x = this.x + 1;
                detail.y = this.y + trueHeight - 2;
                detail.setWidth(this.width - 2);
                yOffset.addAndGet(detail.getHeight());
            }
            subSlots.forEach(slotButton -> {
                slotButton.x = this.x;
                slotButton.y = this.y + yOffset.get();
                yOffset.addAndGet(slotButton.currentHeight);
                slotButton.width = this.width;
            });
        }

        public void close() {
            if (!this.isOpened) {
                this.open();
            }
            this.height = trueHeight;
            this.children().clear();
            subSlots.forEach(slotButton -> {
                slotButton.x = this.x;
                slotButton.y = this.y;
            });
            this.isOpened = false;
        }

        public void select() {
            isSelected = true;
            scrollTo(this.y);
        }

        public void deselect() {
            isSelected = false;
        }

        public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            currentHeight = trueHeight;
            if (this.isOpened) {
                updateChildren();
                for (SlotButton detail1 : subSlots) {
                    currentHeight += detail1.currentHeight;
                    detail1.render(matrices, mouseX, mouseY, delta);
                }
            }
            if (this.isSelected) {
                detail.render(matrices, mouseX, mouseY, delta);
                currentHeight += detail.getHeight();
            }
            drawButton(matrices, mouseX, mouseY, delta);
        }

        public void drawButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            AtomicInteger height = new AtomicInteger(trueHeight);
            if (isOpened) {
                this.subSlots.forEach(child -> {
                    height.addAndGet(child.height);
                });
            }
            if (isSelected) {
                height.addAndGet(detail.getHeight());
            }
            this.height = height.get();

            materialName.y = this.y + 4;
            materialName.x = this.x + 6 * level + 3;
            materialName.setWidth(this.width - 50 - level * 6);

            //setup TextWidget
            moduleName.y = this.y + 4;
            moduleName.x = this.x + 6 * level + 3;
            int materialNameWidth = materialName.getRequiredWidth();
            moduleName.setWidth(this.width - 50 - materialNameWidth - level * 6);

            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, texture);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            int textureOffset = 0;
            if (isOpened) {
                textureOffset = 1;
            }
            int hoveredOffset = 0;
            if (mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + trueHeight) {
                hoveredOffset = 1;
            }

            //render button left side
            drawTexture(matrices, -6 + 6 * level + x, y, 0, 0, hoveredOffset * 14, 140, 14, 140, 28);
            //render button right side
            drawTexture(matrices, x + this.width - 60, y, 0, 140 - 60, hoveredOffset * 14, 60, 14, 140, 28);
            RenderSystem.setShaderTexture(0, plusTexture);
            hoveredOffset = 0;
            if (mouseX > x + width - 14 && mouseX < x + width && mouseY > y && mouseY < y + trueHeight) {
                hoveredOffset = 1;
            }
            //render edit button
            drawTexture(matrices, x + width - 14, y, 0, hoveredOffset * 14, textureOffset * 14, 14, 14, 28, 28);
            RenderSystem.setShaderTexture(0, editTexture);
            hoveredOffset = 0;
            if (mouseX > x + width - 28 && mouseX < x + width - 14 && mouseY > y && mouseY < y + trueHeight) {
                hoveredOffset = 1;
            }
            //edit plus/minus button
            drawTexture(matrices, x + width - 28, y, 0, 0, hoveredOffset * 14, 14, 14, 14, 28);

            moduleName.render(matrices, mouseX, mouseY, delta);
            materialName.render(matrices, mouseX, mouseY, delta);

            RenderSystem.setShaderTexture(0, textureIcon);
            drawTexture(matrices, this.x + this.width - 44, this.y - 1, 0, 0, 16, 16, 16, 16);


        }

        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0) {
                //TODO:MAKe sure this is working corretly
                if (mouseY < this.y + trueHeight && mouseY > this.y) {
                    if (mouseX > this.x + this.width - 14) {
                        if (isOpened) {
                            this.close();
                            return true;
                        } else {
                            this.open();
                            return true;
                        }
                    } else if (mouseX > x + width - 28 && mouseX < x + width - 14) {
                        if (isSelected) {
                            deselect();
                            return true;
                        } else {
                            select();
                            return true;
                        }
                    }
                } else if (mouseY < this.y + currentHeight && mouseY > this.y + trueHeight) {
                    if (isSelected) {
                        if (detail.mouseClicked(mouseX, mouseY, button)) {
                            return true;
                        }
                    }
                }
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }

    class ModuleDetail extends InteractAbleWidget {
        private final Identifier texture = new Identifier(Miapi.MOD_ID, "textures/gui/crafter/detail_background.png");
        private final MultiLineTextWidget description;
        private final List<ScrollingTextWidget> lines = new ArrayList<>();
        private final ScrollingTextWidget headerText;
        private final static int lineSpacing = 2;
        private final static int buttonSpacing = 3;
        private final static int minSize = 30;
        private final static int HeaderSize = 11;
        private SimpleButton editButton;
        private SimpleButton replaceButton;
        private int textColor = ColorHelper.Argb.getArgb(255, 255, 255, 255);

        public ModuleDetail(int x, int y, int width, SlotProperty.ModuleSlot slot) {
            super(x, y, width, 10, Text.empty());
            int fontSize = MinecraftClient.getInstance().textRenderer.fontHeight;

            ItemModule.ModuleInstance moduleInstance = slot.inSlot;
            if (moduleInstance == null) {
                moduleInstance = new ItemModule.ModuleInstance(ItemModule.empty);
            }

            Text header = StatResolver.translateAndResolve(Miapi.MOD_ID + ".module." + moduleInstance.module.getName(), moduleInstance);
            headerText = new ScrollingTextWidget(this.x, this.y, this.width, header, textColor);

            Text displayText = StatResolver.translateAndResolve(Miapi.MOD_ID + ".module." + moduleInstance.module.getName() + ".description", moduleInstance);
            description = new MultiLineTextWidget(x, y, width - 4, height, displayText);
            description.maxLineLength = this.width - 4;
            description.spacer = lineSpacing;
            description.setText(displayText);
            String[] rawLines = displayText.getString().split("\n");
            for (String line : rawLines) {
                lines.add(new ScrollingTextWidget(this.x, this.y, this.width, Text.of(line), textColor));
            }
            editButton = new SimpleButton<>(this.x, this.y, 30, 11, Text.translatable(Miapi.MOD_ID + ".ui.edit"), slot, (editSlot) -> {
                edit.accept(editSlot);
            });
            replaceButton = new SimpleButton<>(this.x, this.y, 60, 11, Text.translatable(Miapi.MOD_ID + ".ui.replace"), slot, (replaceSlot) -> {
                replace.accept(replaceSlot);
            });
            this.height = Math.max(minSize, lines.size() * (fontSize + lineSpacing) + buttonSpacing * 2 + Math.max(replaceButton.getHeight(), editButton.getHeight()) + buttonSpacing * 2 + HeaderSize);
            this.height = Math.max(minSize, description.getHeight() + buttonSpacing * 2 + Math.max(replaceButton.getHeight(), editButton.getHeight()) + buttonSpacing * 2 + HeaderSize);
            addChild(editButton);
            addChild(replaceButton);
        }

        public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            editButton.x = this.x + buttonSpacing;
            editButton.y = this.y + this.height - editButton.getHeight() - buttonSpacing;
            replaceButton.x = this.x + this.width - replaceButton.getWidth() - buttonSpacing;
            replaceButton.y = this.y + this.height - replaceButton.getHeight() - buttonSpacing;
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, texture);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            //drawTexture(matrices, x, y, 0, 0, 0, width, height - 5, 156, 100);
            //drawTexture(matrices, x, y + height - 5, 0, 0, 95, width, 5, 156, 100);
            drawTextureWithEdge(matrices, x, y, this.width, height, 156, 100, 5);

            headerText.x = this.x + buttonSpacing;
            headerText.y = this.y + buttonSpacing;
            headerText.setWidth(this.width - 2 * buttonSpacing);

            headerText.render(matrices, mouseX, mouseY, delta);

            int y = 4 + this.y + lineSpacing * 2 + HeaderSize; // the starting y position for the first line
            description.x = this.x + 2;
            description.y = y;
            for (ScrollingTextWidget line : lines) {
                line.x = this.x + 2;
                line.setWidth(this.width - 4);
                line.y = y;
                //line.render(matrices, mouseX, mouseY, delta);
            }
            description.render(matrices, mouseX, mouseY, delta);
            super.render(matrices, mouseX, mouseY, delta);
        }
    }
}
