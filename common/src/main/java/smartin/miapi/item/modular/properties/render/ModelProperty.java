package smartin.miapi.item.modular.properties.render;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.ModelRotation;
import net.minecraft.client.render.model.json.ItemModelGenerator;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.render.model.json.ModelElement;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.client.model.ColorUtil;
import smartin.miapi.client.model.DynamicBakedModel;
import smartin.miapi.client.model.ModelLoadAccessor;
import smartin.miapi.datapack.SpriteLoader;
import smartin.miapi.item.modular.ItemModule;
import smartin.miapi.item.modular.Transform;
import smartin.miapi.item.modular.TransformStack;
import smartin.miapi.item.modular.cache.ModularItemCache;
import smartin.miapi.item.modular.properties.MaterialProperty;
import smartin.miapi.item.modular.properties.ModuleProperty;
import smartin.miapi.item.modular.properties.SlotProperty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class ModelProperty implements ModuleProperty {
    private static final String CACHE_KEY_MAP = Miapi.MOD_ID + ":modelMap";
    private static final String CACHE_KEY_ITEM = Miapi.MOD_ID + ":itemModelodel";
    private static final Map<String, ModelJson> modelMap = new HashMap<>();
    private static final Map<String, JsonUnbakedModel> loadedMap = new HashMap<>();
    public static final String KEY = "texture";
    public static final List<ModelTransformer> modelTransformers = new ArrayList<>();
    public static Function<SpriteIdentifier, Sprite> textureGetter;
    private static Function<SpriteIdentifier, Sprite> mirroredGetter;
    private static ItemModelGenerator generator;

    public ModelProperty() {
        mirroredGetter = (identifier) -> textureGetter.apply(identifier);
        generator = new ItemModelGenerator();
        ModularItemCache.setSupplier(CACHE_KEY_ITEM, (stack) -> getModelMap(stack).get("item"));
        ModularItemCache.setSupplier(CACHE_KEY_MAP, ModelProperty::generateModels);
    }

    public static Map<String, DynamicBakedModel> getModelMap(ItemStack stack) {
        return (Map<String, DynamicBakedModel>) ModularItemCache.get(stack, CACHE_KEY_MAP);
    }

    private static Map<String, DynamicBakedModel> generateModels(ItemStack stack) {
        Map<String, DynamicBakedModel> bakedModelMap = new HashMap<>();
        ItemModule.ModuleInstance root = ItemModule.getModules(stack);
        AtomicReference<Float> scaleAdder = new AtomicReference<>(1.0f);
        for (ItemModule.ModuleInstance moduleI : root.allSubModules()) {
            ModelJson json = modelMap.get(moduleI.module.getName());
            if (json != null) {
                MaterialProperty.Material material = MaterialProperty.getMaterial(moduleI);
                List<String> list = new ArrayList<>();
                if (material != null) {
                    list = material.getTextureKeys();
                } else {
                    list.add("default");
                }
                JsonUnbakedModel unbakedModel = null;
                for (String str : list) {
                    assert json.jsonUnbakedModelMap != null;
                    if (json.jsonUnbakedModelMap.containsKey(str)) {
                        unbakedModel = json.jsonUnbakedModelMap.get(str);
                        break;
                    }
                }
                assert unbakedModel != null;
                scaleAdder.updateAndGet(v -> (v + 0.001f));
                Transform transform = json.transform.copy();
                TransformStack stack1 = SlotProperty.getLocalTransformStack(moduleI);
                stack1.add(transform);
                Miapi.LOGGER.warn(stack1.primary);
                String modelId = stack1.primary;
                if (modelId == null) {
                    modelId = "item";
                }
                transform = Transform.merge(SlotProperty.getTransform(moduleI), transform);
                transform.scale.multiplyComponentwise(scaleAdder.get(), scaleAdder.get(), scaleAdder.get());
                ModelBakeSettings settings = ModelRotation.X0_Y0;
                BakedModel model = bakeModel(unbakedModel, mirroredGetter, transform, ColorUtil.getModuleColor(moduleI), settings);
                DynamicBakedModel dynamicBakedModel = bakedModelMap.computeIfAbsent(modelId, (key) ->
                        new DynamicBakedModel(new ArrayList<>())
                );
                if (model != null) {
                    if (model.getOverrides() == null) {
                        dynamicBakedModel.quads.addAll(model.getQuads(null, null, Random.create()));
                        for (Direction dir : Direction.values()) {
                            dynamicBakedModel.quads.addAll(model.getQuads(null, dir, Random.create()));
                        }
                    } else {
                        dynamicBakedModel.addModel(model);
                    }
                }
            }
        }
        for (ModelTransformer transformer : modelTransformers) {
            bakedModelMap = transformer.transform(bakedModelMap,stack);
        }
        return bakedModelMap;
    }

    public static BakedModel bakeModel(JsonUnbakedModel unbakedModel, Function<SpriteIdentifier, Sprite> textureGetter, Transform transform, int color, ModelBakeSettings settings) {
        try {
            ModelLoader modelLoader = ModelLoadAccessor.getLoader();
            AtomicReference<JsonUnbakedModel> actualModel = new AtomicReference<>(unbakedModel);
            unbakedModel.getModelDependencies().stream().filter(identifier -> identifier.toString().equals("minecraft:item/generated") || identifier.toString().contains("handheld")).findFirst().ifPresent(identifier -> {
                actualModel.set(generator.create(mirroredGetter, unbakedModel));
            });
            //actualModel.get().getModelDependencies()
            actualModel.set(transformModel(actualModel.get(), transform));
            BakedModel model = actualModel.get().bake(modelLoader, unbakedModel.getRootModel(), textureGetter, settings, new Identifier(unbakedModel.id), true);
            return ColorUtil.recolorModel(model, color);
        } catch (Exception e) {
            return null;
        }
    }

    public static JsonUnbakedModel transformModel(JsonUnbakedModel model, Transform transform) {
        // Create a new model with the same properties as the original
        // Apply the transformations to each element in the model
        model.getElements().replaceAll(element -> transformModel(element, transform));

        return model;
    }

    private static ModelElement transformModel(ModelElement element, Transform transform) {
        net.minecraft.client.render.model.json.ModelRotation newRotation;
        if (element.rotation != null) {
            newRotation = new net.minecraft.client.render.model.json.ModelRotation(element.rotation.origin, element.rotation.axis, element.rotation.angle, true);
        } else {
            newRotation = new net.minecraft.client.render.model.json.ModelRotation(new Vec3f(0, 0, 0), Direction.Axis.X, 0, false);
        }


        return new ModelElement(transform.transformVector(element.from), transform.transformVector(element.to), element.faces, newRotation, element.shade);
    }

    public static BakedModel getItemModel(ItemStack stack) {
        return (BakedModel) ModularItemCache.get(stack, CACHE_KEY_ITEM);
    }

    public static JsonUnbakedModel loadModelFromFilePath(String filePath2) {
        if (loadedMap.get(filePath2) != null) {
            return loadedMap.get(filePath2);
        }
        //try to fix path
        if (!filePath2.endsWith(".json")) {
            filePath2 += ".json";
        }
        if (filePath2.contains("item/") && !filePath2.contains("models/")) {
            filePath2 = filePath2.replace("item/", "models/item/");
        }
        if (loadedMap.get(filePath2) != null) {
            return loadedMap.get(filePath2);
        }
        Identifier modelId = new Identifier(filePath2);
        return loadModelFromFilePath(modelId);
    }

    public static JsonUnbakedModel loadModelFromFilePath(Identifier modelId) {
        try {
            ResourceManager resourceManager = MinecraftClient.getInstance().getResourceManager();
            Resource resource = resourceManager.getResource(modelId).orElseThrow(() -> new RuntimeException("could not find model from path " + modelId.toString()));
            BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
            JsonUnbakedModel model = JsonUnbakedModel.deserialize(reader);
            model.getElements().forEach(modelElement -> {
                modelElement.faces.forEach((direction, modelElementFace) -> {
                });
            });
            model.id = modelId.toString();
            loadedMap.put(modelId.toString(), model);
            model.getModelDependencies().forEach(dependendyId -> {
                if (!dependendyId.toString().contains("generated") && !dependendyId.toString().contains("item/handheld")) {
                    try {
                        loadModelsByPath(dependendyId.toString());
                    } catch (Exception surpressed) {
                        Miapi.LOGGER.error("could not find Model " + dependendyId.toString());
                        surpressed.printStackTrace();
                    }
                }
            });
            loadTextureDependencies(model);
            return model;
        } catch (IOException exception) {
            exception.printStackTrace();
            throw new RuntimeException("failure to load model from path " + modelId.toString());
        }
    }

    public static Map<String, JsonUnbakedModel> loadModelsByPath(String filePath) {
        String materialKey = "[material.texture]";
        Map<String, JsonUnbakedModel> models = new HashMap<>();
        if (filePath.contains("[material.texture]")) {
            models.put("default", loadModelFromFilePath(filePath.replace(materialKey, "default")));
            MaterialProperty.getTextureKeys().forEach((path) -> {
                try {
                    models.put(path, loadModelFromFilePath(filePath.replace(materialKey, path)));
                    Miapi.LOGGER.warn("found " + path);
                } catch (RuntimeException ignored) {
                }
            });
        } else {
            models.put("default", loadModelFromFilePath(filePath));
        }
        MaterialProperty.getTextureKeys();
        return models;
    }

    protected static void loadTextureDependencies(JsonUnbakedModel model) {

        List<Identifier> spritesToLoad = new ArrayList<>();
        bakeModel(model, (identifier) -> {
            spritesToLoad.add(identifier.getTextureId());
            return mirroredGetter.apply(identifier);
        }, Transform.IDENTITY, 0, ModelRotation.X0_Y0);
        SpriteLoader.spritesToAdd.addAll(spritesToLoad);
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        Gson gson = new Gson();
        ModelJson propertyJson = gson.fromJson(data.toString(), ModelJson.class);
        propertyJson.repair();
        propertyJson.jsonUnbakedModelMap = loadModelsByPath(propertyJson.path);
        modelMap.put(moduleKey, propertyJson);
        return true;
    }

    public interface ModelTransformer {
        Map<String,DynamicBakedModel> transform(Map<String,DynamicBakedModel> dynamicBakedModelMap, ItemStack stack);
    }

    static class ModelJson {
        @Nullable
        public Map<String, JsonUnbakedModel> jsonUnbakedModelMap;
        public String path;
        public Transform transform = Transform.IDENTITY;

        public void repair() {
            //this shouldn't be necessary as the values should be loaded from the class but anyways
            if (transform == null) {
                transform = Transform.IDENTITY;
            }
        }
    }
}
