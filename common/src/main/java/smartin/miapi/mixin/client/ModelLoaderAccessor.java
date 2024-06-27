package smartin.miapi.mixin.client;


import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import smartin.miapi.client.model.ModelLoadAccessor;

import java.util.Map;

/**
 * Retrieves ModelLoader instance for {@link ModelLoadAccessor}
 */
@Mixin(ModelBakery.class)
public abstract class ModelLoaderAccessor {

    @Inject(method = "<init>(Lnet/minecraft/client/color/block/BlockColors;Lnet/minecraft/util/profiler/Profiler;Ljava/util/Map;Ljava/util/Map;)V", at = @At("RETURN"), cancellable = false)
    public void miapi$modelLoad(BlockColors blockColors, ProfilerFiller profiler, Map jsonUnbakedModels, Map blockStates, CallbackInfo ci){
        ModelBakery loader = (ModelBakery) (Object) this;
        ModelLoadAccessor.setLoader(loader);
    }
}
