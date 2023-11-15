package smartin.miapi.client.gui.crafting.crafter.replace;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.*;
import smartin.miapi.client.gui.crafting.CraftingScreen;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.edit_options.EditOption;
import smartin.miapi.modules.properties.AllowedSlots;
import smartin.miapi.modules.properties.CraftingConditionProperty;
import smartin.miapi.modules.properties.PriorityProperty;
import smartin.miapi.modules.properties.SlotProperty;

import java.util.*;
import java.util.function.Consumer;

/**
 * The Module Selector when clicked on Relplace, a simple list of modules that fit in the slot
 */
@Environment(EnvType.CLIENT)
public class ReplaceView extends InteractAbleWidget {
    Consumer<CraftOption> craft;
    Consumer<CraftOption> preview;
    SlotButton lastPreview;
    public static List<CraftOptionSupplier> optionSuppliers = new ArrayList<>();

    public ReplaceView(int x, int y, int width, int height, SlotProperty.ModuleSlot slot, EditOption.EditContext editContext, Consumer<SlotProperty.ModuleSlot> back, Consumer<CraftOption> craft, Consumer<CraftOption> preview) {
        super(x, y, width, height, Text.empty());
        this.craft = craft;
        this.preview = preview;
        float headerScale = 1.0f;
        TransformableWidget headerHolder = new TransformableWidget(x, y, width, height, headerScale);
        addChild(headerHolder);


        ScrollingTextWidget header = new ScrollingTextWidget((int) ((this.getX() + 5) / headerScale), (int) (this.getY() / headerScale) + 3, (int) ((this.width - 10) / headerScale), Text.translatable(Miapi.MOD_ID + ".ui.replace.header"), ColorHelper.Argb.getArgb(255, 255, 255, 255));
        headerHolder.addChild(header);
        ScrollList list = new ScrollList(x, y + 14, width, height - 16, new ArrayList<>());
        addChild(list);
        list.children().clear();
        ArrayList<InteractAbleWidget> toList = new ArrayList<>();
        toList.add(new SlotButton(0, 0, this.width, 15, new CraftOption(ItemModule.empty, new HashMap<>())));
        List<CraftOption> craftOptions = new ArrayList<>();
        AllowedSlots.allowedIn(slot).stream()
                .sorted(Comparator.comparingDouble(PriorityProperty::getFor))
                .forEach(module -> {
                    if (CraftingConditionProperty.isVisible(slot, module, MinecraftClient.getInstance().player, null)) {
                        craftOptions.add(new CraftOption(module, new HashMap<>()));
                    }
                });
        optionSuppliers.forEach(craftOptionSupplier -> craftOptions.addAll(craftOptionSupplier.getOption(editContext)));
        craftOptions.stream().sorted(Comparator.comparingDouble(a -> PriorityProperty.getFor(a.module()))).forEach(craftOption -> {
            toList.add(new SlotButton(0, 0, this.width, 15, craftOption));
        });
        list.setList(toList);
    }

    public interface CraftOptionSupplier {
        List<CraftOption> getOption(EditOption.EditContext option);
    }

    public void setPreview(CraftOption option) {
        preview.accept(option);
    }

    class SlotButton extends InteractAbleWidget {
        private final Identifier texture = new Identifier(Miapi.MOD_ID, "textures/gui/crafter/module_button_select.png");
        private ScrollingTextWidget textWidget;
        private CraftOption option;
        private boolean isAllowed = true;
        private HoverDescription hoverDescription;


        public SlotButton(int x, int y, int width, int height, CraftOption option) {
            super(x, y, width, height, Text.empty());
            String moduleName = option.module().getName();
            this.option = option;
            ItemModule.ModuleInstance instance = new ItemModule.ModuleInstance(option.module());
            Text translated = StatResolver.translateAndResolve(Miapi.MOD_ID + ".module." + moduleName, instance);
            textWidget = new ScrollingTextWidget(0, 0, this.width, translated, ColorHelper.Argb.getArgb(255, 255, 255, 255));
            isAllowed = CraftingConditionProperty.isCraftable(null, option.module(), MinecraftClient.getInstance().player, null);
            List<Text> texts = new ArrayList<>();
            if (!isAllowed) {
                texts = CraftingConditionProperty.getReasonsForCraftable(null, option.module(), MinecraftClient.getInstance().player, null);
            }
            hoverDescription = new HoverDescription(x, y, texts);
        }

        public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            int hoverOffset = 0;
            if (isMouseOver(mouseX, mouseY)) {
                hoverOffset = 14;
            }
            if (!isAllowed) {
                hoverOffset = 28;
            }
            drawTextureWithEdge(drawContext, CraftingScreen.BACKGROUND_TEXTURE, getX(), getY(), 404, 54 + hoverOffset, 108, 14, getWidth(), getHeight(), 512, 512, 3);
            textWidget.setX(this.getX() + 2);
            textWidget.setY(this.getY() + 3);

            textWidget.setWidth(this.width - 4);
            textWidget.render(drawContext, mouseX, mouseY, delta);
            if (isMouseOver(mouseX, mouseY) && !isAllowed && hoverDescription != null) {
                hoverDescription.render(drawContext, mouseX, mouseY, delta);
            }

        }

        public void renderHover(DrawContext drawContext, int mouseX, int mouseY, float delta) {
            if (isMouseOver(mouseX, mouseY) && !isAllowed && hoverDescription != null) {
                hoverDescription.setX(this.getX());
                hoverDescription.setY(this.getY() + this.getHeight());
                hoverDescription.render(drawContext, mouseX, mouseY, delta);
            }
        }

        public boolean isMouseOver(double mouseX, double mouseY) {
            boolean isOver = super.isMouseOver(mouseX, mouseY);
            if (!this.equals(lastPreview) && isOver) {
                lastPreview = this;
                setPreview(option);
            }
            return isOver;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (isMouseOver(mouseX, mouseY) && (button == 0) && isAllowed) {
                playClickedSound();
                craft.accept(option);
                return true;

            }
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }
}
