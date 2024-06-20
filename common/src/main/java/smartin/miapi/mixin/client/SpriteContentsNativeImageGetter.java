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
        //Miapi.LOGGER.info("x " + x + " y " + y + " a " + unpackSkipPixels + " b " + unpackSkipRows + " i " + spriteContents.getId());
        if (((SpriteContentsAccessor) spriteContents).getImage() != images[0]) {
            NativeImageGetter.ImageHolder holder = NativeImageGetter.nativeImageMap.getOrDefault(spriteContents, new NativeImageGetter.ImageHolder());
            holder.nativeImage = images[0];
            holder.width = spriteContents.getWidth();
            holder.height = spriteContents.getHeight();
            holder.x = unpackSkipPixels;
            holder.y = unpackSkipRows;
            NativeImageGetter.nativeImageMap.put((SpriteContents) (Object) (this), holder);
        } else {
            NativeImageGetter.ImageHolder holder = NativeImageGetter.nativeImageMap.getOrDefault(spriteContents, new NativeImageGetter.ImageHolder());
            holder.nativeImage = images[0];
            holder.width = spriteContents.getWidth();
            holder.height = spriteContents.getHeight();
            holder.x = unpackSkipPixels;
            holder.y = unpackSkipRows;
            NativeImageGetter.nativeImageMap.put((SpriteContents) (Object) (this), holder);
        }
    }
}
