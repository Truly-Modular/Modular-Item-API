package smartin.miapi.mixin.client;


import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import smartin.miapi.client.model.ModelLoadAccessor;

import java.util.Map;

/**
 * Retrieves ModelLoader instance for {@link ModelLoadAccessor}
 */
@Mixin(ModelLoader.class)
public abstract class ModelLoaderAccessor {

    @Inject(method = "<init>(Lnet/minecraft/client/color/block/BlockColors;Lnet/minecraft/util/profiler/Profiler;Ljava/util/Map;Ljava/util/Map;)V", at = @At("RETURN"), cancellable = false)
    public void miapi$modelLoad(BlockColors blockColors, Profiler profiler, Map jsonUnbakedModels, Map blockStates, CallbackInfo ci){
        ModelLoader loader = (ModelLoader) (Object) this;
        ModelLoadAccessor.setLoader(loader);
    }
}
