package smartin.miapi.client.gui.crafting.crafter;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
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
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.edit_options.EditOption;
import smartin.miapi.registries.RegistryInventory;

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


        ScrollingTextWidget header = new ScrollingTextWidget((int) ((this.getX() + 5) / headerScale), (int) (this.getY() / headerScale), (int) ((this.width - 10) / headerScale), Text.translatable(Miapi.MOD_ID + ".ui.edit.header"), ColorHelper.Argb.getArgb(255, 255, 255, 255));
        headerHolder.addChild(header);
        ScrollList list = new ScrollList(x, y + 16, width, height - 14, new ArrayList<>());
        defaultChildren.add(list);
        list.children().clear();
        defaultChildren.add(new SimpleButton<>(this.getX() + 2, this.getY() + this.height - 10, 40, 12, Text.translatable(Miapi.MOD_ID + ".ui.back"), stack, back));
        ArrayList<InteractAbleWidget> toList = new ArrayList<>();

        RegistryInventory.editOptions.getFlatMap().forEach((s, editOption) -> {
            if (editOption.isVisible(stack, instance.copy())) {
                toList.add(new SlotButton(0, 0, this.width, 15, s, editOption, instance.copy()));
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
            ItemModule.ModuleInstance toCrafter = instance;
            stack.getOrCreateNbt().remove(ModularItemCache.CACHE_KEY);
            ItemStack crafted = option.execute(packetByteBuf, stack, toCrafter);
            preview.accept(crafted);
            ScreenHandler screenHandler = Miapi.server.getPlayerManager().getPlayerList().get(0).currentScreenHandler;
            if (screenHandler instanceof CraftingScreenHandler screenHandler1) {
                screenHandler1.setItem(crafted.copy());
            }
        };
        Consumer<PacketByteBuf> previewBuffer = (packetByteBuf) -> preview.accept(option.execute(packetByteBuf, stack, instance.copy()));
        this.children().clear();
        this.addChild(option.getGui(getX(), getY(), width, height, stack, instance, craftBuffer, previewBuffer, (objects) -> {
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

        public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            int hoverOffset = 0;
            if (isMouseOver(mouseX, mouseY)) {
                hoverOffset = 14;
            }
            drawTextureWithEdge(drawContext, texture, this.getX(), this.getY(), 0, hoverOffset, 140, 14, this.getWidth(), this.getHeight(), 140, 28, 2);
            textWidget.setX(this.getX() + 2);
            textWidget.setY(this.getY() + 3);

            textWidget.setWidth(this.width - 4);
            textWidget.render(drawContext, mouseX, mouseY, delta);
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
