package smartin.miapi.mixin.client;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.client.renderer.texture.atlas.SpriteSources;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(SpriteSources.class)
public interface SpriteSourcesAccessor {
    @Invoker
    static SpriteSourceType callRegister(String name, MapCodec<? extends SpriteSource> codec) {
        throw new UnsupportedOperationException();
    }
}
