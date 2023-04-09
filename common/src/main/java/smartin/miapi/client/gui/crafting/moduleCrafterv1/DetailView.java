package smartin.miapi.client.gui.crafting.moduleCrafterv1;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.ScrollingTextWidget;
import smartin.miapi.item.modular.properties.SlotProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class DetailView extends InteractAbleWidget {


    public DetailView(int x, int y, int width, int height, SlotProperty.ModuleSlot instance, Consumer<SlotProperty.ModuleSlot> select, Consumer<SlotProperty.ModuleSlot> edit, Consumer<SlotProperty.ModuleSlot> replace) {
        super(x, y, width, height, Text.empty());
        this.addChild(new SlotButton(this.x, this.y, this.width, 16, instance));
    }

    class SlotButton extends InteractAbleWidget {
        private final SlotProperty.ModuleSlot slot;
        private final int level;
        private boolean isOpened = false;
        private List<SlotButton> subSlots = new ArrayList<>();
        private int trueHeight;
        private ScrollingTextWidget textWidget;
        private SlotButton parent;
        //private static final Identifier ButtonTexture = new Identifier(Miapi.MOD_ID, "textures/button.png");
        private final Identifier texture = new Identifier(Miapi.MOD_ID,"textures/button2.png");

        public SlotButton(int x, int y, int width, int height, SlotProperty.ModuleSlot slot) {
            this(x, y, width, height, slot, 0 , null);
        }

        public SlotButton(int x, int y, int width, int height, SlotProperty.ModuleSlot slot, int level, SlotButton parent) {
            super(x, y, width, height, Text.empty());
            trueHeight = height;
            this.parent = parent;
            this.slot = slot;
            this.level = level;
            if (this.slot.inSlot != null) {
                AtomicInteger yOffset = new AtomicInteger();
                SlotProperty.getSlots(this.slot.inSlot).forEach((id, childSlot) -> {
                    yOffset.addAndGet(this.height);
                    subSlots.add(new SlotButton(this.x, this.y + yOffset.get(), this.width, this.height, childSlot, level + 1, this));
                });
            }
            Text displayText = Text.translatable(Miapi.MOD_ID+".module.empty");
            if (this.slot.inSlot != null) {
                displayText = Text.translatable(Miapi.MOD_ID + ".module." + this.slot.inSlot.module.getName());
            }
            textWidget = new ScrollingTextWidget(this.x + 10, this.y, this.width - 20, displayText, ColorHelper.Argb.getArgb(255, 255, 255, 255));
            open();
        }

        public void open() {
            if(this.isOpened){
                this.close();
            }
            if(this.parent!=null){
                parent.open();
            }
            AtomicInteger yOffset = new AtomicInteger();
            subSlots.forEach(slotButton -> {
                yOffset.addAndGet(this.height);
                slotButton.x = this.x;
                slotButton.y = this.y + yOffset.get();
            });
            this.height += subSlots.size() * trueHeight;
            this.children.addAll(subSlots);
            this.isOpened = true;
        }

        public void close() {
            if(!this.isOpened) {
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

        public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            drawButton(matrices, mouseX, mouseY, delta);
            if (this.isOpened) {
                subSlots.forEach(slotButton -> {
                    slotButton.render(matrices, mouseX, mouseY, delta);
                });
            }
        }

        public void drawButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            AtomicInteger height = new AtomicInteger(trueHeight);
            if (isOpened) {
                this.subSlots.forEach(child -> {
                    height.addAndGet(child.height);
                });
            }
            this.height = height.get();
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();

            int color = ColorHelper.Argb.getArgb(255, 50, 50, 50);

            if (this.isMouseOver(mouseX, mouseY) && mouseY<this.y+trueHeight) {
                color = ColorHelper.Argb.getArgb(255, 100, 100, 100);
            }
            textWidget.y = this.y+4;
            textWidget.x = this.x+6*level+3;
            textWidget.setWidth(this.width-28-level*6);

            enableScissor(this.x,this.y,this.x+this.width,this.y+trueHeight);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0,texture);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();
            int textureOffset = 0;
            if(isOpened){
                textureOffset = 14;
            }
            drawTexture(matrices, -6+6*level+x, y, 0, 0, textureOffset, 140, 14, 140, 28);
            drawTexture(matrices, x+this.width-60, y, 0, 140-60, textureOffset, 60, 14, 140, 28);
            disableScissor();
            textWidget.render(matrices, mouseX, mouseY, delta);
            //drawSquareBorder(matrices, this.x, this.y, this.width, this.trueHeight-2, 1, color);

        }

        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (mouseY < this.y + trueHeight && mouseY > this.y) {
                if (mouseX > this.x + this.width - 10) {
                    if (isOpened) {
                        this.close();
                    } else {
                        this.open();
                    }
                }
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }
}
