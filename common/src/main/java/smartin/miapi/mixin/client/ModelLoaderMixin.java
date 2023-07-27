package smartin.miapi.mixin.client;


import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import smartin.miapi.Miapi;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Mixin(ModelLoader.class)
abstract class ModelLoaderMixin {


    @ModifyVariable(
            method = "<init>(Lnet/minecraft/client/color/block/BlockColors;Lnet/minecraft/util/profiler/Profiler;Ljava/util/Map;Ljava/util/Map;)V",
            at = @At(value = "HEAD"),
            ordinal = 0)
    private static Map<Identifier, JsonUnbakedModel> miapi$jsonUnbakedModels(Map<Identifier, JsonUnbakedModel> jsonUnbakedModels) {
        /*
        Map<Identifier, Resource> rawModels = MinecraftClient.getInstance().getResourceManager().findResources("models", (identifier -> identifier.getNamespace().equals(Miapi.MOD_ID)));

        Map<Identifier, JsonUnbakedModel> mutableMap = new HashMap<>(jsonUnbakedModels);

        rawModels.forEach((id, recource) -> {
            try {
                Miapi.LOGGER.error("loaded " + id.toString());
                mutableMap.put(id, JsonUnbakedModel.deserialize(recource.getReader()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        return mutableMap;

         */
        return jsonUnbakedModels;
    }
}
