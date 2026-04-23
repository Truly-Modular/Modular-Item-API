package smartin.miapi.mixin.client;

import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(SpriteSourceList.class)
public interface SpriteSourceListAccessor {

    @Accessor("sources")
    List<SpriteSource> getSourcesMiapi();

    @Invoker("<init>")
    static SpriteSourceList createSpriteSourceList(List<SpriteSource> sources) {
        throw new UnsupportedOperationException();
    }
}
