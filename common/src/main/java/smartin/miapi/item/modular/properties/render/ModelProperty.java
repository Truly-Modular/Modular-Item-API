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
    public static final String key = "texture";
    public static Function<SpriteIdentifier, Sprite> textureGetter;
    private static Function<SpriteIdentifier, Sprite> mirroredGetter;
    private static final String cacheKey = Miapi.MOD_ID + ":model";
    private static final Map<String, ModelJson> modelMap = new HashMap<>();
    private static ItemModelGenerator generator;
    private static Map<String,JsonUnbakedModel> loadedMap = new HashMap<>();
    public static final List<ModelTransformer> modelTransformers = new ArrayList<>();

    public ModelProperty() {
        mirroredGetter = (identifier) -> {
            return textureGetter.apply(identifier);
        };
        generator = new ItemModelGenerator();
        ModularItemCache.setSupplier(cacheKey, (stack) -> {
            return generateModel(stack);
        });
    }


    private static BakedModel generateModel(ItemStack stack) {
        DynamicBakedModel dynamicBakedModel = new DynamicBakedModel(new ArrayList<>());
        ItemModule.ModuleInstance root = ItemModule.getModules(stack);
        AtomicReference<Float> scaleAdder = new AtomicReference<>(1.0f);
        for(ItemModule.ModuleInstance moduleI : root.allSubModules()){
            ModelJson json = modelMap.get(moduleI.module.getName());
            if (json != null) {
                MaterialProperty.Material material = MaterialProperty.getMaterial(moduleI);
                List<String> list = new ArrayList<>();
                list.add("default");
                if (material != null) {
                    list = material.getTextureKeys();
                }
                JsonUnbakedModel unbakedModel = null;
                for (String str : list) {
                    if (json.jsonUnbakedModelMap.containsKey(str)) {
                        unbakedModel = json.jsonUnbakedModelMap.get(str);
                        break;
                    }
                }
                scaleAdder.updateAndGet(v -> (v + 0.001f));
                Transform transform = json.transform.copy();
                transform = Transform.merge(SlotProperty.getTransform(moduleI), transform);
                transform.scale.multiplyComponentwise(scaleAdder.get(), scaleAdder.get(), scaleAdder.get());
                ModelBakeSettings settings = ModelRotation.X0_Y0;
                BakedModel model = bakeModel(unbakedModel, mirroredGetter, transform, ColorUtil.getModuleColor(moduleI), settings);
                if(model.getOverrides()==null){
                    dynamicBakedModel.quads.addAll(model.getQuads(null, null, Random.create()));
                    for (Direction dir : Direction.values()) {
                        dynamicBakedModel.quads.addAll(model.getQuads(null, dir, Random.create()));
                    }
                }
                else{
                    dynamicBakedModel.addModel(model);
                }
            }
        }
        for (ModelTransformer transformer : modelTransformers) {
            dynamicBakedModel = transformer.transform(dynamicBakedModel, stack);
        }
        return dynamicBakedModel;
    }

    public static BakedModel bakeModel(JsonUnbakedModel unbakedModel, Function<SpriteIdentifier, Sprite> textureGetter, Transform transform, int color, ModelBakeSettings settings) {
        try{
            ModelLoader modelLoader = ModelLoadAccessor.getLoader();
            if(true){
                //return modelLoader.bake(new Identifier("minecraft","item/bow"),settings);
            }
            AtomicReference<JsonUnbakedModel> actualModel = new AtomicReference<>(unbakedModel);
            unbakedModel.getModelDependencies().stream().filter(identifier -> identifier.toString().equals("minecraft:item/generated") || identifier.toString().contains("handheld")).findFirst().ifPresent(identifier -> {
                actualModel.set(generator.create(mirroredGetter, unbakedModel));
            });
            //actualModel.get().getModelDependencies()
            actualModel.set(transformModel(actualModel.get(), transform));
            BakedModel model = actualModel.get().bake(modelLoader, unbakedModel.getRootModel(), textureGetter, settings, new Identifier(unbakedModel.id), true);
            return ColorUtil.recolorModel(model, color);
        }catch (Exception e){
            return null;
        }
    }

    public static JsonUnbakedModel transformModel(JsonUnbakedModel model, Transform transform) {
        // Create a new model with the same properties as the original
        // Apply the transformations to each element in the model
        for (int i = 0; i < model.getElements().size(); i++) {
            model.getElements().set(i, transformModel(model.getElements().get(i), transform));
        }

        return model;
    }

    private static ModelElement transformModel(ModelElement element, Transform transform) {
        net.minecraft.client.render.model.json.ModelRotation newRotation;
        if (element.rotation != null) {
            newRotation = new net.minecraft.client.render.model.json.ModelRotation(element.rotation.origin, element.rotation.axis, element.rotation.angle, true);
        } else {
            newRotation = new net.minecraft.client.render.model.json.ModelRotation(new Vec3f(0, 0, 0), Direction.Axis.X, 0, false);
        }


        ModelElement newElement = new ModelElement(transform.transformVector(element.from), transform.transformVector(element.to), element.faces, newRotation, element.shade);

        return newElement;
    }

    public static BakedModel getModel(ItemStack stack) {
        return (BakedModel) ModularItemCache.get(stack, cacheKey);
    }

    public static JsonUnbakedModel loadModelFromFilePath(String filePath2) {
        if(loadedMap.get(filePath2)!=null){
            return loadedMap.get(filePath2);
        }
        //try to fix path
        if(!filePath2.endsWith(".json")){
            filePath2+=".json";
        }
        if(filePath2.contains("item/") && !filePath2.contains("models/")){
            filePath2 = filePath2.replace("item/","models/item/");
        }
        if(loadedMap.get(filePath2)!=null){
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
            loadedMap.put(modelId.toString(),model);
            model.getModelDependencies().forEach(dependendyId->{
                if(!dependendyId.toString().contains("generated") && !dependendyId.toString().contains("item/handheld")){
                    try{
                        loadModelsbyPath(dependendyId.toString());
                    }catch (Exception surpressed){
                        Miapi.LOGGER.error("could not find Model "+dependendyId.toString());
                        surpressed.printStackTrace();
                    }
                }
            });
            loadTextureDependencies(model);
            return model;
        } catch (IOException exception) {
            exception.printStackTrace();
            new RuntimeException("failure to load model from path " + modelId.toString());
        }
        return null;
    }

    public static Map<String, JsonUnbakedModel> loadModelsbyPath(String filePath) {
        String materialKey = "[material.texture]";
        Map<String, JsonUnbakedModel> models = new HashMap<>();
        if (filePath.contains("[material.texture]")) {
            String currentPath = filePath;
            models.put("default", loadModelFromFilePath(currentPath.replace(materialKey, "default")));
            try {
                MaterialProperty.getTextureKeys().forEach((path) -> {
                    models.put("default", loadModelFromFilePath(currentPath.replace(materialKey, path)));
                });
            } catch (RuntimeException surpressed) {

            }
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
        propertyJson.jsonUnbakedModelMap = loadModelsbyPath(propertyJson.path);
        if (propertyJson.jsonUnbakedModelMap == null) {
            throw new Exception("could not find model path");
        }
        modelMap.put(moduleKey, propertyJson);
        return true;
    }

    public interface ModelTransformer {
        DynamicBakedModel transform(DynamicBakedModel dynamicBakedModel, ItemStack stack);
    }

    class ModelJson {
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
