package smartin.miapi.client.renderer;

import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.SpriteContents;
import smartin.miapi.mixin.client.SpriteContentsAccessor;

import java.util.Map;
import java.util.WeakHashMap;

public class NativeImageGetter {
    public static Map<SpriteContents, NativeImage> nativeImageMap = new WeakHashMap<>();

    public static NativeImage get(SpriteContents contents) {
        return nativeImageMap.getOrDefault(contents, ((SpriteContentsAccessor) contents).getImage());
    }
}
