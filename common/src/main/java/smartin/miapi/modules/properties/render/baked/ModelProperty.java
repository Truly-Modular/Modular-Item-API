package smartin.miapi.modules.properties.render.baked;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import smartin.miapi.Miapi;
import smartin.miapi.client.model.DynamicBakery;
import smartin.miapi.client.model.MiapiItemModel;
import smartin.miapi.client.model.MiapiModel;
import smartin.miapi.client.model.ModelHolder;
import smartin.miapi.client.model.item.BakedSingleModel;
import smartin.miapi.client.model.module.BakedMiapiModel;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.item.modular.Transform;
import smartin.miapi.material.MaterialProperty;
import smartin.miapi.material.base.ColorController;
import smartin.miapi.material.base.Material;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.render.colorproviders.ColorProvider;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeAble;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.ArrayList;
import java.util.List;

/**
 * NOTE!
 * this is functional, but badly outdated.
 * It is just the old 1.20 code, made functional
 * Reworking this for a more dynamic solution tied in with the normal Bakery makes much more sense.
 * But this likely has to wait till after 1.21.1
 * It would save a marginal performance to do so, but decent amount of complexity of this class.
 * Abandoning the DynmicBakery would also help to allow for 3rd party models to work with this.
 * TODO:rework the model handling
 */
@Environment(EnvType.CLIENT)
public class ModelProperty extends CodecProperty<List<ModelData>> {
    public static ModelProperty property;
    public static final ResourceLocation KEY = Miapi.id("model");
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
        ReloadEvents.START.subscribe((isClient, registryAccess, worker) -> ModelManager.modelCache.clear());
        MiapiItemModel.modelSuppliers.add((key, mode, model, stack) -> {
            List<MiapiModel> miapiModels = new ArrayList<>();
            for (ModelHolder holder : getForModule(model, key, stack)) {
                miapiModels.add(BakedMiapiModel.createBaked(holder, model, stack, mode));
            }
            return miapiModels;
        });
    }

    public static List<ModelHolder> getForModule(ModuleInstance instance, String key, ItemStack itemStack) {
        List<ModelData> modelDataList = property.getData(instance).orElse(new ArrayList<>());
        List<ModelHolder> models = new ArrayList<>();
        for (ModelData json : modelDataList) {
            bakedModel(instance, json, itemStack, key).forEach(models::add);
        }
        return models;
    }

    public static boolean isAllowedKey(@Nullable String jsonKey, @Nullable String modelTypeKey) {
        return jsonKey == null && modelTypeKey == null ||
               jsonKey != null && jsonKey.equals(modelTypeKey) ||
               jsonKey != null && modelTypeKey == null && jsonKey.equals("default") ||
               jsonKey == null && modelTypeKey != null && modelTypeKey.equals("default") ||
               "item".equals(modelTypeKey) && "default".equals(jsonKey) ||
               ("item".equals(jsonKey) && modelTypeKey == null);
    }

    public static List<ModelHolder> bakedModel(ModuleInstance instance, ModelData json, ItemStack itemStack, String key) {
        int condition = ColorController.getColor(StatResolver.resolveString(json.condition, instance));
        if (condition != 0) {
            if (
                    json.transform.origin == null && "item".equals(key) ||
                    json.transform.origin != null && json.transform.origin.equals(key) ||
                    ("item".equals(json.transform.origin) && key == null)) {
                return bakedModel(instance, json, itemStack);
            }
        }
        return List.of();
    }

    public static List<ModelHolder> bakedModel(ModuleInstance instance, ModelData json, ItemStack itemStack) {
        Material material = MaterialProperty.getMaterial(instance);
        json.repair();
        List<String> list = new ArrayList<>();
        if (material != null) {
            list.add(material.getStringID());
            list = material.getTextureKeys();
        } else {
            list.add("default");
        }
        UnbakedModelHolder unbakedModel = findUnbakedModel(json.path, list);
        if (unbakedModel == null) {
            return List.of();
        }
        List<ModelHolder> holders = new ArrayList<>();
        findModels(instance, json, itemStack, unbakedModel, holders, list);
        return holders;
    }

    private static @Nullable UnbakedModelHolder findUnbakedModel(String path, List<String> list) {
        UnbakedModelHolder unbakedModel = null;
        for (String str : list) {
            String fullPath = path.replace("[material.texture]", str);
            if (ModelManager.modelCache.containsKey(fullPath)) {
                unbakedModel = ModelManager.modelCache.get(fullPath);
                break;
            }
        }
        if (unbakedModel == null) {
            String fullPath = path.replace("[material.texture]", "default");
            if (ModelManager.modelCache.containsKey(fullPath)) {
                unbakedModel = ModelManager.modelCache.get(fullPath);
            }
        }
        return unbakedModel;
    }

    private static void findModels(ModuleInstance instance, ModelData json, ItemStack itemStack, UnbakedModelHolder unbakedModel, List<ModelHolder> holders, List<String> materialKeys) {
        BakedSingleModel model = DynamicBakery.bakeModel(unbakedModel.model(), ModelManager.textureGetter, FastColor.ARGB32.color(255, 255, 255, 255), Transform.IDENTITY);
        Matrix4f matrix4f = Transform.toModelTransformation(json.transform).toMatrix();
        String colorProviderId = unbakedModel.modelMetadata().colorProvider() != null ?
                unbakedModel.modelMetadata().colorProvider() : json.color_provider;
        ColorProvider colorProvider = ColorProvider.getProvider(colorProviderId, itemStack, instance, json.getTrimMode());
        if (colorProvider == null) {
            throw new RuntimeException("colorProvider is null");
        }
        holders.add(new ModelHolder(
                model.optimize(), matrix4f, colorProvider,
                unbakedModel.modelMetadata().lightValues() == null ? new int[]{-1, -1} : unbakedModel.modelMetadata().lightValues(),
                json.getTrimMode(), json.entity_render));
        if (holders.size() > 15) {
            return;
        }
        unbakedModel.modelMetadata().modelSources().forEach(modelSource -> {
            UnbakedModelHolder nextUnbaked = findUnbakedModel(modelSource, materialKeys);
            if (nextUnbaked != null) {
                findModels(instance, json, itemStack, nextUnbaked, holders, materialKeys);
            }
        });
    }

    @Override
    public boolean load(ResourceLocation moduleKey, JsonElement data, boolean isClient) throws Exception {
        decode(data).forEach(modelData -> {
            modelData.repair();
            ModelManager.loadModelsByPath(modelData.path);
        });
        return true;
    }

    @Override
    public List<ModelData> merge(List<ModelData> left, List<ModelData> right, MergeType mergeType) {
        return MergeAble.mergeList(left, right, mergeType);
    }
}
