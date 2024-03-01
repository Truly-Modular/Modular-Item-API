package smartin.miapi.modules.material.palette;

import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.util.math.ColorHelper;
import smartin.miapi.client.renderer.NativeImageGetter;
import smartin.miapi.modules.material.Material;

public abstract class SpritePixelReplacer extends SpriteColorer {
    public SpritePixelReplacer(Material material) {
        super(material);
    }

    public abstract int getReplacementColor(int previousAbgr);

    @Override
    public NativeImage transform(SpriteContents originalSprite) {
        NativeImageGetter.ImageHolder rawImage = NativeImageGetter.get(originalSprite);
        NativeImage image = new NativeImage(rawImage.getWidth(), rawImage.getHeight(), true);
        for (int x = 0; x < rawImage.getWidth(); x++) {
            for (int y = 0; y < rawImage.getHeight(); y++) {
                int abgr = rawImage.getColor(x, y);
                int opacity = ColorHelper.Abgr.getAlpha(abgr);
                if (opacity < 5 && opacity > -1) {
                    image.setColor(x, y, 0);
                } else {
                    image.setColor(x, y, getReplacementColor(abgr));
                }
            }
        }
        image.untrack();
        return image;
    }
}
