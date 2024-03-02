package smartin.miapi.modules.material.palette;

import com.google.gson.JsonElement;
import com.redpxnda.nucleus.util.Color;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.SpriteContents;
import smartin.miapi.client.renderer.NativeImageGetter;
import smartin.miapi.modules.material.Material;

public class SpriteOverlayer extends SpriteColorer {
    public final Color averageColor;
    public final SpriteFromJson delegate;

    public SpriteOverlayer(Material material, JsonElement json) {
        super(material);
        delegate = new SpriteFromJson(json);
        averageColor = delegate.getAverageColor();
    }

    @Override
    public Color getAverageColor() {
        return averageColor;
    }

    @Override
    public NativeImage transform(SpriteContents sprite) {
        NativeImageGetter.ImageHolder rawImage = NativeImageGetter.get(sprite);
        NativeImageGetter.ImageHolder overlayImage = delegate.getNativeImage();
        NativeImage image = new NativeImage(rawImage.getWidth(), rawImage.getHeight(), true);

        for (int x = 0; x < rawImage.getWidth(); x++) {
            for (int y = 0; y < rawImage.getHeight(); y++) {
                if (rawImage.getOpacity(x, y) < 5 && rawImage.getOpacity(x, y) > -1) {
                    image.setColor(x, y, 0);
                } else {
                    image.setColor(x, y, overlayImage.getColor(x % overlayImage.getWidth(), y % overlayImage.getHeight()));
                }
            }
        }
        image.untrack();
        return image;
    }

    @Override
    public boolean isAnimated() {
        return delegate.isAnimated();
    }
}
