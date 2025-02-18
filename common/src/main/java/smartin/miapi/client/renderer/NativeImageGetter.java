package smartin.miapi.client.renderer;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.SpriteContents;
import smartin.miapi.mixin.client.SpriteContentsAccessor;

import java.util.Map;
import java.util.WeakHashMap;

public class NativeImageGetter {
    public static Map<SpriteContents, ImageHolder> nativeImageMap = new WeakHashMap<>();

    public static ImageHolder get(SpriteContents contents) {
        return nativeImageMap.getOrDefault(contents, getFromContents(contents));
    }

    public static ImageHolder getFromContents(SpriteContents contents) {
        ImageHolder imageHolder = new ImageHolder();
        imageHolder.nativeImage = getImage(contents);
        imageHolder.height = contents.height();
        imageHolder.width = contents.width();
        return imageHolder;
    }

    public static NativeImage getImage(SpriteContents contents) {
        return ((SpriteContentsAccessor) contents).getImage();
    }

    public static ImageHolder getFromContents(NativeImage contents) {
        ImageHolder imageHolder = new ImageHolder();
        imageHolder.nativeImage = contents;
        imageHolder.height = contents.getHeight();
        imageHolder.width = contents.getWidth();
        return imageHolder;
    }

    public static class ImageHolder {
        public NativeImage nativeImage;
        public int x = 0;
        public int y = 0;
        public int width = 16;
        public int height = 16;

        public int getWidth() {
            return this.width;
        }

        public int getHeight() {
            return this.height;
        }

        public NativeImage.Format getFormat() {
            return nativeImage.format();
        }

        public int getColor(int x, int y) {
            return nativeImage.getPixelRGBA(x + this.x, y + this.y);
        }

        public void setColor(int x, int y, int color) {
            nativeImage.setPixelRGBA(x + this.x, y + this.y, color);
        }

        public void setLuminance(int x, int y, byte luminance) {
            nativeImage.setPixelLuminance(x + this.x, y + this.y, luminance);
        }

        public byte getRed(int x, int y) {
            return nativeImage.getRedOrLuminance(x + this.x, y + this.y);
        }

        public byte getGreen(int x, int y) {
            return nativeImage.getGreenOrLuminance(x + this.x, y + this.y);
        }

        public byte getBlue(int x, int y) {
            return nativeImage.getBlueOrLuminance(x + this.x, y + this.y);
        }

        public byte getOpacity(int x, int y) {
            return nativeImage.getLuminanceOrAlpha(x + this.x, y + this.y);
        }

    }
}
