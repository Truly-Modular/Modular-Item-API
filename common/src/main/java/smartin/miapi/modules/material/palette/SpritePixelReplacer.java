package smartin.miapi.modules.material.palette;

import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.util.math.ColorHelper;
import smartin.miapi.client.renderer.NativeImageGetter;
import smartin.miapi.modules.material.Material;

import java.io.IOException;

public abstract class SpritePixelReplacer extends SpriteColorer {
    public SpritePixelReplacer(Material material) {
        super(material);
    }

    protected NativeImage lastImage = null;

    public abstract int getReplacementColor(int pixelX, int pixelY, int previousAbgr);

    @Override
    public NativeImage transform(SpriteContents originalSprite) {
        NativeImageGetter.ImageHolder rawImage = NativeImageGetter.get(originalSprite);
        if (lastImage == null) {
            lastImage = new NativeImage(rawImage.getWidth(), rawImage.getHeight(), true);
            lastImage.untrack();
        }
        if (lastImage != null && (lastImage.getHeight() != rawImage.getHeight() || lastImage.getWidth() != rawImage.getWidth())) {
            lastImage.close();
            lastImage = new NativeImage(rawImage.getWidth(), rawImage.getHeight(), true);
            lastImage.untrack();
        }
        for (int x = 0; x < rawImage.getWidth(); x++) {
            for (int y = 0; y < rawImage.getHeight(); y++) {
                int abgr = rawImage.getColor(x, y);
                int opacity = ColorHelper.Abgr.getAlpha(abgr);
                if (opacity < 5 && opacity > -1) {
                    lastImage.setColor(x, y, 0);
                } else {
                    lastImage.setColor(x, y, getReplacementColor(x, y, abgr));
                }
            }
        }
        return lastImage;
    }

    @Override
    public void close() throws IOException {
        if (lastImage != null) {
            lastImage.close();
            lastImage = null;
        }
    }
}
