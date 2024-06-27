package smartin.miapi.modules.material.palette;

import smartin.miapi.client.renderer.NativeImageGetter;
import smartin.miapi.modules.material.Material;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.util.FastColor;

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
                int opacity = FastColor.ABGR32.alpha(abgr);
                if (opacity < 5 && opacity > -1) {
                    lastImage.setPixelRGBA(x, y, 0);
                } else {
                    lastImage.setPixelRGBA(x, y, getReplacementColor(x, y, abgr));
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
