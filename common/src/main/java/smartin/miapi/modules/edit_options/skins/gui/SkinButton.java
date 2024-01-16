package smartin.miapi.modules.edit_options.skins.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.text.Text;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.ScrollingTextWidget;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.modules.conditions.ConditionManager;
import smartin.miapi.modules.edit_options.skins.Skin;

import java.util.ArrayList;
import java.util.List;

class SkinButton extends InteractAbleWidget implements SkinGui.SortAble {
    private final SkinGui skinGui;
    String skinPath;
    Skin skin;
    String sortAble;
    ScrollingTextWidget textWidget;
    static final int SIZE_Y = 16;
    public boolean isEnabled = true;
    public boolean isAllowed = true;
    public List<Text> reasons = new ArrayList<>();


    public SkinButton(SkinGui skinGui, int x, int y, int width, String skinPath, Skin skin) {
        super(x, y, width, SIZE_Y, Text.empty());
        this.skinGui = skinGui;
        this.skinPath = skinPath;
        this.skin = skin;
        if (skin.condition != null) {
            isAllowed = skin.condition.isAllowed(new ConditionManager.ModuleConditionContext(skinGui.instance, null, MinecraftClient.getInstance().player, skinGui.instance.getProperties(), reasons));
        }
        String[] parts = skinPath.split("/");
        Text skinName = StatResolver.translateAndResolve(Miapi.MOD_ID + ".skin.name." + parts[parts.length - 1], skinGui.instance);
        sortAble = skinName.getString();
        textWidget = new ScrollingTextWidget(x + 3, y + 2, width - 6, skinName, skin.textureOptions.color());
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderTexture(0, skin.textureOptions.texture());
        int hover = this.isMouseOver(mouseX, mouseY) ? skin.textureOptions.ySize() : 0;
        if (!isAllowed) {
            hover = hover * 3;
        } else {
            hover = skinGui.currentSkin().equals(skinPath) ? skin.textureOptions.ySize() * 2 : hover;
        }
        if (isMouseOver(mouseX, mouseY)) {
            hover = skin.textureOptions.ySize();
        }
        drawTextureWithEdge(drawContext, skin.textureOptions.texture(), getX(), getY(), 0, hover, 100, SIZE_Y, this.width, height, skin.textureOptions.xSize(), skin.textureOptions.ySize() * 3, skin.textureOptions.borderSize());
        textWidget.setY(this.getY() + 4);
        textWidget.render(drawContext, mouseX, mouseY, delta);
        children().forEach(element -> {
            if (element instanceof Drawable drawable) {
                drawable.render(drawContext, mouseX, mouseY, delta);
            }
        });
        if (isMouseOver(mouseX, mouseY)) {
            skinGui.setPreview(skinPath);
        }
    }

    @Override
    public void renderHover(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        if (!isAllowed) {
            if (isMouseOver(mouseX, mouseY)) {
                drawContext.drawTooltip(MinecraftClient.getInstance().textRenderer, reasons, mouseX, mouseY);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOver(mouseX, mouseY) && isAllowed) {
            skinGui.setCraft(skinPath);
            return true;
        }
        return false;
    }

    @Override
    public void filter(String search) {
        this.isEnabled = this.skinPath.toLowerCase().contains(search.toLowerCase()) || this.textWidget.getText().toString().toLowerCase().contains(search.toLowerCase());
    }

    @Override
    public String sortAndGetTop() {
        return sortAble;
    }

    @Override
    public boolean isActive() {
        return isEnabled && isAllowed;
    }
}
