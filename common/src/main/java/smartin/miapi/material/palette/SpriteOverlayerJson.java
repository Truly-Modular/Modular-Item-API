package smartin.miapi.material.palette;

import com.google.gson.JsonElement;
import com.mojang.blaze3d.platform.NativeImage;
import com.redpxnda.nucleus.util.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import smartin.miapi.client.AnimatedTexturesManager;
import smartin.miapi.client.renderer.NativeImageGetter;
import smartin.miapi.material.base.Material;

import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class SpriteOverlayerJson extends SpritePixelReplacer {
    public final Color averageColor;
    public final SpriteFromJson delegate;
    protected NativeImageGetter.ImageHolder overlayImage;

    public SpriteOverlayerJson(Material material, JsonElement json) {
        super(material);
        delegate = SpriteFromJson.getFromJson(json);
        averageColor = delegate.getAverageColor();
    }

    @Override
    public void tick(Consumer<NativeImage> nativeImageConsumer, SpriteContents spriteContents) {
        super.tick(nativeImageConsumer, spriteContents);
        AnimatedTexturesManager.markAnimated(delegate.rawSprite);
    }

    @Override
    public Color getAverageColor() {
        return averageColor;
    }

    @Override
    public int getReplacementColor(int x, int y, int previousAbgr) {
        int abgr = overlayImage.getColor(x % overlayImage.getWidth(), y % overlayImage.getHeight());
        int alpha = FastColor.ABGR32.alpha(abgr);

        if (alpha != 255) {
            float overlayA = alpha / 255f;
            int overlayR = FastColor.ABGR32.red(abgr);
            int overlayG = FastColor.ABGR32.green(abgr);
            int overlayB = FastColor.ABGR32.blue(abgr);

            float baseA = FastColor.ABGR32.alpha(previousAbgr) / 255f;
            int baseR = FastColor.ABGR32.red(previousAbgr);
            int baseG = FastColor.ABGR32.green(previousAbgr);
            int baseB = FastColor.ABGR32.blue(previousAbgr);

            int newR = Mth.lerpInt(overlayA, baseR, overlayR);
            int newG = Mth.lerpInt(overlayA, baseG, overlayG);
            int newB = Mth.lerpInt(overlayA, baseB, overlayB);
            int newA = (int) (Mth.lerp(baseA, overlayA, 1) * 255);

            return FastColor.ABGR32.color(newA, newB, newG, newR);
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
