package smartin.miapi.material.palette;

import com.google.gson.JsonElement;
import com.mojang.blaze3d.platform.NativeImage;
import com.redpxnda.nucleus.util.Color;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.client.MiapiClient;
import smartin.miapi.client.atlas.MaterialAtlasManager;
import smartin.miapi.client.atlas.MaterialSpriteManager;
import smartin.miapi.client.renderer.NativeImageGetter;
import smartin.miapi.material.Material;
import smartin.miapi.mixin.client.SpriteContentsAccessor;

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
    protected ResourceLocation spriteId = Material.BASE_PALETTE_ID;
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
    public PaletteAtlasBackedColorer(Material material, ResourceLocation id) {
        super(material);
        setupSprite(id);
    }

    /**
     * Create a new PaletteAtlasBackedColorer from its json format
     */
    public PaletteAtlasBackedColorer(Material material, JsonElement json) {
        super(material);
        ResourceLocation id = ResourceLocation.parse(json.getAsJsonObject().get("location").getAsString());
        setupSprite(id);
    }

    @Override
    public int getReplacementColor(int pixelX, int pixelY, int previousAbgr) {
        int red = FastColor.ABGR32.red(previousAbgr);
        return image.getColor(Mth.clamp(red, 0, 255), 0);
    }

    public void setupSprite(ResourceLocation id) {
        spriteId = id;
        MiapiClient.materialAtlasManager.addSpriteToLoad(id, c -> {
            contents = c;
            isAnimated = isAnimatedSprite(c);
        });
    }

    @Nullable
    public ResourceLocation getSpriteId() {
        return spriteId;
    }

    public boolean doTick() {
        return isAnimated;
    }

    @Override
    public NativeImage transform(SpriteContents originalSprite) {
        if (contents == null) {
            TextureAtlasSprite sprite = MiapiClient.materialAtlasManager.getMaterialSprite(getSpriteId());
            if (sprite != null) {
                MaterialSpriteManager.markTextureAsAnimatedInUse(sprite);
                contents = sprite.contents();
            } else {
                contents = originalSprite;
            }
        }
        image = NativeImageGetter.get(contents);
        NativeImage result = super.transform(originalSprite);
        image = null;
        return result;
    }

    @Override
    public Color getAverageColor() {
        if (averageColor == null) {
            TextureAtlasSprite sprite = MiapiClient.materialAtlasManager.getMaterialSprite(spriteId);
            if (sprite == null) {
                return Color.WHITE;
            }
            NativeImage img = ((SpriteContentsAccessor) sprite.contents()).getImage();

            List<Color> colors = new ArrayList<>();
            int height = img.getHeight();
            int width = img.getWidth();
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    int color = img.getPixelRGBA(x, y);
                    colors.add(new Color(
                            FastColor.ABGR32.red(color),
                            FastColor.ABGR32.green(color),
                            FastColor.ABGR32.blue(color),
                            FastColor.ABGR32.alpha(color)
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
