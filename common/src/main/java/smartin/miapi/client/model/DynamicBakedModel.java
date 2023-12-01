package smartin.miapi.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.BasicBakedModel;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;

import java.util.*;

/**
 * A BakedModel made to be semi mutable and allow for more dynamic interactions
 */
@Environment(EnvType.CLIENT)
public class DynamicBakedModel implements BakedModel {
    public List<BakedQuad> quads;
    public ModelTransformation modelTransformation = ModelTransformation.NONE;
    public List<BakedModel> childModels = new ArrayList<>();
    private DynamicBakedModel overrideModel;
    public ModelOverrideList overrideList;

    public DynamicBakedModel(List<BakedQuad> quads) {
        this.quads = quads;
        overrideList = new DynamicOverrides();
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, Random random) {
        List<BakedQuad> bakedQuads = new ArrayList<>();
        quads.forEach(bakedQuad -> {
            if (bakedQuad.getFace().equals(face)) {
                bakedQuads.add(bakedQuad);
            }
        });
        return bakedQuads;
    }

    public void addModel(BakedModel child) {
        if (overrideModel == null) {
            overrideModel = new DynamicBakedModel(new ArrayList<>());
        }
        this.childModels.add(child);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean hasDepth() {
        return false;
    }

    @Override
    public boolean isSideLit() {
        return false;
    }

    @Override
    public boolean isBuiltin() {
        return false;
    }

    @Override
    public Sprite getParticleSprite() {
        Identifier stoneTextureId = new Identifier("minecraft", "block/stone");
        return MinecraftClient.getInstance().getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE).apply(stoneTextureId);
    }

    @Override
    public ModelTransformation getTransformation() {
        return modelTransformation;
    }

    public void setModelTransformation(ModelTransformation transformation) {
        modelTransformation = transformation;
        this.childModels.forEach(childModel -> {
            if (childModel instanceof DynamicBakedModel dynamicBakedModel) {
                dynamicBakedModel.setModelTransformation(transformation);
            } else {
                Miapi.LOGGER.warn("childModel is not a Dynamic Model. This should not happen");
            }
        });
    }

    @Override
    public ModelOverrideList getOverrides() {
        if (overrideList instanceof DynamicOverrides && childModels.isEmpty()) return ModelOverrideList.EMPTY;
        return overrideList;
    }

    class DynamicOverrides extends ModelOverrideList {

        public DynamicOverrides() {
            super(DynamicBakery.dynamicBaker, null, new ArrayList<>());
        }

        @Override
        public BakedModel apply(BakedModel oldmodel, ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity entity, int seed) {
            if (overrideModel != null) {
                overrideModel.childModels.clear();
                overrideModel.quads.clear();
                overrideModel.quads.addAll(quads);
                childModels.forEach(model -> {
                    if (model != null && model.getOverrides() != null) {
                        BakedModel override = model.getOverrides().apply(model, stack, world, entity, seed);
                        overrideModel.addModel(override);
                    } else {
                        overrideModel.addModel(model);
                    }
                });
                return overrideModel;
            }
            return oldmodel;
        }
    }

    public BakedModel optimize() {
        Map<Direction, List<BakedQuad>> bakedQuads = createEmpty();
        putDirectionalQuads(bakedQuads, this);
        Map<DynamicModelOverrides.ConditionHolder,
                Map<Direction, List<BakedQuad>>> completeList = new LinkedHashMap<>();
        childModels.forEach(child -> {
            if (child instanceof DynamicBakedModel dynamicBakedModel) {
                putDirectionalQuads(bakedQuads, child);
                if (dynamicBakedModel.overrideList instanceof DynamicBakery.DynamicOverrideList dynamicOverrideList) {
                    for (DynamicBakery.DynamicOverrideList.DynamicBakedOverride overrides : dynamicOverrideList.dynamicOverrides) {
                        DynamicModelOverrides.ConditionHolder condition = overrides.conditionHolder;
                        assert condition != null;
                        if (!completeList.containsKey(condition)) {
                            completeList.put(condition, createEmpty());
                        }
                    }
                }
            }
        });
        childModels.forEach(child -> {
            if (child instanceof DynamicBakedModel dynamicBakedModel) {
                if (dynamicBakedModel.overrideList instanceof DynamicBakery.DynamicOverrideList dynamicOverrideList) {
                    completeList.forEach(((conditionHolder, directionBakedQuadHashMap) -> {
                        int index = -1;
                        boolean fallback = true;
                        for (int i = 0; i < dynamicOverrideList.dynamicOverrides.length; i++) {
                            DynamicModelOverrides.ConditionHolder otherCOndition = dynamicOverrideList.dynamicOverrides[i].conditionHolder;
                            if (dynamicOverrideList.dynamicOverrides[i].conditionHolder.equals(conditionHolder)) {
                                putDirectionalQuads(directionBakedQuadHashMap, dynamicOverrideList.dynamicOverrides[i].model);
                                fallback = false;
                            }
                            if (conditionHolder.isAcceptable(otherCOndition)) {
                                index = i;
                            }
                        }
                        if (index == -1) {
                            putDirectionalQuads(directionBakedQuadHashMap, dynamicBakedModel);
                        } else {
                            if (fallback) {
                                putDirectionalQuads(directionBakedQuadHashMap, dynamicOverrideList.dynamicOverrides[index].model);
                            }
                        }
                    }));
                }
            }
        });
        Map<DynamicModelOverrides.ConditionHolder, BakedModel> overrideModels = new LinkedHashMap<>();
        completeList.forEach(((conditionHolder, directionBakedQuadHashMap) -> {
            List<BakedQuad> defaultList = directionBakedQuadHashMap.get(null);
            defaultList = defaultList == null ? new ArrayList<>() : defaultList;
            BakedModel model = new BasicBakedModel(defaultList, directionBakedQuadHashMap, true, false, true, this.getParticleSprite(), this.modelTransformation, ModelOverrideList.EMPTY);
            overrideModels.put(conditionHolder, model);
        }));
        List<BakedQuad> defaultList = bakedQuads.get(null);
        defaultList = defaultList == null ? new ArrayList<>() : defaultList;
        Map<Direction,List<BakedQuad>> map = new HashMap<>();
        for(Direction direction:Direction.values()){
            map.put(direction,new ArrayList<>());
        }
        BakedModel model = new BasicBakedModel(new ArrayList<>(), bakedQuads, true, false, true, this.getParticleSprite(), this.modelTransformation, ModelOverrideList.EMPTY);
        return model;
    }

    private Map<Direction, List<BakedQuad>> createEmpty() {
        HashMap<Direction, List<BakedQuad>> defaultMap = new HashMap<>();
        for (Direction direction : Direction.values()) {
            defaultMap.put(direction, new ArrayList<>());
        }
        defaultMap.put(null, new ArrayList<>());
        return defaultMap;
    }

    private void putDirectionalQuads(Map<Direction, List<BakedQuad>> directionalQuads, BakedModel model) {
        model.getQuads(null, null, Random.create()).forEach(bakedQuad -> {
            List<BakedQuad> list = directionalQuads.getOrDefault(bakedQuad.getFace(), new ArrayList<>());
            list.add(bakedQuad);
        });
        for (Direction dir : Direction.values()) {
            model.getQuads(null, dir, Random.create()).forEach(bakedQuad -> {
                if (bakedQuad.getFace().equals(dir)) {
                    List<BakedQuad> list = directionalQuads.getOrDefault(bakedQuad.getFace(), new ArrayList<>());
                    list.add(bakedQuad);
                }
            });
        }
        for (Direction direction : Direction.values()) {
            directionalQuads.put(direction, cleanUp(directionalQuads.get(direction)));
        }
        directionalQuads.put(null, cleanUp(directionalQuads.get(null)));
    }

    private List<BakedQuad> cleanUp(List<BakedQuad> quads) {
        return quads;
    }
}
