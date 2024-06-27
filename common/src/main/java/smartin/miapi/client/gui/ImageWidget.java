package smartin.miapi.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ImageWidget extends InteractAbleWidget {
    ResourceLocation texture;
    int textureSizeX;
    int textureSizeY;
    int u;
    int v;
    int regionHeight;
    int regionWidth;


    /**
     * If the Image to be rendered is to be rendered in full and has the same size as in file and on screen
     *
     * @param x       the top left X of the Image to be rendered
     * @param y       the top left Y of the Image to be rendered
     * @param width   the width of the Image on the screen
     * @param height  the height of the Image of the Screen
     * @param title   a Text that allows searching through Widgets
     * @param texture the TextureID of the Texture to be rendered
     */
    public ImageWidget(int x, int y, int width, int height, Component title, ResourceLocation texture) {
        this(x, y, width, height, title, texture, width, height);
    }

    /**
     * if the Image is to be rendered in full this can be used
     *
     * @param x            the top left X of the Image to be rendered
     * @param y            the top left Y of the Image to be rendered
     * @param width        the width of the Image on the screen
     * @param height       the height of the Image of the Screen
     * @param title        a Text that allows searching through Widgets
     * @param texture      the TextureID of the Texture to be rendered
     * @param textureSizeX the total width of the texture to be rendered
     * @param textureSizeY the total height of the texture to be rendered
     */
    public ImageWidget(int x, int y, int width, int height, Component title, ResourceLocation texture, int textureSizeX, int textureSizeY) {
        this(x, y, width, height, title, texture, 0, 0, textureSizeX, textureSizeY, textureSizeX, textureSizeY);
    }

    /**
     * @param x            the top left X of the Image to be rendered
     * @param y            the top left Y of the Image to be rendered
     * @param width        the width of the Image on the screen
     * @param height       the height of the Image of the Screen
     * @param title        a Text that allows searching through Widgets
     * @param texture      the TextureID of the Texture to be rendered
     * @param u            the u of the texture, the top left corner of the subpart of the texture to be rendered
     * @param v            the v of the texture, the top left corner of the subpart of the texture to be rendered
     * @param regionWidth  the Width of the texture on the raw file to be rendered
     * @param regionHeight the Height of the texture on the raw file to be rendered
     * @param textureSizeX the total width of the texture to be rendered
     * @param textureSizeY the total height of the texture to be rendered
     */
    public ImageWidget(int x, int y, int width, int height, Component title, ResourceLocation texture, int u, int v, int regionWidth, int regionHeight, int textureSizeX, int textureSizeY) {
        super(x, y, width, height, title);
        this.texture = texture;
        this.textureSizeX = textureSizeX;
        this.textureSizeY = textureSizeY;
        this.u = u;
        this.v = v;
        this.regionWidth = regionWidth;
        this.regionHeight = regionHeight;
    }

    @Override
    public void renderWidget(GuiGraphics drawContext, int mouseX, int mouseY, float delta) {
        drawContext.blit(texture, this.getX(), this.getY(), getWidth(), getHeight(), (float) u, (float) v, regionWidth, regionHeight, textureSizeX, textureSizeY);
        super.render(drawContext, mouseX, mouseY, delta);
    }
}
