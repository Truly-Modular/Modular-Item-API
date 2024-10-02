package smartin.miapi.forge.mixin;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import smartin.miapi.client.model.DynamicBakery;

import java.util.function.Function;

@Mixin(BlockModel.class)
public abstract class ModelLoaderMixin {

    @Inject(
            at = @At("HEAD"),
            method = "Lnet/minecraft/client/renderer/block/model/BlockModel;bake(Lnet/minecraft/client/resources/model/ModelBaker;Ljava/util/function/Function;Lnet/minecraft/client/resources/model/ModelState;)Lnet/minecraft/client/resources/model/BakedModel;",
            cancellable = true)
    private void miapi$loadModelHook(ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState state, CallbackInfoReturnable<BakedModel> cir) {
        DynamicBakery.dynamicBaker = baker;
    }
}
