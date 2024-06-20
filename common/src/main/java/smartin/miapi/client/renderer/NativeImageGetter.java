package smartin.miapi.client.renderer;

import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.SpriteContents;
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
        imageHolder.nativeImage = ((SpriteContentsAccessor) contents).getImage();
        imageHolder.height = contents.getHeight();
        imageHolder.width = contents.getWidth();
        return imageHolder;
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
            return nativeImage.getFormat();
        }

        public int getColor(int x, int y) {
            return nativeImage.getColor(x + this.x, y + this.y);
        }

        public void setColor(int x, int y, int color) {
            nativeImage.setColor(x + this.x, y + this.y, color);
        }

        public void setLuminance(int x, int y, byte luminance) {
            nativeImage.setLuminance(x + this.x, y + this.y, luminance);
        }

        public byte getRed(int x, int y) {
            return nativeImage.getRed(x + this.x, y + this.y);
        }

        public byte getGreen(int x, int y) {
            return nativeImage.getGreen(x + this.x, y + this.y);
        }

        public byte getBlue(int x, int y) {
            return nativeImage.getBlue(x + this.x, y + this.y);
        }

        public byte getOpacity(int x, int y) {
            return nativeImage.getOpacity(x + this.x, y + this.y);
        }

    }
}
