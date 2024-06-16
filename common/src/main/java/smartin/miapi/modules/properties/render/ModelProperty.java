package smartin.miapi.modules.properties.render;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.json.ItemModelGenerator;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.Resource;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import smartin.miapi.Miapi;
import smartin.miapi.client.model.*;
import smartin.miapi.client.model.item.BakedSingleModel;
import smartin.miapi.client.renderer.TrimRenderer;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.item.modular.Transform;
import smartin.miapi.item.modular.TransformMap;
import smartin.miapi.mixin.client.ModelLoaderInterfaceAccessor;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.material.Material;
import smartin.miapi.modules.material.MaterialProperty;
import smartin.miapi.modules.properties.EmissiveProperty;
import smartin.miapi.modules.properties.SlotProperty;
import smartin.miapi.modules.properties.render.colorproviders.ColorProvider;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static smartin.miapi.Miapi.gson;


@Environment(EnvType.CLIENT)
public class ModelProperty implements RenderProperty {
    public static ModuleProperty property;
    private static final String CACHE_KEY_MAP = Miapi.MOD_ID + ":modelMap";
    private static final String CACHE_KEY_ITEM = Miapi.MOD_ID + ":itemModelodel";
    public static final Map<String, UnbakedModelHolder> modelCache = new HashMap<>();
    public static final String KEY = "texture";
    public static final List<ModelTransformer> modelTransformers = new ArrayList<>();
    public static Function<SpriteIdentifier, Sprite> textureGetter;
    private static Function<SpriteIdentifier, Sprite> mirroredGetter;
    private static ItemModelGenerator generator;

    public ModelProperty() {
        property = this;
        mirroredGetter = (identifier) -> textureGetter.apply(identifier);
        generator = new ItemModelGenerator();
        ModularItemCache.setSupplier(CACHE_KEY_ITEM, (stack) -> getModelMap(stack).get("item"));
        ModularItemCache.setSupplier(CACHE_KEY_MAP, ModelProperty::generateModels);
        MiapiItemModel.modelSuppliers.add((key, model, stack) -> {
            List<MiapiModel> miapiModels = new ArrayList<>();
            for (ModelHolder holder : getForModule(model, key, stack)) {
                miapiModels.add(new BakedMiapiModel(holder, model, stack));
            }
            return miapiModels;
        });
    }

    public static List<ModelJson> getJson(ModuleInstance moduleInstance) {
        List<ModelJson> modelJsonList = new ArrayList<>();
        JsonElement data = moduleInstance.getProperties().get(property);
        if (data == null) {
            return new ArrayList<>();
        }
        if (data.isJsonArray()) {
            JsonArray dataArray = data.getAsJsonArray();
            for (JsonElement element : dataArray) {
                ModelJson propertyJson = gson.fromJson(element.toString(), ModelJson.class);
                propertyJson.repair();
                modelJsonList.add(propertyJson);
            }
        } else {
            ModelJson propertyJson = gson.fromJson(data.toString(), ModelJson.class);
            propertyJson.repair();
            modelJsonList.add(propertyJson);
        }
        return modelJsonList;
    }

    public static List<ModelHolder> getForModule(ModuleInstance instance, String key, ItemStack itemStack) {
        List<ModelJson> modelJsonList = getJson(instance);
        List<ModelHolder> models = new ArrayList<>();
        for (ModelJson json : modelJsonList) {
            ModelHolder holder = bakedModel(instance,json,itemStack,key);
            if(holder!=null){
                models.add(holder);
            }
        }
        return models;
    }

    @Nullable
    public static ModelHolder bakedModel(ModuleInstance instance, ModelJson json, ItemStack itemStack, String key) {
        int condition = Material.getColor(StatResolver.resolveString(json.condition, instance));
        if(condition!=0){
            if (
                    json.transform.origin == null && key == null ||
                            json.transform.origin != null && json.transform.origin.equals(key) ||
                            ("item".equals(json.transform.origin) && key == null)) {
                return bakedModel(instance, json, itemStack);
            }
        }
        return null;
    }

    @Nullable
    public static ModelHolder bakedModel(ModuleInstance instance, ModelJson json, ItemStack itemStack) {
        Material material = MaterialProperty.getMaterial(instance);
        List<String> list = new ArrayList<>();
        if (material != null) {
            list.add(material.getKey());
            list = material.getTextureKeys();
        } else {
            list.add("default");
        }
        UnbakedModelHolder unbakedModel = null;
        for (String str : list) {
            String fullPath = json.path.replace("[material.texture]", str);
            if (modelCache.containsKey(fullPath)) {
                unbakedModel = modelCache.get(fullPath);
                break;
            }
        }
        BakedSingleModel model = DynamicBakery.bakeModel(unbakedModel.model, textureGetter, ColorHelper.Argb.getArgb(255, 255, 255, 255), Transform.IDENTITY);
        if (model != null) {
            Matrix4f matrix4f = Transform.toModelTransformation(json.transform).toMatrix();
            String colorProviderId = unbakedModel.modelData.colorProvider != null ?
                    unbakedModel.modelData.colorProvider : json.color_provider;
            ColorProvider colorProvider = ColorProvider.getProvider(colorProviderId, itemStack, instance);
            if (colorProvider == null) {
                throw new RuntimeException("colorProvider is null");
            }
            return new ModelHolder(model.optimize(), matrix4f, colorProvider, unbakedModel.modelData.lightValues, json.getTrimMode(), json.entity_render);
        }
        return null;
    }

    public static boolean isAllowedKey(@Nullable String jsonKey, @Nullable String modelTypeKey) {
        return jsonKey == null && modelTypeKey == null ||
                jsonKey != null && jsonKey.equals(modelTypeKey) ||
                ("item".equals(jsonKey) && modelTypeKey == null);
    }

    public static Map<String, BakedModel> getModelMap(ItemStack stack) {
        //return (Map<String, BakedModel>) ModularItemCache.getRaw(stack, CACHE_KEY_MAP);
        return new HashMap<>();
    }

    @Nullable
    public static BakedModel getItemModel(ItemStack stack) {
        return ModularItemCache.getRaw(stack, CACHE_KEY_ITEM);
    }

    protected static Map<String, BakedModel> generateModels(ItemStack itemStack) {
        ModuleInstance root = ItemModule.getModules(itemStack);

        List<TransformedUnbakedModel> unbakedModels = resolveUnbakedModel(root);

        for (ModelTransformer transformer : modelTransformers) {
            unbakedModels = transformer.unBakedTransform(unbakedModels, itemStack);
        }
        Map<String, BakedSingleModel> bakedModelMap = bakedModelMap(unbakedModels);

        for (ModelTransformer transformer : modelTransformers) {
            bakedModelMap = transformer.bakedTransform(bakedModelMap, itemStack);
        }
        Map<String, BakedModel> optimizedMap = optimize(bakedModelMap);
        return optimizedMap;
    }

    protected static Map<String, BakedModel> optimize(Map<String, BakedSingleModel> bakedModelMap) {
        HashMap<String, BakedModel> map = new HashMap<>();
        bakedModelMap.forEach((id, dynamicModel) -> {
            map.put(id, dynamicModel.optimize());
        });
        return map;
    }

    protected static Map<String, BakedSingleModel> bakedModelMap(List<TransformedUnbakedModel> unbakedModels) {
        Map<String, BakedSingleModel> bakedModelMap = new HashMap<>();
        for (TransformedUnbakedModel unbakedModel : unbakedModels) {
            ModelBakeSettings settings = unbakedModel.transform.get().toModelBakeSettings();
            BakedSingleModel model = DynamicBakery.bakeModel(unbakedModel.unbakedModel, mirroredGetter, unbakedModel.color, unbakedModel.transform.get());
            BakedSingleModel bakedSIngleModel = bakedModelMap.computeIfAbsent(unbakedModel.transform.primary, (key) ->
                    new BakedSingleModel(new ArrayList<>())
            );
            if (model != null) {
                if (model.getOverrides() == null || model.getOverrides().equals(ModelOverrideList.EMPTY)) {
                    bakedSIngleModel.quads.addAll(model.getQuads(null, null, Random.create()));
                    for (Direction dir : Direction.values()) {
                        bakedSIngleModel.quads.addAll(model.getQuads(null, dir, Random.create()));
                    }
                } else {
                    bakedSIngleModel.addModel(model);
                }
            } else {
                Miapi.LOGGER.warn("Model is null? - this probably indicates another issue");
            }
        }
        return bakedModelMap;
    }

    protected static List<TransformedUnbakedModel> resolveUnbakedModel(ModuleInstance root) {
        List<TransformedUnbakedModel> unbakedModels = new ArrayList<>();
        AtomicReference<Float> scaleAdder = new AtomicReference<>(1.0f);
        for (ModuleInstance moduleI : root.allSubModules()) {
            Gson gson = Miapi.gson;
            List<ModelJson> modelJsonList = new ArrayList<>();
            JsonElement data = moduleI.getProperties().get(property);
            if (data == null) {
                return unbakedModels;
            }
            if (data.isJsonArray()) {
                JsonArray dataArray = data.getAsJsonArray();
                for (JsonElement element : dataArray) {
                    ModelJson propertyJson = gson.fromJson(element.toString(), ModelJson.class);
                    propertyJson.repair();
                    modelJsonList.add(propertyJson);
                }
            } else {
                ModelJson propertyJson = gson.fromJson(data.toString(), ModelJson.class);
                propertyJson.repair();
                modelJsonList.add(propertyJson);
            }
            if (modelJsonList == null) {
                Miapi.LOGGER.warn("Module " + moduleI.module.name() + " has no Model Attached, is this intentional?");
                return new ArrayList<>();
            }
            for (ModelJson json : modelJsonList) {
                int color = 0;
                int condition = Material.getColor(StatResolver.resolveString(json.condition, moduleI));
                if (condition != 0) {
                    Material material = MaterialProperty.getMaterial(moduleI);
                    List<String> list = new ArrayList<>();
                    if (material != null) {
                        list.add(material.getKey());
                        list = material.getTextureKeys();
                    } else {
                        list.add("default");
                    }
                    JsonUnbakedModel unbakedModel = null;
                    for (String str : list) {
                        String fullPath = json.path.replace("[material.texture]", str);
                        if (modelCache.containsKey(fullPath)) {
                            unbakedModel = modelCache.get(fullPath).model;
                        }
                    }
                    assert unbakedModel != null;
                    TransformMap transformMap = SlotProperty.getTransformStack(moduleI);
                    if (json.transform == null) {
                        json.transform = Transform.IDENTITY;
                    }
                    transformMap.add(json.transform.copy());
                    String modelId = transformMap.primary;
                    Transform transform1 = transformMap.get(transformMap.primary);
                    if (modelId == null) {
                        modelId = "item";
                    }
                    transformMap.primary = modelId;
                    transform1.scale.mul(scaleAdder.get());
                    transformMap.set(transformMap.primary, transform1);
                    unbakedModels.add(new TransformedUnbakedModel(transformMap, unbakedModel, moduleI, color));
                }
            }
        }
        return unbakedModels;
    }

    protected static JsonUnbakedModel loadModelFromFilePath(String filePath2) throws FileNotFoundException {
        if (modelCache.containsKey(filePath2)) {
            return modelCache.get(filePath2).model;
        }
        if (!filePath2.endsWith(".json")) {
            filePath2 += ".json";
        }
        if (filePath2.contains("item/") && !filePath2.contains("models/")) {
            filePath2 = filePath2.replace("item/", "models/item/");
        }
        ModelLoader loader = ModelLoadAccessor.getLoader();
        filePath2 = filePath2.replace(".json", "");
        filePath2 = filePath2.replace("models/", "");
        Identifier modelId = new Identifier(filePath2);
        JsonUnbakedModel model = ((ModelLoaderInterfaceAccessor) loader).loadModelFromPath(modelId);
        if (!filePath2.endsWith(".json")) {
            filePath2 += ".json";
        }
        if (filePath2.contains("item/") && !filePath2.contains("models/")) {
            filePath2 = filePath2.replace("item/", "models/item/");
        }
        UnbakedModelHolder holder = new UnbakedModelHolder(model, fromPath(ModelLoader.MODELS_FINDER.toResourcePath(modelId)));
        modelCache.put(filePath2, holder);
        modelCache.put(modelId.toString(), holder);
        model.getOverrides().forEach(modelOverride -> {
            try {
                loadModelFromFilePath(modelOverride.getModelId().toString());
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
        loadTextureDependencies(model);
        return model;
    }

    public static ModelData fromPath(Identifier identifier) {
        try {
            Optional<Resource> resource = MinecraftClient.getInstance().getResourceManager().getResource(identifier);
            if (resource.isPresent()) {
                return resource.get().getMetadata().decode(new ModelDecoder()).orElse(ModelDecoder.EMPTY());
            }
        } catch (IOException ignored) {
        }
        return ModelDecoder.EMPTY();
    }

    protected static Map<String, JsonUnbakedModel> loadModelsByPath(String filePath) {
        String materialKey = "[material.texture]";
        Map<String, JsonUnbakedModel> models = new HashMap<>();
        if (filePath.contains(materialKey)) {
            try {
                String path = filePath.replace(materialKey, "default");
                JsonUnbakedModel model = loadModelFromFilePath(path);
                models.put("default", model);
            } catch (FileNotFoundException fileNotFoundException) {
                throw new RuntimeException(fileNotFoundException);
            }
            MaterialProperty.getTextureKeys().forEach((path) -> {
                try {
                    String fullPath = filePath.replace(materialKey, path);
                    JsonUnbakedModel model = loadModelFromFilePath(fullPath);
                    if (model != null) {
                        models.put(path, model);
                    }
                } catch (FileNotFoundException ignored) {
                }
            });
        } else {
            try {
                JsonUnbakedModel model = loadModelFromFilePath(filePath);
                models.put("default", model);
            } catch (FileNotFoundException fileNotFoundException) {
                throw new RuntimeException(fileNotFoundException);
            }
        }
        return models;
    }

    protected static void loadTextureDependencies(JsonUnbakedModel model) {
        DynamicBakery.bakeModel(model, (identifier) -> mirroredGetter.apply(identifier), 0, Transform.IDENTITY);
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
                loadModelsByPath(propertyJson.path);
                jsonList.add(propertyJson);
            }
        } else {
            ModelJson propertyJson = gson.fromJson(data.toString(), ModelJson.class);
            propertyJson.repair();
            loadModelsByPath(propertyJson.path);
            jsonList.add(propertyJson);
        }
        return true;
    }

    public interface ModelTransformer {
        default Map<String, BakedSingleModel> bakedTransform(Map<String, BakedSingleModel> dynamicBakedModelMap, ItemStack stack) {
            return dynamicBakedModelMap;
        }

        default List<TransformedUnbakedModel> unBakedTransform(List<TransformedUnbakedModel> list, ItemStack itemStack) {
            return list;
        }
    }

    public record TransformedUnbakedModel(TransformMap transform, JsonUnbakedModel unbakedModel,
                                          ModuleInstance instance, int color) {
    }

    public static class ModelJson {
        public String path;
        public Transform transform = Transform.IDENTITY;
        public String condition = "1";
        public String color_provider = "material";
        public String trim_mode;
        public Boolean entity_render;
        public String id = null;

        public void repair() {
            //this shouldn't be necessary as the values should be loaded from the class but anyways
            if (transform == null) {
                transform = Transform.IDENTITY;
            }
            transform = Transform.repair(transform);
            if (entity_render == null) {
                entity_render = !this.getTrimMode().equals(TrimRenderer.TrimMode.NONE);
            }
        }

        public TrimRenderer.TrimMode getTrimMode() {
            if (trim_mode == null) {
                return TrimRenderer.TrimMode.NONE;
            } else {
                switch (trim_mode.toLowerCase()) {
                    case "armor_layer_one": {
                        return TrimRenderer.TrimMode.ARMOR_LAYER_ONE;
                    }
                    case "armor_layer_two": {
                        return TrimRenderer.TrimMode.ARMOR_LAYER_TWO;
                    }
                    case "item": {
                        return TrimRenderer.TrimMode.ITEM;
                    }
                    default: {
                        return TrimRenderer.TrimMode.NONE;
                    }
                }
            }
        }
    }

    static class ModelDecoder implements ResourceMetadataReader<ModelData> {

        public static ModelData EMPTY() {
            return new ModelData(null, null);
        }

        @Override
        public String getKey() {
            return "miapi_model_data";
        }

        @Override
        public ModelData fromJson(JsonObject json) {
            String data = null;
            int[] light = null;
            if (json.has("modelProvider")) {
                data = json.get("modelProvider").getAsString();
                if (!ColorProvider.colorProviders.containsKey(data)) {
                    Miapi.LOGGER.error("Color Provider " + data + " does not exist");
                    data = null;
                }
            }
            if (json.has("lightValues")) {
                light = EmissiveProperty.getLightValues(json.get("lightValues"));
            }
            return new ModelData(data, light);
        }
    }

    public record ModelData(String colorProvider, int[] lightValues) {
    }

    public record UnbakedModelHolder(JsonUnbakedModel model, ModelData modelData) {
    }
}
