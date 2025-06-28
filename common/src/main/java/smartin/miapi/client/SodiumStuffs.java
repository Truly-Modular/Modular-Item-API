package smartin.miapi.client;

import net.caffeinemc.mods.sodium.api.texture.SpriteUtil;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class SodiumStuffs {

    public static void markAnimated(TextureAtlasSprite sprite){
        SpriteUtil.INSTANCE.markSpriteActive(sprite);
    }
}
