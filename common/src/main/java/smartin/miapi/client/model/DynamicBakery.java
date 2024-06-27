package smartin.miapi.client.model;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.render.model.*;
import net.minecraft.client.render.model.json.*;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.BuiltInModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.client.model.item.BakedSingleModelOverrides;
import smartin.miapi.client.model.item.BakedSingleModel;
import smartin.miapi.item.modular.Transform;
import smartin.miapi.modules.properties.render.ModelProperty;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public class DynamicBakery {
    public static ModelBaker dynamicBaker;
    static final ItemModelGenerator ITEM_MODEL_GENERATOR = new ItemModelGenerator();

    private DynamicBakery() {

    }

    private static final FaceBakery QUAD_FACTORY = new FaceBakery();

    public static BakedSingleModel bakeModel(BlockModel unbakedModel, Function<Material, TextureAtlasSprite> textureGetter, int color, Transform settings) {
        try {
            ModelBakery modelLoader = ModelLoadAccessor.getLoader();
            AtomicReference<BlockModel> actualModel = new AtomicReference<>(unbakedModel);
            unbakedModel.getDependencies().stream().filter(identifier -> identifier.toString().equals("minecraft:item/generated") || identifier.toString().contains("handheld")).findFirst().ifPresent(identifier -> {
                actualModel.set(ITEM_MODEL_GENERATOR.generateBlockModel(ModelProperty.textureGetter, unbakedModel));
            });
            BakedSingleModel model = DynamicBakery.bake(actualModel.get(), modelLoader, unbakedModel.getRootModel(), textureGetter, Transform.toModelTransformation(settings), new ResourceLocation(unbakedModel.name), true, color);
            for (Direction direction : Direction.values()) {
                if (!model.getQuads(null, direction, RandomSource.create()).isEmpty()) {
                    return model;
                }
            }
            try {
                actualModel.set(ITEM_MODEL_GENERATOR.generateBlockModel(ModelProperty.textureGetter, unbakedModel));
                return DynamicBakery.bake(actualModel.get(), modelLoader, unbakedModel.getRootModel(), textureGetter, Transform.toModelTransformation(settings), new ResourceLocation(unbakedModel.name), true, color);
            } catch (Exception suppressed) {

            }
            return model;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static BakedSingleModel bake(BlockModel model, ModelBakery loader, BlockModel parent, Function<Material, TextureAtlasSprite> textureGetter, Transform settings, ResourceLocation id, boolean hasDepth, int color) {
        TextureAtlasSprite sprite = textureGetter.apply(model.getMaterial("particle"));
        if (model.getRootModel() == ModelBakery.BLOCK_ENTITY_MARKER) {
            BakedModel model1 = new BuiltInModel(model.getTransforms(), compileOverrides(model, loader, parent, textureGetter, BlockModelRotation.X0_Y0, color), sprite, model.getGuiLight().lightLikeBlock());
            return dynamicBakedModel(rotate(model1, settings));
        } else {
            SimpleBakedModel.Builder builder = (new SimpleBakedModel.Builder(model, compileOverrides(model, loader, parent, textureGetter, BlockModelRotation.X0_Y0, color), hasDepth)).particle(sprite);

            for (BlockElement modelElement : model.getElements()) {

                for (Direction direction : modelElement.faces.keySet()) {
                    BlockElementFace modelElementFace = modelElement.faces.get(direction);
                    TextureAtlasSprite sprite2 = textureGetter.apply(model.getMaterial(modelElementFace.texture));
                    if (modelElementFace.cullForDirection == null) {
                        builder.addUnculledFace(createQuad(modelElement, modelElementFace, sprite2, direction, BlockModelRotation.X0_Y0, id, color));
                    } else {
                        builder.addCulledFace(Direction.rotate(BlockModelRotation.X0_Y0.getRotation().getMatrix(), modelElementFace.cullForDirection), createQuad(modelElement, modelElementFace, sprite2, direction, BlockModelRotation.X0_Y0, id, color));
                    }
                }
            }

            return dynamicBakedModel(rotate(builder.build(), settings));
        }
    }

    public static BakedModel rotate(BakedModel model, Transform transform) {
        List<BakedQuad> quads = new ArrayList<>();
        Map<Direction, List<BakedQuad>> directionBakedModelMap = new HashMap<>();
        for (Direction direction : Direction.values()) {
            directionBakedModelMap.put(direction, new ArrayList<>());
        }
        for (BakedQuad quad : model.getQuads(null, null, RandomSource.create())) {
            quads.addAll(rotate(quad, transform));
        }
        for (Direction direction : Direction.values()) {
            for (BakedQuad quad : model.getQuads(null, direction, RandomSource.create())) {
                quads.addAll(rotate(quad, transform));
            }
        }
        return new SimpleBakedModel(quads, directionBakedModelMap, model.useAmbientOcclusion(), model.usesBlockLight(), model.isGui3d(), model.getParticleIcon(), model.getTransforms(), model.getOverrides());
    }

    public static List<BakedQuad> rotate(BakedQuad quad, Transform transform) {
        int[] rotatedData = transform.rotateVertexData(quad.getVertices());

        for (int i = 0; i < 4; i++) {
            //rotatedData[4 + 8 * i] = Float.floatToIntBits(Float.intBitsToFloat(rotatedData[4 + 8 * i]) + Float.intBitsToFloat(rotatedData[4]) - Float.intBitsToFloat(rotatedData[4 + 8 * 3]));
        }
        //rotatedData[5] = rotatedData[4]+100;
        //rotatedData[8] = rotatedData[16];

        //rotatedData[9] = rotatedData[17];
        //rotatedData[10] = rotatedData[18];

        List<BakedQuad> quads = new ArrayList<>();
        quads.add(new BakedQuad(rotatedData, quad.getTintIndex(), Direction.rotate(transform.toMatrix(), quad.getDirection()), quad.getSprite(), quad.isShade()));

        for (int i = 0; i < rotatedData.length; i += 8) {
            int endIndex = Math.min(i + 8, rotatedData.length);
            int[] individualArray = Arrays.copyOfRange(rotatedData, i, endIndex);
            //quads.add(new BakedQuad(individualArray, quad.getColorIndex(), Direction.transform(transform.toMatrix(), quad.getFace()), quad.getSprite(), quad.hasShade()));

        }
        return quads;
    }

    public static BakedSingleModel dynamicBakedModel(BakedModel model) {
        if (model instanceof BakedSingleModel bakedSIngleModel) {
            new Exception().printStackTrace();
            return bakedSIngleModel;
        }
        List<BakedQuad> quads = new ArrayList<>();
        quads.addAll(model.getQuads(null, null, RandomSource.create()));
        for (Direction dir : Direction.values()) {
            quads.addAll(model.getQuads(null, dir, RandomSource.create()));
        }
        BakedSingleModel model1 = new BakedSingleModel(quads);
        model1.overrideList = model.getOverrides();
        return model1;
    }

    private static ItemOverrides compileOverrides
            (BlockModel model, ModelBakery modelLoader, BlockModel parent, Function<Material, TextureAtlasSprite> textureGetter, ModelState rotation,
             int color) {
        if (model.getOverrides().isEmpty()) {
            return ItemOverrides.EMPTY;
        } else {
            Objects.requireNonNull(modelLoader);
            return new DynamicOverrideList(modelLoader, parent, modelLoader::getOrLoadModel, model.getOverrides(), textureGetter, rotation, color);
        }
    }

    private static BakedQuad createQuad
            (BlockElement element, BlockElementFace elementFace, TextureAtlasSprite sprite, Direction side, ModelState settings, ResourceLocation id,
             int color) {
        BakedQuad quad = QUAD_FACTORY.bakeQuad(element.from, element.to, elementFace, sprite, side, settings, element.rotation, element.shade, id);
        quad = ColorUtil.recolorBakedQuad(quad, color);
        return quad;
    }

    public static class DynamicOverrideList extends ItemOverrides {
        public final DynamicBakedOverride[] dynamicOverrides;
        public final ResourceLocation[] dynamicConditionTypes;
        public final List<ItemOverride> overrideList;

        public DynamicOverrideList(ModelBakery modelLoader, BlockModel parent, Function<ResourceLocation, UnbakedModel> unbakedModelGetter, List<ItemOverride> overrides, Function<Material, TextureAtlasSprite> textureGetter, ModelState rotation, int color) {
            super(dynamicBaker, parent, overrides);
            this.dynamicConditionTypes = overrides.stream().flatMap(ItemOverride::getPredicates).map(ItemOverride.Predicate::getProperty).distinct().toArray(ResourceLocation[]::new);
            Object2IntMap<ResourceLocation> object2IntMap = new Object2IntOpenHashMap<>();
            this.overrideList = overrides;

            for (int i = 0; i < this.dynamicConditionTypes.length; ++i) {
                object2IntMap.put(this.dynamicConditionTypes[i], i);
            }

            List<DynamicBakedOverride> list = Lists.newArrayList();

            for (int j = overrides.size() - 1; j >= 0; --j) {
                ItemOverride modelOverride = overrides.get(j);
                BlockModel model = ModelProperty.modelCache.get(modelOverride.getModel().toString()).model();
                BakedSingleModel bakedModel = bakeModel(model, textureGetter, color, Transform.IDENTITY);
                assert bakedModel != null;
                bakedModel = (BakedSingleModel) ColorUtil.recolorModel(bakedModel, color);
                InlinedCondition[] inlinedConditions = modelOverride.getPredicates().map((condition) -> {
                    int i = object2IntMap.getInt(condition.getProperty());
                    return new InlinedCondition(i, condition.getValue());
                }).toArray(InlinedCondition[]::new);
                list.add(new DynamicBakedOverride(inlinedConditions, bakedModel, overrides.get(j)));
            }

            this.dynamicOverrides = list.toArray(new DynamicBakedOverride[0]);
        }

        @Environment(EnvType.CLIENT)
        public static class DynamicBakedOverride {
            public final InlinedCondition[] conditions;
            @Nullable
            public final BakedModel model;
            public final BakedSingleModelOverrides.ConditionHolder conditionHolder;

            DynamicBakedOverride(InlinedCondition[] conditions, @Nullable BakedModel model, ItemOverride override) {
                this.conditions = conditions;
                this.model = model;
                this.conditionHolder = new BakedSingleModelOverrides.ConditionHolder(override);
            }

            boolean test(float[] values) {

                for (InlinedCondition inlinedCondition : this.conditions) {
                    float f = values[inlinedCondition.index];
                    if (f < inlinedCondition.threshold) {
                        return false;
                    }
                }

                return true;
            }
        }

        @Nullable
        public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel world, @Nullable LivingEntity entity, int seed) {
            if (this.dynamicOverrides.length != 0) {
                Item item = stack.getItem();
                int i = this.dynamicConditionTypes.length;
                float[] fs = new float[i];

                for (int j = 0; j < i; ++j) {
                    ResourceLocation identifier = this.dynamicConditionTypes[j];
                    ItemPropertyFunction modelPredicateProvider = ItemProperties.getProperty(item, identifier);
                    if (modelPredicateProvider != null) {
                        fs[j] = modelPredicateProvider.call(stack, world, entity, seed);
                    } else {
                        fs[j] = Float.NEGATIVE_INFINITY;
                    }
                }

                for (DynamicBakedOverride bakedOverride : this.dynamicOverrides) {
                    if (bakedOverride.test(fs)) {
                        BakedModel bakedModel = bakedOverride.model;
                        if (bakedModel == null) {
                            return model;
                        }

                        return bakedModel;
                    }
                }
            }

            return model;
        }

        @Environment(EnvType.CLIENT)
        private record InlinedCondition(int index, float threshold) {
        }
    }
}
