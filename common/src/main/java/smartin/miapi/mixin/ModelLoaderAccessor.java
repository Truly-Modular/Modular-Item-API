package smartin.miapi.mixin;


import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import smartin.miapi.client.model.ModelLoadAccessor;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Retrieves ModelLoader instance for {@link ModelLoadAccessor}
 */
@Mixin(ModelLoader.class)
public abstract class ModelLoaderAccessor {

    @Inject(method = "<init>(Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/client/color/block/BlockColors;Lnet/minecraft/util/profiler/Profiler;I)V", at = @At("RETURN"), cancellable = false)
    public void modelLoad(ResourceManager resourceManager, BlockColors blockColors, Profiler profiler, int mipmapLevel, CallbackInfo ci){
        ModelLoader loader = (ModelLoader) (Object) this;
        ModelLoadAccessor.setLoader(loader);
    }
}
