package smartin.miapi.client.model;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.ModelPredicateProvider;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.render.model.*;
import net.minecraft.client.render.model.json.*;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.modules.properties.render.ModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public class DynamicBakery {
    public static Baker dynamicBaker;
    static final ItemModelGenerator ITEM_MODEL_GENERATOR = new ItemModelGenerator();

    private DynamicBakery() {

    }

    private static final BakedQuadFactory QUAD_FACTORY = new BakedQuadFactory();

    public static DynamicBakedModel bakeModel(JsonUnbakedModel unbakedModel, Function<SpriteIdentifier, Sprite> textureGetter, int color, ModelBakeSettings settings) {
        try {
            ModelLoader modelLoader = ModelLoadAccessor.getLoader();
            AtomicReference<JsonUnbakedModel> actualModel = new AtomicReference<>(unbakedModel);
            unbakedModel.getModelDependencies().stream().filter(identifier -> identifier.toString().equals("minecraft:item/generated") || identifier.toString().contains("handheld")).findFirst().ifPresent(identifier -> {
                actualModel.set(ITEM_MODEL_GENERATOR.create(ModelProperty.textureGetter, unbakedModel));
            });
            DynamicBakedModel model = DynamicBakery.bake(actualModel.get(), modelLoader, unbakedModel.getRootModel(), textureGetter, settings, new Identifier(unbakedModel.id), true, color);
            for (Direction direction : Direction.values()) {
                if (!model.getQuads(null, direction, Random.create()).isEmpty()) {
                    return model;
                }
            }
            try {
                actualModel.set(ITEM_MODEL_GENERATOR.create(ModelProperty.textureGetter, unbakedModel));
                return DynamicBakery.bake(actualModel.get(), modelLoader, unbakedModel.getRootModel(), textureGetter, settings, new Identifier(unbakedModel.id), true, color);
            } catch (Exception surpressed) {

            }
            return model;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static DynamicBakedModel bake(JsonUnbakedModel model, ModelLoader loader, JsonUnbakedModel parent, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings settings, Identifier id, boolean hasDepth, int color) {
        Sprite sprite = textureGetter.apply(model.resolveSprite("particle"));
        if (model.getRootModel() == ModelLoader.BLOCK_ENTITY_MARKER) {
            return dynamicBakedModel(new BuiltinBakedModel(model.getTransformations(), compileOverrides(model, loader, parent, textureGetter, settings, color), sprite, model.getGuiLight().isSide()));
        } else {
            BasicBakedModel.Builder builder = (new BasicBakedModel.Builder(model, compileOverrides(model, loader, parent, textureGetter, settings, color), hasDepth)).setParticle(sprite);

            for (ModelElement modelElement : model.getElements()) {

                for (Direction direction : modelElement.faces.keySet()) {
                    ModelElementFace modelElementFace = modelElement.faces.get(direction);
                    Sprite sprite2 = textureGetter.apply(model.resolveSprite(modelElementFace.textureId));
                    if (modelElementFace.cullFace == null) {
                        builder.addQuad(createQuad(modelElement, modelElementFace, sprite2, direction, settings, id, color));
                    } else {
                        builder.addQuad(Direction.transform(settings.getRotation().getMatrix(), modelElementFace.cullFace), createQuad(modelElement, modelElementFace, sprite2, direction, settings, id, color));
                    }
                }
            }

            return dynamicBakedModel(builder.build());
        }
    }

    public static DynamicBakedModel dynamicBakedModel(BakedModel model) {
        if (model instanceof DynamicBakedModel dynamicBakedModel) {
            new Exception().printStackTrace();
            return dynamicBakedModel;
        }
        List<BakedQuad> quads = new ArrayList<>();
        quads.addAll(model.getQuads(null, null, Random.create()));
        for (Direction dir : Direction.values()) {
            quads.addAll(model.getQuads(null, dir, Random.create()));
        }
        DynamicBakedModel model1 = new DynamicBakedModel(quads);
        model1.overrideList = model.getOverrides();
        return model1;
    }

    private static ModelOverrideList compileOverrides(JsonUnbakedModel model, ModelLoader modelLoader, JsonUnbakedModel parent, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotation, int color) {
        if (model.getOverrides().isEmpty()) {
            return ModelOverrideList.EMPTY;
        } else {
            Objects.requireNonNull(modelLoader);
            return new DynamicOverrideList(modelLoader, parent, modelLoader::getOrLoadModel, model.getOverrides(), textureGetter, rotation, color);
        }
    }

    private static BakedQuad createQuad(ModelElement element, ModelElementFace elementFace, Sprite sprite, Direction side, ModelBakeSettings settings, Identifier id, int color) {
        BakedQuad quad = QUAD_FACTORY.bake(element.from, element.to, elementFace, sprite, side, settings, element.rotation, element.shade, id);
        quad = ColorUtil.recolorBakedQuad(quad, color);
        return quad;
    }

    static class DynamicOverrideList extends ModelOverrideList {
        public final DynamicBakedOverride[] dynamicOverrides;
        public final Identifier[] dynamicConditionTypes;
        public final List<ModelOverride> overrideList;

        public DynamicOverrideList(ModelLoader modelLoader, JsonUnbakedModel parent, Function<Identifier, UnbakedModel> unbakedModelGetter, List<ModelOverride> overrides, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotation, int color) {
            super(dynamicBaker, parent, overrides);
            this.dynamicConditionTypes = overrides.stream().flatMap(ModelOverride::streamConditions).map(ModelOverride.Condition::getType).distinct().toArray(Identifier[]::new);
            Object2IntMap<Identifier> object2IntMap = new Object2IntOpenHashMap<>();
            this.overrideList = overrides;

            for (int i = 0; i < this.dynamicConditionTypes.length; ++i) {
                object2IntMap.put(this.dynamicConditionTypes[i], i);
            }

            List<DynamicBakedOverride> list = Lists.newArrayList();

            for (int j = overrides.size() - 1; j >= 0; --j) {
                ModelOverride modelOverride = overrides.get(j);
                JsonUnbakedModel model = ModelProperty.modelCache.get(modelOverride.getModelId().toString());
                DynamicBakedModel bakedModel = bakeModel(model, textureGetter, color, rotation);
                assert bakedModel != null;
                bakedModel = (DynamicBakedModel) ColorUtil.recolorModel(bakedModel, color);
                InlinedCondition[] inlinedConditions = modelOverride.streamConditions().map((condition) -> {
                    int i = object2IntMap.getInt(condition.getType());
                    return new InlinedCondition(i, condition.getThreshold());
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
            public final DynamicModelOverrides.ConditionHolder conditionHolder;

            DynamicBakedOverride(InlinedCondition[] conditions, @Nullable BakedModel model, ModelOverride override) {
                this.conditions = conditions;
                this.model = model;
                this.conditionHolder = new DynamicModelOverrides.ConditionHolder(override);
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
        public BakedModel apply(BakedModel model, ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity entity, int seed) {
            if (this.dynamicOverrides.length != 0) {
                Item item = stack.getItem();
                int i = this.dynamicConditionTypes.length;
                float[] fs = new float[i];

                for (int j = 0; j < i; ++j) {
                    Identifier identifier = this.dynamicConditionTypes[j];
                    ModelPredicateProvider modelPredicateProvider = ModelPredicateProviderRegistry.get(item, identifier);
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
