package smartin.miapi.mixin.client;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.TextureAtlasHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TextureAtlasHolder.class)
public interface TextureAtlasHolderAccessor {
    @Accessor("textureAtlas")
    TextureAtlas getMiapiTextureAtlas();
}
