package smartin.miapi.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import smartin.miapi.client.atlas.BufferSpriteAdder;
import smartin.miapi.config.MiapiConfig;

import java.util.ArrayList;
import java.util.List;

@Mixin(SpriteSourceList.class)
public class SpriteSourceListMixin {
    @Unique
    private static ResourceLocation blockAtlas = ResourceLocation.fromNamespaceAndPath("minecraft", "blocks");


    @ModifyReturnValue(
            method = "load(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/texture/atlas/SpriteSourceList;",
            at = @At("RETURN"))
    private static SpriteSourceList miapi$insertSprites(SpriteSourceList original, ResourceManager resourceManager, ResourceLocation sprite) {
        if (sprite != null && sprite.equals(blockAtlas) && MiapiConfig.getClientConfig().render.enableFastRender) {
            List<SpriteSource> list = new ArrayList<>(((SpriteSourceListAccessor) original).getSourcesMiapi());
            list.add(new BufferSpriteAdder());
            return SpriteSourceListAccessor.createSpriteSourceList(list);
        }
        return original;
    }
}