package smartin.miapi.mixin.client;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.SpriteContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import smartin.miapi.client.renderer.NativeImageGetter;

@Mixin(SpriteContents.class)
public abstract class SpriteContentsNativeImageGetter {

    @Inject(
            method = "upload",
            at = @At("HEAD")
    )
    private void miapi$customItemRenderingEntityGetter(int x, int y, int unpackSkipPixels, int unpackSkipRows, NativeImage[] images, CallbackInfo ci) {
        SpriteContents spriteContents = (SpriteContents) (Object) (this);
        //Miapi.LOGGER.info("x " + x + " y " + y + " a " + unpackSkipPixels + " b " + unpackSkipRows + " i " + spriteContents.getID());
        if (((SpriteContentsAccessor) spriteContents).getImage() != images[0]) {
            NativeImageGetter.ImageHolder holder = NativeImageGetter.nativeImageMap.getOrDefault(spriteContents, new NativeImageGetter.ImageHolder());
            holder.nativeImage = images[0];
            holder.width = spriteContents.width();
            holder.height = spriteContents.height();
            holder.x = unpackSkipPixels;
            holder.y = unpackSkipRows;
            NativeImageGetter.nativeImageMap.put((SpriteContents) (Object) (this), holder);
        } else {
            NativeImageGetter.ImageHolder holder = NativeImageGetter.nativeImageMap.getOrDefault(spriteContents, new NativeImageGetter.ImageHolder());
            holder.nativeImage = images[0];
            holder.width = spriteContents.width();
            holder.height = spriteContents.height();
            holder.x = unpackSkipPixels;
            holder.y = unpackSkipRows;
            NativeImageGetter.nativeImageMap.put((SpriteContents) (Object) (this), holder);
        }
    }
}
