package smartin.miapi.forge.mixin;

import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import smartin.miapi.client.model.CustomModel;

@Mixin(ModelLoader.class)
public class ModelLoaderMixin {
    @Shadow
    private void putModel(Identifier id, UnbakedModel unbakedModel) {
    }

    @Inject(at = @At("HEAD"), method = "loadModel", cancellable = true)
    private void loadModelHook(Identifier id, CallbackInfo ci) {
        if (CustomModel.isModularItem(id)) {
            System.out.println("Mixin applying to: " + id);
            putModel(id, new CustomModel());
            ci.cancel();
        }
    }
}
