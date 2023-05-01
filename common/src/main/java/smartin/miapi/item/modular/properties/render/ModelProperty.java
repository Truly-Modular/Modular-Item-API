package smartin.miapi.item.modular.properties.render;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.ModelRotation;
import net.minecraft.client.render.model.json.ItemModelGenerator;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
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
    private static final Map<String, List<ModelJson>> modelMap = new HashMap<>();
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

    private static Map<String, DynamicBakedModel> generateModels(ItemStack itemStack) {
        ItemModule.ModuleInstance root = ItemModule.getModules(itemStack);
        AtomicReference<Float> scaleAdder = new AtomicReference<>(1.0f);
        List<TransformedUnbakedModel> unbakedModels = new ArrayList<>();
        for (ItemModule.ModuleInstance moduleI : root.allSubModules()) {
            List<ModelJson> modelJsonList = modelMap.get(moduleI.module.getName());
            for (ModelJson json : modelJsonList) {
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
                    scaleAdder.updateAndGet(v -> (v + 0.005f));
                    TransformStack transformStack = SlotProperty.getTransformStack(moduleI);
                    transformStack.add(json.transform.copy());
                    String modelId = transformStack.primary;
                    Transform transform1 = transformStack.get(transformStack.primary);
                    if (modelId == null) {
                        modelId = "item";
                    }
                    transformStack.primary = modelId;
                    transform1.scale.add(new Vec3f(scaleAdder.get()-1,scaleAdder.get()-1,scaleAdder.get()-1));
                    transform1.translation.add(new Vec3f(-scaleAdder.get()/3+1,-scaleAdder.get()/3+1,-scaleAdder.get()/3+1));
                    transformStack.set(transformStack.primary, transform1);
                    unbakedModels.add(new TransformedUnbakedModel(transformStack, unbakedModel, moduleI));
                }
            }
        }
        for (ModelTransformer transformer : modelTransformers) {
            unbakedModels = transformer.unBakedTransform(unbakedModels, itemStack);
        }
        Map<String, DynamicBakedModel> bakedModelMap = new HashMap<>();

        for (TransformedUnbakedModel unbakedModel : unbakedModels) {
            ModelBakeSettings settings = unbakedModel.transform.get().toModelBakeSettings();
            BakedModel model = bakeModel(unbakedModel.unbakedModel, mirroredGetter, ColorUtil.getModuleColor(unbakedModel.instance), settings);
            DynamicBakedModel dynamicBakedModel = bakedModelMap.computeIfAbsent(unbakedModel.transform.primary, (key) ->
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

        for (ModelTransformer transformer : modelTransformers) {
            bakedModelMap = transformer.bakedTransform(bakedModelMap, itemStack);
        }
        return bakedModelMap;
    }

    public static BakedModel bakeModel(JsonUnbakedModel unbakedModel, Function<SpriteIdentifier, Sprite> textureGetter, int color, ModelBakeSettings settings) {
        try {
            ModelLoader modelLoader = ModelLoadAccessor.getLoader();
            AtomicReference<JsonUnbakedModel> actualModel = new AtomicReference<>(unbakedModel);
            unbakedModel.getModelDependencies().stream().filter(identifier -> identifier.toString().equals("minecraft:item/generated") || identifier.toString().contains("handheld")).findFirst().ifPresent(identifier -> {
                actualModel.set(generator.create(mirroredGetter, unbakedModel));
            });
            BakedModel model = actualModel.get().bake(modelLoader, unbakedModel.getRootModel(), textureGetter, settings, new Identifier(unbakedModel.id), true);
            return ColorUtil.recolorModel(model, color);
        } catch (Exception e) {
            return null;
        }
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
        }, 0, ModelRotation.X0_Y0);
        SpriteLoader.spritesToAdd.addAll(spritesToLoad);
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        Gson gson = Miapi.gson;
        List<ModelJson> jsonList = new ArrayList<>();
        if (data.isJsonArray()) {
            JsonArray dataArray = data.getAsJsonArray();
            for (JsonElement element : dataArray) {
                ModelJson propertyJson = gson.fromJson(element.toString(), ModelJson.class);
                propertyJson.repair();
                propertyJson.jsonUnbakedModelMap = loadModelsByPath(propertyJson.path);
                jsonList.add(propertyJson);
            }
            modelMap.put(moduleKey, jsonList);
        } else {
            ModelJson propertyJson = gson.fromJson(data.toString(), ModelJson.class);
            propertyJson.repair();
            propertyJson.jsonUnbakedModelMap = loadModelsByPath(propertyJson.path);
            jsonList.add(propertyJson);
        }
        modelMap.put(moduleKey, jsonList);
        return true;
    }

    public interface ModelTransformer {
        default Map<String, DynamicBakedModel> bakedTransform(Map<String, DynamicBakedModel> dynamicBakedModelMap, ItemStack stack) {
            return dynamicBakedModelMap;
        }

        default List<TransformedUnbakedModel> unBakedTransform(List<TransformedUnbakedModel> list, ItemStack itemStack) {
            return list;
        }
    }

    public record TransformedUnbakedModel(TransformStack transform, JsonUnbakedModel unbakedModel,
                                          ItemModule.ModuleInstance instance) {
    }

    static class ModelJson {
        @Nullable
        public Map<String, JsonUnbakedModel> jsonUnbakedModelMap;
        public String path;
        public Transform transform = Transform.IDENTITY;
        public String condition = "1";
        public String color = "0";

        public void repair() {
            //this shouldn't be necessary as the values should be loaded from the class but anyways
            if (transform == null) {
                transform = Transform.IDENTITY;
            }
        }
    }
}
