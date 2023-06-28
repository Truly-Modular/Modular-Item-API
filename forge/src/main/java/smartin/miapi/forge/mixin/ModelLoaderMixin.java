package smartin.miapi.forge.mixin;

import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import smartin.miapi.client.model.CustomModel;
import smartin.miapi.registries.RegistryInventory;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Mixin(ModelLoader.class)
public abstract class ModelLoaderMixin {
    @Shadow
    private void putModel(Identifier id, UnbakedModel unbakedModel) {}

    @Shadow @Final private Map<Identifier, UnbakedModel> unbakedModels;

    @Shadow @Final private Map<Identifier, UnbakedModel> modelsToBake;

    @Inject(at = @At("HEAD"), method = "loadModel", cancellable = true)
    private void loadModelHook(Identifier id, CallbackInfo ci) {
        if (isModularItem(id) && id instanceof ModelIdentifier model && Objects.equals(model.getVariant(), "inventory")) {
            Identifier identifier = id.withPrefixedPath("item/");
            UnbakedModel unbaked = new CustomModel();
            putModel(model, unbaked);
            unbakedModels.put(identifier, unbaked);
            ci.cancel();
        }
    }

    private static boolean isModularItem(Identifier identifier){
        return RegistryInventory.modularItems.get(identifier.toString().replace("item/","").replace("#inventory", ""))!=null;
    }
}
