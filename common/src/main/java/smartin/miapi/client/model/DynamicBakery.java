package smartin.miapi.client.model;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.ModelPredicateProvider;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.render.model.ModelRotation;
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
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public class DynamicBakery {
    private DynamicBakery(){

    }
    private static final BakedQuadFactory QUAD_FACTORY = new BakedQuadFactory();

    public static BakedModel bake(JsonUnbakedModel model, ModelLoader loader, JsonUnbakedModel parent, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings settings, Identifier id, boolean hasDepth, int color) {
        Sprite sprite = textureGetter.apply(model.resolveSprite("particle"));
        if (model.getRootModel() == ModelLoader.BLOCK_ENTITY_MARKER) {
            return new BuiltinBakedModel(model.getTransformations(), compileOverrides(model, loader, parent, color), sprite, model.getGuiLight().isSide());
        } else {
            BasicBakedModel.Builder builder = (new BasicBakedModel.Builder(model, compileOverrides(model, loader, parent, color), hasDepth)).setParticle(sprite);

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

            return builder.build();
        }
    }

    private static ModelOverrideList compileOverrides(JsonUnbakedModel model, ModelLoader modelLoader, JsonUnbakedModel parent, int color) {
        if (model.getOverrides().isEmpty()) {
            return ModelOverrideList.EMPTY;
        } else {
            Objects.requireNonNull(modelLoader);
            return new OverrideList(modelLoader, parent, modelLoader::getOrLoadModel, model.getOverrides(), color);
        }
    }

    private static BakedQuad createQuad(ModelElement element, ModelElementFace elementFace, Sprite sprite, Direction side, ModelBakeSettings settings, Identifier id, int color) {
        BakedQuad quad = QUAD_FACTORY.bake(element.from, element.to, elementFace, sprite, side, settings, element.rotation, element.shade, id);
        quad = ColorUtil.recolorBakedQuad(quad, color);
        return quad;
    }

    static class OverrideList extends ModelOverrideList {
        private final DynamicBakedOverride[] dynamicOverrides;
        private final Identifier[] dynamicConditionTypes;

        public OverrideList(ModelLoader modelLoader, JsonUnbakedModel parent, Function<Identifier, UnbakedModel> unbakedModelGetter, List<ModelOverride> overrides, int color) {
            super(modelLoader, parent, unbakedModelGetter, overrides);
            this.dynamicConditionTypes = overrides.stream().flatMap(ModelOverride::streamConditions).map(ModelOverride.Condition::getType).distinct().toArray(Identifier[]::new);
            Object2IntMap<Identifier> object2IntMap = new Object2IntOpenHashMap();

            for (int i = 0; i < this.dynamicConditionTypes.length; ++i) {
                object2IntMap.put(this.dynamicConditionTypes[i], i);
            }

            List<DynamicBakedOverride> list = Lists.newArrayList();

            for (int j = overrides.size() - 1; j >= 0; --j) {
                ModelOverride modelOverride = overrides.get(j);
                BakedModel bakedModel = this.bakeOverridingModel(modelLoader, parent, unbakedModelGetter, modelOverride);
                assert bakedModel != null;
                bakedModel = ColorUtil.recolorModel(bakedModel, color);
                InlinedCondition[] inlinedConditions = modelOverride.streamConditions().map((condition) -> {
                    int i = object2IntMap.getInt(condition.getType());
                    return new InlinedCondition(i, condition.getThreshold());
                }).toArray(InlinedCondition[]::new);
                list.add(new DynamicBakedOverride(inlinedConditions, bakedModel));
            }

            this.dynamicOverrides = list.toArray(new DynamicBakedOverride[0]);
        }

        @Environment(EnvType.CLIENT)
        static class DynamicBakedOverride {
            private final InlinedCondition[] conditions;
            @Nullable
            final BakedModel model;

            DynamicBakedOverride(InlinedCondition[] conditions, @Nullable BakedModel model) {
                this.conditions = conditions;
                this.model = model;
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
        private BakedModel bakeOverridingModel(ModelLoader loader, JsonUnbakedModel parent, Function<Identifier, UnbakedModel> unbakedModelGetter, ModelOverride override) {
            UnbakedModel unbakedModel = unbakedModelGetter.apply(override.getModelId());
            return Objects.equals(unbakedModel, parent) ? null : loader.bake(override.getModelId(), ModelRotation.X0_Y0);
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

                DynamicBakedOverride[] var16 = this.dynamicOverrides;
                int var14 = var16.length;

                for (int var15 = 0; var15 < var14; ++var15) {
                    DynamicBakedOverride bakedOverride = var16[var15];
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
