package smartin.miapi.client.gui.crafting.crafter;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.ScrollList;
import smartin.miapi.client.gui.ScrollingTextWidget;
import smartin.miapi.client.gui.SimpleButton;
import smartin.miapi.item.modular.ItemModule;
import smartin.miapi.item.modular.properties.SlotProperty;
import smartin.miapi.item.modular.properties.crafting.AllowedSlots;

import java.util.ArrayList;
import java.util.function.Consumer;

public class ReplaceView extends InteractAbleWidget {
    Consumer<ItemModule> craft;

    public ReplaceView(int x, int y, int width, int height, SlotProperty.ModuleSlot slot, Consumer<SlotProperty.ModuleSlot> back, Consumer<ItemModule> craft) {
        super(x, y, width, height, Text.empty());
        this.craft = craft;
        ScrollList list = new ScrollList(x, y, width, height, new ArrayList<>());
        addChild(list);
        list.children().clear();
        addChild(new SimpleButton<>(this.x + 2, this.y + this.height - 10, 40, 10, Text.literal("Back"), slot, back::accept));
        ArrayList<InteractAbleWidget> toList = new ArrayList<>();
        toList.add(new SlotButton(0, 0, this.width, 15, null));
        AllowedSlots.allowedIn(slot).forEach(module -> {
            toList.add(new SlotButton(0, 0, this.width, 15, module));
        });
        list.setList(toList);
    }

    class SlotButton extends InteractAbleWidget {
        private final Identifier texture = new Identifier(Miapi.MOD_ID, "textures/gui/crafter/module_button_select.png");
        private ScrollingTextWidget textWidget;
        private ItemModule module;


        public SlotButton(int x, int y, int width, int height, ItemModule module) {
            super(x, y, width, height, Text.empty());
            String moduleName = "emtpy";
            if (module != null) {
                moduleName = module.getName();
            }
            textWidget = new ScrollingTextWidget(0, 0, this.width, Text.translatable(Miapi.MOD_ID + ".module.name." + moduleName), ColorHelper.Argb.getArgb(255, 255, 255, 255));
            this.module = module;
        }

        public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, texture);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            int hoverOffset = 0;
            if (isMouseOver(mouseX, mouseY)) {
                hoverOffset = 14;
            }
            drawTextureWithEdge(matrices, this.x, this.y, 0, hoverOffset, 140, 14, this.width, this.height, 140, 28, 2);
            textWidget.x = this.x + 2;
            textWidget.y = this.y + 3;
            textWidget.setWidth(this.width - 4);
            textWidget.render(matrices, mouseX, mouseY, delta);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if(isMouseOver(mouseX,mouseY)){
                if (button == 0) {
                    craft.accept(module);
                    return true;
                }
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }
}
