package smartin.miapi.client.gui.crafting.crafter.create_module;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.ScrollList;
import smartin.miapi.client.gui.ScrollingTextWidget;
import smartin.miapi.client.gui.crafting.CraftingScreen;
import smartin.miapi.client.gui.crafting.crafter.replace.CraftViewRework;
import smartin.miapi.modules.edit_options.CreateItemOption.CreateItemOption;
import smartin.miapi.modules.edit_options.EditOption;

import java.util.ArrayList;
import java.util.List;

public class CreateListView extends InteractAbleWidget {
    final EditOption.EditContext editContext;
    public ScrollList scrollList;

    public CreateListView(int x, int y, int width, int height, EditOption.EditContext editContext) {
        super(x, y, width, height, Text.empty());
        List<InteractAbleWidget> widgets = new ArrayList<>();
        this.editContext = editContext;
        CreateItemOption.createAbleItems.stream()
                .filter(item -> item.isAllowed(editContext.getPlayer(), editContext.getWorkbench()))
                .forEach(item -> widgets.add(new CreateItemEntry(0, 0, 100, 14, item)));
        scrollList = new ScrollList(getX(), getY(), getWidth(), getHeight(), widgets);
        this.addChild(scrollList);
    }

    public void create(CreateItemOption.CreateItem createItem) {
        CreateItemOption.selected = createItem;
        CraftViewRework craftView = new CraftViewRework(
                this.getX(),
                this.getY(),
                this.width,
                this.height,
                1,
                createItem.getBaseModule(),
                CreateItemOption.transform(editContext,createItem),
                (backSlot) -> {
                    this.children().clear();
                    this.addChild(scrollList);
                });
        this.children().clear();
        this.addChild(craftView);
    }

    protected class CreateItemEntry extends InteractAbleWidget {
        final CreateItemOption.CreateItem createItem;
        final ScrollingTextWidget textWidget;

        public CreateItemEntry(int x, int y, int width, int height, CreateItemOption.CreateItem item) {
            super(x, y, width, height, Text.empty());
            createItem = item;
            textWidget = new ScrollingTextWidget(x + 5, +x + 5, getWidth(), item.getName());
            this.addChild(textWidget);
        }

        @Override
        public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            int hoverOffset = 0;
            if (isMouseOver(mouseX, mouseY)) {
                hoverOffset = 14;
            }
            drawTextureWithEdge(drawContext, CraftingScreen.BACKGROUND_TEXTURE, getX(), getY(), 404, 54+hoverOffset, 108, 14, getWidth(), getHeight(), 512, 512, 3);
            textWidget.setX(this.getX() + 2);
            textWidget.setY(this.getY() + 3);
            textWidget.setWidth(this.getWidth() - 10);
            textWidget.render(drawContext, mouseX, mouseY, delta);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (isMouseOver(mouseX, mouseY)) {
                create(createItem);
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }
}
