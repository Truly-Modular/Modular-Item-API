package smartin.miapi.mixin.client;


import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Map;

@Mixin(ModelBakery.class)
abstract class ModelLoaderMixin {


    @ModifyVariable(
            method = "<init>",
            at = @At(value = "HEAD"),
            ordinal = 0)
    private static Map<ResourceLocation, BlockModel> miapi$jsonUnbakedModels(Map<ResourceLocation, BlockModel> jsonUnbakedModels) {
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
