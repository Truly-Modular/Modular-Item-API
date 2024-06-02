package smartin.miapi.modules.material.palette;

import com.google.gson.JsonElement;
import com.redpxnda.nucleus.util.Color;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import smartin.miapi.client.renderer.NativeImageGetter;
import smartin.miapi.modules.material.Material;

public class SpriteOverlayer extends SpritePixelReplacer {
    public final Color averageColor;
    public final SpriteFromJson delegate;
    protected NativeImageGetter.ImageHolder overlayImage;

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
    public int getReplacementColor(int x, int y, int previousAbgr) {
        int abgr = overlayImage.getColor(x % overlayImage.getWidth(), y % overlayImage.getHeight());
        int alpha = ColorHelper.Abgr.getAlpha(abgr);

        if (alpha != 255) {
            float overlayA = alpha/255f;
            int overlayR = ColorHelper.Abgr.getRed(abgr);
            int overlayG = ColorHelper.Abgr.getGreen(abgr);
            int overlayB = ColorHelper.Abgr.getBlue(abgr);

            float baseA = ColorHelper.Abgr.getAlpha(previousAbgr)/255f;
            int baseR = ColorHelper.Abgr.getRed(previousAbgr);
            int baseG = ColorHelper.Abgr.getGreen(previousAbgr);
            int baseB = ColorHelper.Abgr.getBlue(previousAbgr);

            int newR = MathHelper.lerp(overlayA, baseR, overlayR);
            int newG = MathHelper.lerp(overlayA, baseG, overlayG);
            int newB = MathHelper.lerp(overlayA, baseB, overlayB);
            int newA = (int) (MathHelper.lerp(baseA, overlayA, 1)*255);

            return ColorHelper.Abgr.getAbgr(newA, newB, newG, newR);
        }

        return abgr;
    }

    @Override
    public NativeImage transform(SpriteContents sprite) {
        overlayImage = delegate.getNativeImage(); // temporarily saving overlay image for getReplacementColor
        delegate.markUse();
        NativeImage result = super.transform(sprite);
        overlayImage = null;
        return result;
    }

    @Override
    public boolean doTick() {
        return delegate.isAnimated();
    }
}
