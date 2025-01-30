package smartin.miapi.modules.properties.render;

import com.google.common.cache.CacheLoader;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import smartin.miapi.Miapi;
import smartin.miapi.client.model.*;
import smartin.miapi.client.model.item.BakedSingleModel;
import smartin.miapi.client.renderer.TrimRenderer;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.item.modular.Transform;
import smartin.miapi.item.modular.TransformMap;
import smartin.miapi.material.Material;
import smartin.miapi.material.MaterialProperty;
import smartin.miapi.mixin.client.ModelLoaderInterfaceAccessor;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.properties.render.colorproviders.ColorProvider;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeAble;
import smartin.miapi.modules.properties.util.MergeType;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;


@Environment(EnvType.CLIENT)
public class ModelProperty extends CodecProperty<List<ModelProperty.ModelData>> {
    public static ModelProperty property;
    private static final String CACHE_KEY_ITEM = Miapi.MOD_ID + ":itemModelodel";
    public static final Map<String, UnbakedModelHolder> modelCache = new HashMap<>();
    public static final ResourceLocation KEY = Miapi.id("model");
    public static Function<net.minecraft.client.resources.model.Material, TextureAtlasSprite> textureGetter;
    public static Codec<ModelData> DATA_CODEC = ModelData.CODEC;
    public static Codec<List<ModelData>> CODEC = Codec.withAlternative(Codec.list(DATA_CODEC), new Codec<>() {
        @Override
        public <T> DataResult<Pair<List<ModelData>, T>> decode(DynamicOps<T> ops, T input) {
            var result = DATA_CODEC.decode(ops, input);
            if (result.isSuccess()) {
                var success = result.getOrThrow();
                return DataResult.success(new Pair<>(List.of(success.getFirst()), success.getSecond()));
            } else {
                return DataResult.error(() -> "could not decode as single entry");
            }
        }

        @Override
        public <T> DataResult<T> encode(List<ModelData> input, DynamicOps<T> ops, T prefix) {
            return Codec.list(DATA_CODEC).encode(input, ops, prefix);
        }
    });

    public ModelProperty() {
        super(CODEC);
        property = this;
        ModularItemCache.setSupplier(CACHE_KEY_ITEM, (stack) -> getModelMap(stack).get("item"));
        MiapiItemModel.modelSuppliers.add((key, mode, model, stack) -> {
            List<MiapiModel> miapiModels = new ArrayList<>();
            for (ModelHolder holder : getForModule(model, key, stack)) {
                miapiModels.add(new BakedMiapiModel(holder, model, stack));
            }
            return miapiModels;
        });
    }

    public static List<ModelHolder> getForModule(ModuleInstance instance, String key, ItemStack itemStack) {
        List<ModelData> modelDataList = property.getData(instance).orElse(new ArrayList<>());
        List<ModelHolder> models = new ArrayList<>();
        for (ModelData json : modelDataList) {
            ModelHolder holder = bakedModel(instance, json, itemStack, key);
            if (holder != null) {
                models.add(holder);
            }
        }
        return models;
    }

    @Nullable
    public static ModelHolder bakedModel(ModuleInstance instance, ModelData json, ItemStack itemStack, String key) {
        int condition = Material.getColor(StatResolver.resolveString(json.condition, instance));
        if (condition != 0) {
            if (
                    json.transform.origin == null && "item".equals(key) ||
                    json.transform.origin != null && json.transform.origin.equals(key) ||
                    ("item".equals(json.transform.origin) && key == null)) {
                return bakedModel(instance, json, itemStack);
            }
        }
        return null;
    }

    @Nullable
    public static ModelHolder bakedModel(ModuleInstance instance, ModelData json, ItemStack itemStack) {
        Material material = MaterialProperty.getMaterial(instance);
        json.repair();
        List<String> list = new ArrayList<>();
        if (material != null) {
            list.add(material.getStringID());
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
        if (unbakedModel == null) {
            String fullPath = json.path.replace("[material.texture]", "default");
            if (modelCache.containsKey(fullPath)) {
                unbakedModel = modelCache.get(fullPath);
            } else {
                return null;
            }
        }
        BakedSingleModel model = DynamicBakery.bakeModel(unbakedModel.model, textureGetter, FastColor.ARGB32.color(255, 255, 255, 255), Transform.IDENTITY);
        if (model != null) {
            Matrix4f matrix4f = Transform.toModelTransformation(json.transform).toMatrix();
            String colorProviderId = unbakedModel.modelMetadata.colorProvider != null ?
                    unbakedModel.modelMetadata.colorProvider : json.color_provider;
            ColorProvider colorProvider = ColorProvider.getProvider(colorProviderId, itemStack, instance);
            if (colorProvider == null) {
                throw new RuntimeException("colorProvider is null");
            }
            return new ModelHolder(
                    model.optimize(), matrix4f, colorProvider,
                    unbakedModel.modelMetadata.lightValues == null ? new int[]{-1, -1} : unbakedModel.modelMetadata.lightValues,
                    json.getTrimMode(), json.entity_render);
        }
        return null;
    }

    public static boolean isAllowedKey(@Nullable String jsonKey, @Nullable String modelTypeKey) {
        return jsonKey == null && modelTypeKey == null ||
               jsonKey != null && jsonKey.equals(modelTypeKey) ||
               jsonKey != null && modelTypeKey == null && jsonKey.equals("default") ||
               jsonKey == null && modelTypeKey != null && modelTypeKey.equals("default") ||
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

    protected static BlockModel loadModelFromFilePath(String filePath2) throws FileNotFoundException {
        if (modelCache.containsKey(filePath2)) {
            return modelCache.get(filePath2).model;
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
        UnbakedModelHolder holder = new UnbakedModelHolder(model, fromPath(ModelBakery.MODEL_LISTER.idToFile(modelId)));
        modelCache.put(filePath2, holder);
        modelCache.put(modelId.toString(), holder);
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

    public static ModelMetadata fromPath(ResourceLocation identifier) {
        try {
            Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(identifier);
            if (resource.isPresent()) {
                return resource.get().metadata().getSection(new ModelDecoder()).orElse(ModelDecoder.EMPTY());
            }
        } catch (IOException ignored) {
        }
        return ModelDecoder.EMPTY();
    }

    protected static void loadModelsByPath(String filePath) {
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

    protected static void loadTextureDependencies(BlockModel model) {
        DynamicBakery.bakeModel(model, (identifier) -> textureGetter.apply(identifier), 0, Transform.IDENTITY);
    }

    @Override
    public boolean load(ResourceLocation moduleKey, JsonElement data, boolean isClient) throws Exception {
        decode(data).forEach(modelData -> {
            modelData.repair();
            loadModelsByPath(modelData.path);
        });
        return true;
    }

    @Override
    public List<ModelData> merge(List<ModelData> left, List<ModelData> right, MergeType mergeType) {
        return MergeAble.mergeList(left, right, mergeType);
    }

    public record TransformedUnbakedModel(TransformMap transform, BlockModel unbakedModel,
                                          ModuleInstance instance, int color) {
    }

    public static class ModelData {
        public String path;
        public Transform transform = Transform.IDENTITY;
        public String condition = "1";
        public String color_provider = "material";
        public String trim_mode = "none";
        public Boolean entity_render = false;
        public String id = null;

        public static final Codec<ModelData> CODEC = RecordCodecBuilder.create((instance) ->
                instance.group(
                        Codec.STRING.fieldOf("path")
                                .forGetter((modelData) -> modelData.path),
                        Transform.CODEC.optionalFieldOf("transform", Transform.IDENTITY)
                                .forGetter((modelData) -> modelData.transform),
                        Codec.STRING.optionalFieldOf("condition", "1")
                                .forGetter((modelData) -> modelData.condition),
                        Codec.STRING.optionalFieldOf("color_provider", "material")
                                .forGetter((modelData) -> modelData.color_provider),
                        Codec.STRING.optionalFieldOf("trim_mode", "none")
                                .forGetter((modelData) -> modelData.trim_mode),
                        Miapi.FIXED_BOOL_CODEC.optionalFieldOf("entity_render")
                                .forGetter((modelData) -> Optional.of(modelData.entity_render)),
                        Codec.STRING.optionalFieldOf("id", "")
                                .forGetter((modelData) -> modelData.id)
                ).apply(instance, ModelData::new)
        );

        // Constructor to work with RecordCodecBuilder
        public ModelData(String path, Transform transform, String condition, String color_provider,
                         String trim_mode, Optional<Boolean> entity_render, String id) {
            this.path = path;
            this.transform = transform;
            this.condition = condition;
            this.color_provider = color_provider;
            this.trim_mode = trim_mode;
            this.entity_render = entity_render.orElseGet(() -> !(
                    this.getTrimMode().equals(TrimRenderer.TrimMode.NONE) &&
                    this.getTrimMode().equals(TrimRenderer.TrimMode.ITEM)));
            this.id = id;
            repair();
        }

        public void repair() {
            //this shouldn't be necessary as the values should be loaded from the class but anyways
            if (transform == null) {
                transform = Transform.IDENTITY;
            }
            transform = Transform.repair(transform);
        }

        public TrimRenderer.TrimMode getTrimMode() {
            if (trim_mode == null) {
                return TrimRenderer.TrimMode.NONE;
            } else {
                return switch (trim_mode.toLowerCase()) {
                    case "armor_layer_one" -> TrimRenderer.TrimMode.ARMOR_LAYER_ONE;
                    case "armor_layer_two" -> TrimRenderer.TrimMode.ARMOR_LAYER_TWO;
                    case "item" -> TrimRenderer.TrimMode.ITEM;
                    default -> TrimRenderer.TrimMode.NONE;
                };
            }
        }
    }

    static class ModelDecoder implements MetadataSectionSerializer<ModelMetadata> {

        public static ModelMetadata EMPTY() {
            return new ModelMetadata(null, null);
        }

        @Override
        public @NotNull String getMetadataSectionName() {
            return "miapi_model_data";
        }

        @Override
        public @NotNull ModelMetadata fromJson(JsonObject json) {
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
                EmissivityProperty.LightJson lightJson = EmissivityProperty.property.decode(json.get("lightValues"));
                light = lightJson.asArray();
            }
            return new ModelMetadata(data, light);
        }
    }

    public record ModelMetadata(String colorProvider, int[] lightValues) {
    }

    public record UnbakedModelHolder(BlockModel model, ModelMetadata modelMetadata) {
    }
}
