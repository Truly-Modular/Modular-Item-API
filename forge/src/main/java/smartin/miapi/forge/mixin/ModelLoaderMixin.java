package smartin.miapi.forge.mixin;

import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import smartin.miapi.client.model.item.ItemBakedModelReplacement;
import smartin.miapi.registries.RegistryInventory;

import java.util.Map;
import java.util.Objects;

@Mixin(ModelLoader.class)
public abstract class ModelLoaderMixin {
    @Shadow
    private void putModel(Identifier id, UnbakedModel unbakedModel) {
    }

    @Shadow
    @Final
    private Map<Identifier, UnbakedModel> unbakedModels;

    @Inject(at = @At("HEAD"), method = "loadModel", cancellable = true)
    private void miapi$loadModelHook(Identifier id, CallbackInfo ci) {
        if (isModularItem(id) && id instanceof ModelIdentifier model && Objects.equals(model.getVariant(), "inventory")) {
            Identifier identifier = id.withPrefixedPath("item/");
            UnbakedModel unbaked = new ItemBakedModelReplacement();
            putModel(model, unbaked);
            unbakedModels.put(identifier, unbaked);
            ci.cancel();
        }
    }

    private static boolean isModularItem(Identifier identifier) {
        if (identifier != null && identifier.toString() != null) {
            return RegistryInventory.modularItems.get(identifier.toString().replace("item/", "").replace("#inventory", "")) != null;
        }
        return false;
    }
}
