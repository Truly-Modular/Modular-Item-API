package smartin.miapi.modules.properties.render.baked;

import com.google.common.cache.CacheLoader;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.client.model.DynamicBakery;
import smartin.miapi.client.model.ModelLoadAccessor;
import smartin.miapi.item.modular.Transform;
import smartin.miapi.material.MaterialProperty;
import smartin.miapi.mixin.client.ModelLoaderInterfaceAccessor;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to load and manage models easier
 */
public class ModelManager {
    /**
     * load and unsure a model is loaded even using [material.texture] links.
     * scans all known material variants as well
     * @param filePath
     */
    public static void loadModelsByPath(String filePath) {
        String materialKey = "[material.texture]";
        Map<String, BlockModel> models = new HashMap<>();
        if (filePath.contains(materialKey)) {
            try {
                String path = filePath.replace(materialKey, "default");
                BlockModel model = loadModelFromFilePath(path);
                models.put("default", model);
            } catch (FileNotFoundException | CacheLoader.InvalidCacheLoadException fileNotFoundException) {
                throw new RuntimeException(fileNotFoundException);
            }
            MaterialProperty.getTextureKeys().forEach((path) -> {
                try {
                    String fullPath = filePath.replace(materialKey, path);
                    BlockModel model = loadModelFromFilePath(fullPath);
                    if (model != null) {
                        models.put(path, model);
                    }
                } catch (FileNotFoundException | CacheLoader.InvalidCacheLoadException ignored) {
                }
            });
        } else {
            try {
                BlockModel model = loadModelFromFilePath(filePath);
                models.put("default", model);
            } catch (FileNotFoundException | CacheLoader.InvalidCacheLoadException fileNotFoundException) {
                throw new RuntimeException(fileNotFoundException);
            }
        }
    }

    protected static BlockModel loadModelFromFilePath(String filePath2) throws FileNotFoundException {
        if (ModelProperty.modelCache.containsKey(filePath2)) {
            return ModelProperty.modelCache.get(filePath2).model();
        }
        if (!filePath2.endsWith(".json")) {
            filePath2 += ".json";
        }
        if (filePath2.contains("item/") && !filePath2.contains("models/")) {
            filePath2 = filePath2.replace("item/", "models/item/");
        }
        ModelBakery loader = ModelLoadAccessor.getLoader();
        filePath2 = filePath2.replace(".json", "");
        filePath2 = filePath2.replace("models/", "");
        ResourceLocation modelId = ResourceLocation.parse(filePath2);
        BlockModel model = ((ModelLoaderInterfaceAccessor) loader).loadModelFromPath(modelId);
        if (!filePath2.endsWith(".json")) {
            filePath2 += ".json";
        }
        if (filePath2.contains("item/") && !filePath2.contains("models/")) {
            filePath2 = filePath2.replace("item/", "models/item/");
        }
        UnbakedModelHolder holder = new UnbakedModelHolder(model, ModelMetadata.fromPath(ModelBakery.MODEL_LISTER.idToFile(modelId)));
        ModelProperty.modelCache.put(filePath2, holder);
        ModelProperty.modelCache.put(modelId.toString(), holder);
        model.getOverrides().forEach(modelOverride -> {
            try {
                loadModelFromFilePath(modelOverride.getModel().toString());
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
        loadTextureDependencies(model);
        return model;
    }

    protected static void loadTextureDependencies(BlockModel model) {
        DynamicBakery.bakeModel(model, (identifier) -> ModelProperty.textureGetter.apply(identifier), 0, Transform.IDENTITY);
    }
}
