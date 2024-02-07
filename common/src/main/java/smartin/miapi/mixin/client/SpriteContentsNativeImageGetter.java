package smartin.miapi.mixin.client;

import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.SpriteContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import smartin.miapi.client.renderer.NativeImageGetter;

@Mixin(SpriteContents.class)
public abstract class SpriteContentsNativeImageGetter {
    @Inject(
            method = "upload(IIII[Lnet/minecraft/client/texture/NativeImage;)V",
            at = @At("HEAD")
    )
    private void miapi$customItemRenderingEntityGetter(int x, int y, int unpackSkipPixels, int unpackSkipRows, NativeImage[] images, CallbackInfo ci) {
        SpriteContents spriteContents = (SpriteContents) (Object) (this);
        if(((SpriteContentsAccessor)spriteContents).getImage() != images[0]){
            NativeImageGetter.nativeImageMap.put((SpriteContents) (Object) (this), images[0]);
        }
    }
}
