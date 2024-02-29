package smartin.miapi.modules.material.palette;

import com.redpxnda.nucleus.util.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.util.math.ColorHelper;
import smartin.miapi.client.atlas.MaterialAtlasManager;
import smartin.miapi.client.renderer.NativeImageGetter;
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
public abstract class MaterialPalette extends MaterialSpriteColorer {
    protected Color paletteAverageColor;
    public boolean isAnimated = false;
    public NativeImage materialPalette;

    public MaterialPalette(Material material) {
        super(material);
    }

    public boolean isAnimated() {
        return false;
    }

    @Environment(EnvType.CLIENT)
    public NativeImage transform(SpriteContents sprite) {
        NativeImageGetter.ImageHolder rawImage = NativeImageGetter.get(sprite);
        NativeImage image = new NativeImage(rawImage.getWidth(), rawImage.getHeight(), true);
        for (int x = 0; x < rawImage.getWidth(); x++) {
            for (int y = 0; y < rawImage.getHeight(); y++) {
                if (rawImage.getOpacity(x, y) < 5 && rawImage.getOpacity(x, y) > -1) {
                    image.setColor(x, y, 0);
                } else {
                    int unsignedInt = rawImage.getRed(x, y) & 0xFF;
                    image.setColor(x, y, getColor(unsignedInt));
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
            NativeImage img = materialPalette;

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
        return materialPalette.getColor(Math.max(Math.min(color, 255), 0), 0);
    }
}
