package smartin.miapi.modules.properties.render.baked;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.client.model.MiapiItemModel;
import smartin.miapi.client.model.MiapiModel;
import smartin.miapi.client.model.ModelHolder;
import smartin.miapi.client.model.module.BakedMiapiModel;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeAble;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

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
        ReloadEvents.START.subscribe((isClient, registryAccess, worker) -> modelCache.clear());
        ModularItemCache.setSupplier(CACHE_KEY_ITEM, (stack) -> getModelMap(stack).get("item"));
        MiapiItemModel.modelSuppliers.add((key, mode, model, stack) -> {
            List<MiapiModel> miapiModels = new ArrayList<>();
            for (ModelHolder holder : getForModule(model, key, stack)) {
                miapiModels.add(BakedMiapiModel.createBaked(holder, model, stack,mode));
            }
            return miapiModels;
        });
    }

    public static List<ModelHolder> getForModule(ModuleInstance instance, String key, ItemStack itemStack) {
        List<ModelData> modelDataList = property.getData(instance).orElse(new ArrayList<>());
        List<ModelHolder> models = new ArrayList<>();
        for (ModelData json : modelDataList) {
            ModelHolder holder = ModelHolder.bakedModel(instance, json, itemStack, key);
            if (holder != null) {
                models.add(holder);
            }
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

    public static Map<String, BakedModel> getModelMap(ItemStack stack) {
        //return (Map<String, BakedModel>) ModularItemCache.getRaw(stack, CACHE_KEY_MAP);
        return new HashMap<>();
    }

    @Nullable
    public static BakedModel getItemModel(ItemStack stack) {
        return ModularItemCache.getRaw(stack, CACHE_KEY_ITEM);
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
