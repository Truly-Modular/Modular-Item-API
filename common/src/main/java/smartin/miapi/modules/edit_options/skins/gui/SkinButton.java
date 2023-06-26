package smartin.miapi.modules.edit_options.skins.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.ScrollingTextWidget;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.modules.edit_options.skins.Skin;

class SkinButton extends InteractAbleWidget implements SkinGui.SortAble {
    private final SkinGui skinGui;
    String skinPath;
    Skin skin;
    String sortAble;
    ScrollingTextWidget textWidget;
    static final int sizeY = 16;


    public SkinButton(SkinGui skinGui, int x, int y, int width, String skinPath, Skin skin) {
        super(x, y, width, sizeY, Text.empty());
        this.skinGui = skinGui;
        this.skinPath = skinPath;
        this.skin = skin;
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
        hover = skinGui.currentSkin().equals(skinPath) ? skin.textureOptions.ySize() * 2 : hover;
        drawTextureWithEdge(drawContext, skin.textureOptions.texture(), getX(), getY(), 0, hover, 100, sizeY, this.width, height, skin.textureOptions.xSize(), skin.textureOptions.ySize() * 3, skin.textureOptions.borderSize());
        textWidget.setY(this.getY() + 3);
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
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        skinGui.setCraft(skinPath);
        return true;
    }

    @Override
    public void filter(String search) {

    }

    @Override
    public String sortAndGetTop() {
        return sortAble;
    }
}
