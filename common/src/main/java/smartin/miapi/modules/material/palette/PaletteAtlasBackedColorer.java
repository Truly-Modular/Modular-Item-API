package smartin.miapi.modules.material.palette;

import com.redpxnda.nucleus.util.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.client.MiapiClient;
import smartin.miapi.client.atlas.MaterialAtlasManager;
import smartin.miapi.client.renderer.NativeImageGetter;
import smartin.miapi.mixin.client.SpriteContentsAccessor;
import smartin.miapi.modules.material.Material;

import java.util.ArrayList;
import java.util.List;

/**
 * This class focuses on implementing the Recoloring via a MaterialSprite thats 1x256 and stored in the
 * {@link MaterialAtlasManager}
 * Pro: This allows for Resource-pack Materials
 * Cons: This forces the Material into a 1x256 Texture that can be animated
 * to Control the Animation in Code extend the SpriteContents class and implement it in there
 */
public abstract class PaletteAtlasBackedColorer extends SpriteColorer {
    protected Identifier paletteSpriteId = Material.BASE_PALETTE_ID;
    protected Color paletteAverageColor;
    public boolean isAnimated = false;

    public PaletteAtlasBackedColorer(Material material) {
        super(material);
    }

    /**
     * returns the AtlasSensitive SpriteId of this Material
     *
     * @return
     */
    @Environment(EnvType.CLIENT)
    @Nullable
    public Identifier getSpriteId() {
        return paletteSpriteId;
    }

    /**
     * Sets the AtlasSensitive SpriteId.
     * This should only be called by the atlas
     *
     * @param id
     */
    @Environment(EnvType.CLIENT)
    public void setSpriteId(Identifier id) {
        paletteSpriteId = id;
    }

    /**
     * This method should be used to generate the SpriteContents
     *
     * @param id
     * @return
     */
    @Environment(EnvType.CLIENT)
    @Nullable
    public abstract SpriteContents generateSpriteContents(Identifier id);

    /**
     * This Method allows to read the SpriteContents from the ResourcePack and modify or adjust it.
     * Generally the Content from the Resourcepack should be higher priority
     *
     * @param id
     * @param fromResourcePack
     * @return
     */
    @Environment(EnvType.CLIENT)
    @Nullable
    public SpriteContents generateSpriteContents(Identifier id, @Nullable SpriteContents fromResourcePack) {
        if (fromResourcePack != null && fromResourcePack.getWidth() == 256) {
            if (isAnimatedSpite(fromResourcePack)) {
                isAnimated = true;
            }
            return fromResourcePack;
        } else {
            return generateSpriteContents(id);
        }
    }

    public boolean isAnimated() {
        return isAnimated;
    }

    /**
     * actually recolors a model Sprite
     *
     * @param sprite original model Sprite
     * @return
     */
    @Override
    @Environment(EnvType.CLIENT)
    public NativeImage transform(SpriteContents sprite) {
        NativeImageGetter.ImageHolder rawImage = NativeImageGetter.get(sprite);
        NativeImage image = new NativeImage(rawImage.getWidth(), rawImage.getHeight(), true);
        for (int x = 0; x < rawImage.getWidth(); x++) {
            for (int y = 0; y < rawImage.getHeight(); y++) {
                if (rawImage.getOpacity(x, y) < 5 && rawImage.getOpacity(x, y) > -1) {
                    image.setColor(x, y, 0);
                } else {
                    if (material != null) {
                        int unsignedInt = rawImage.getRed(x, y) & 0xFF;
                        image.setColor(x, y, getColor(unsignedInt));
                    } else {
                        image.setColor(x, y, rawImage.getColor(x, y));
                    }
                }
            }
        }
        image.untrack();
        return image;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public Color getAverageColor() {
        if (paletteAverageColor == null) {
            NativeImage img = ((SpriteContentsAccessor) MiapiClient.materialAtlasManager.getMaterialSprite(paletteSpriteId).getContents()).getImage();

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
            paletteAverageColor = new Color(red / colors.size(), green / colors.size(), blue / colors.size(), alpha / colors.size());
        }
        return paletteAverageColor;
    }

    private int getColor(int color) {
        Sprite sprite = MiapiClient.materialAtlasManager.getMaterialSprite(this.getSpriteId());
        if (sprite == null) {
            sprite = MiapiClient.materialAtlasManager.getMaterialSprite(MaterialAtlasManager.BASE_MATERIAL_ID);
        }
        if(sprite==null){
            return color;
        }
        return NativeImageGetter.get(sprite.getContents()).getColor(Math.max(Math.min(color, 255), 0), 0);
    }
}
