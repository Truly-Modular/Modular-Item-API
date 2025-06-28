package smartin.miapi.client;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class AnimatedTexturesManager {

    public static void markAnimated(TextureAtlasSprite textureAtlasSprite) {
        if (MiapiClient.isSodiumLoaded() && textureAtlasSprite != null) {
            SodiumStuffs.markAnimated(textureAtlasSprite);
        }
    }
}
