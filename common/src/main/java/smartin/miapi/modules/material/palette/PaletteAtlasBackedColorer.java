package smartin.miapi.modules.material.palette;

import com.google.gson.JsonElement;
import com.redpxnda.nucleus.util.Color;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.client.MiapiClient;
import smartin.miapi.client.atlas.MaterialAtlasManager;
import smartin.miapi.client.renderer.NativeImageGetter;
import smartin.miapi.mixin.client.SpriteContentsAccessor;
import smartin.miapi.modules.material.Material;

import java.util.ArrayList;
import java.util.List;

/**
 * Uses textures in {@link MaterialAtlasManager} and treats them as the colors of a {@link GrayscalePaletteColorer} to recolor module sprites. <br>
 * Essentially, textures in the atlas must be 1x256(or (f)x256 when f is the number of animated frames). The x position of each pixel represents
 * the grayscale value to replace with that pixel. For example, if index 8 had #F038AD as its color, all #080808 pixels of the module sprite will
 * be replaced with #F038AD.
 * This is to be used when you need a simple animated color palette.
 */
public class PaletteAtlasBackedColorer extends SpritePixelReplacer {
    protected Identifier spriteId = Material.BASE_PALETTE_ID;
    protected Color averageColor;
    protected boolean isAnimated = false;
    protected NativeImageGetter.ImageHolder image;
    protected SpriteContents contents;

    protected PaletteAtlasBackedColorer(Material material) {
        super(material);
    }

    /**
     * Create a new PaletteAtlasBackedColorer, uploading the texture at the specified id
     */
    public PaletteAtlasBackedColorer(Material material, Identifier id) {
        super(material);
        setupSprite(id);
    }

    /**
     * Create a new PaletteAtlasBackedColorer from its json format
     */
    public PaletteAtlasBackedColorer(Material material, JsonElement json) {
        super(material);
        Identifier id = new Identifier(json.getAsJsonObject().get("location").getAsString());
        setupSprite(id);
    }

    @Override
    public int getReplacementColor(int pixelX, int pixelY, int previousAbgr) {
        int red = ColorHelper.Abgr.getRed(previousAbgr);
        return image.getColor(MathHelper.clamp(red, 0, 255), 0);
    }

    public void setupSprite(Identifier id) {
        spriteId = id;
        MiapiClient.materialAtlasManager.addSpriteToLoad(id, c -> {
            contents = c;
            isAnimated = isAnimatedSprite(c);
        });
    }

    @Nullable
    public Identifier getSpriteId() {
        return spriteId;
    }

    public boolean isAnimated() {
        return isAnimated;
    }

    @Override
    public NativeImage transform(SpriteContents originalSprite) {
        if (contents == null) contents = MiapiClient.materialAtlasManager.getMaterialSprite(getSpriteId()).getContents();
        image = NativeImageGetter.get(contents);
        NativeImage result = super.transform(originalSprite);
        image = null;
        return result;
    }

    @Override
    public Color getAverageColor() {
        if (averageColor == null) {
            NativeImage img = ((SpriteContentsAccessor) MiapiClient.materialAtlasManager.getMaterialSprite(spriteId).getContents()).getImage();

            List<Color> colors = new ArrayList<>();
            int height = img.getHeight();
            int width = img.getWidth();
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    int color = img.getColor(x, y);
                    colors.add(new Color(
                            ColorHelper.Abgr.getRed(color),
                            ColorHelper.Abgr.getGreen(color),
                            ColorHelper.Abgr.getBlue(color),
                            ColorHelper.Abgr.getAlpha(color)
                    ));
                }
            }

            int red = 0;
            int green = 0;
            int blue = 0;
            int alpha = 0;
            for (Color color : colors) {
                if (color.a() > 0) {
                    red += color.r();
                    green += color.g();
                    blue += color.b();
                    alpha += color.a();
                }
            }
            averageColor = new Color(red / colors.size(), green / colors.size(), blue / colors.size(), alpha / colors.size());
        }
        return averageColor;
    }
}
