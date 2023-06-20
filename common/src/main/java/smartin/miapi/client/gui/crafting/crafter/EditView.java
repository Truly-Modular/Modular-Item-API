package smartin.miapi.client.gui.crafting.crafter;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Element;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.*;
import smartin.miapi.client.gui.crafting.CraftingScreenHandler;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.edit_options.EditOption;
import smartin.miapi.modules.properties.AllowedSlots;
import smartin.miapi.modules.properties.SlotProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class EditView extends InteractAbleWidget {
    ItemStack stack;
    ItemModule.ModuleInstance instance;
    Consumer<ItemStack> preview;
    List<Element> defaultChildren = new ArrayList<>();

    public EditView(int x, int y, int width, int height, ItemStack stack, ItemModule.ModuleInstance instance, Consumer<ItemStack> preview, Consumer<Object> back) {
        super(x, y, width, height, Text.empty());
        this.preview = preview;
        this.stack = stack;
        this.instance = instance;
        float headerScale = 1.5f;
        TransformableWidget headerHolder = new TransformableWidget(x, y, width, height, headerScale);
        defaultChildren.add(headerHolder);


        ScrollingTextWidget header = new ScrollingTextWidget((int) ((this.x + 5) / headerScale), (int) (this.y / headerScale), (int) ((this.width - 10) / headerScale), Text.translatable(Miapi.MOD_ID + ".ui.edit.header"), ColorHelper.Argb.getArgb(255, 255, 255, 255));
        headerHolder.addChild(header);
        ScrollList list = new ScrollList(x, y + 16, width, height - 14, new ArrayList<>());
        defaultChildren.add(list);
        list.children().clear();
        defaultChildren.add(new SimpleButton<>(this.x + 2, this.y + this.height - 10, 40, 12, Text.translatable(Miapi.MOD_ID + ".ui.back"), stack, back));
        ArrayList<InteractAbleWidget> toList = new ArrayList<>();

        Miapi.editOptions.getFlatMap().forEach((s, editOption) -> {
            if (editOption.isVisible(stack, instance)) {
                toList.add(new SlotButton(0, 0, this.width, 15, s, editOption, instance));
            }
        });

        list.setList(toList);
        setDefaultChildren();
    }

    private void setDefaultChildren() {
        this.children().clear();
        defaultChildren.forEach(this::addChild);
    }

    public void setEditOption(EditOption option) {
        Consumer<PacketByteBuf> craftBuffer = (packetByteBuf) -> {
            //TODO:Execute this on server
            ItemStack crafted = option.execute(packetByteBuf, stack, instance);
            preview.accept(crafted);
            ScreenHandler screenHandler = Miapi.server.getPlayerManager().getPlayerList().get(0).currentScreenHandler;
            if(screenHandler instanceof CraftingScreenHandler screenHandler1){
                screenHandler1.setItem(crafted.copy());
            }
        };
        Consumer<PacketByteBuf> previewBuffer = (packetByteBuf) -> preview.accept(option.execute(packetByteBuf, stack, instance));
        this.children().clear();
        this.addChild(option.getGui(x, y, width, height, stack, instance, craftBuffer, previewBuffer, (objects) -> {
            setDefaultChildren();
        }));
    }

    class SlotButton extends InteractAbleWidget {
        private final Identifier texture = new Identifier(Miapi.MOD_ID, "textures/gui/crafter/module_button_select.png");
        private final ScrollingTextWidget textWidget;
        private final EditOption option;


        public SlotButton(int x, int y, int width, int height, String editName, EditOption option, ItemModule.ModuleInstance instance) {
            super(x, y, width, height, Text.empty());
            Text translated = StatResolver.translateAndResolve(Miapi.MOD_ID + ".edit.option." + editName, instance);
            textWidget = new ScrollingTextWidget(0, 0, this.width, translated, ColorHelper.Argb.getArgb(255, 255, 255, 255));
            this.option = option;
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
            if (isMouseOver(mouseX, mouseY)) {
                if (button == 0) {
                    setEditOption(option);
                    return true;
                }
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }
}
