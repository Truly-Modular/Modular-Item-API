package smartin.miapi.client.model.item;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.client.model.DynamicBakery;

import java.util.*;

/**
 * A BakedModel made to be semi mutable and allow for more dynamic interactions
 */
@Environment(EnvType.CLIENT)
public class BakedSingleModel implements BakedModel {
    public List<BakedQuad> quads;
    public ItemTransforms modelTransformation = ItemTransforms.NO_TRANSFORMS;
    public List<BakedModel> childModels = new ArrayList<>();
    private BakedSingleModel overrideModel;
    public ItemOverrides overrideList;

    public BakedSingleModel(List<BakedQuad> quads) {
        this.quads = quads;
        overrideList = new DynamicOverrides();
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, RandomSource random) {
        List<BakedQuad> bakedQuads = new ArrayList<>();
        quads.forEach(bakedQuad -> {
            if (bakedQuad.getDirection().equals(face)) {
                bakedQuads.add(bakedQuad);
            }
        });
        return bakedQuads;
    }

    public void addModel(BakedModel child) {
        if (overrideModel == null) {
            overrideModel = new BakedSingleModel(new ArrayList<>());
        }
        this.childModels.add(child);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean usesBlockLight() {
        return false;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        ResourceLocation stoneTextureId = ResourceLocation.fromNamespaceAndPath("minecraft", "block/stone");
        return Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(stoneTextureId);
    }

    @Override
    public ItemTransforms getTransforms() {
        return modelTransformation;
    }

    public void setModelTransformation(ItemTransforms transformation) {
        modelTransformation = transformation;
        this.childModels.forEach(childModel -> {
            if (childModel instanceof BakedSingleModel bakedSIngleModel) {
                bakedSIngleModel.setModelTransformation(transformation);
            } else {
                Miapi.LOGGER.warn("childModel is not a Dynamic Model. This should not happen");
            }
        });
    }

    @Override
    public ItemOverrides getOverrides() {
        if (overrideList instanceof DynamicOverrides && childModels.isEmpty()) return ItemOverrides.EMPTY;
        return overrideList;
    }

    class DynamicOverrides extends ItemOverrides {

        public DynamicOverrides() {
            super(DynamicBakery.dynamicBaker, null, new ArrayList<>());
        }

        @Override
        public BakedModel resolve(BakedModel oldmodel, ItemStack stack, @Nullable ClientLevel world, @Nullable LivingEntity entity, int seed) {
            if (overrideModel != null) {
                overrideModel.childModels.clear();
                overrideModel.quads.clear();
                overrideModel.quads.addAll(quads);
                childModels.forEach(model -> {
                    if (model != null && model.getOverrides() != null) {
                        BakedModel override = model.getOverrides().resolve(model, stack, world, entity, seed);
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
        Map<BakedSingleModelOverrides.ConditionHolder,
                Map<Direction, List<BakedQuad>>> completeList = new LinkedHashMap<>();
        childModels.forEach(child -> {
            if (child instanceof BakedSingleModel bakedSIngleModel) {
                putDirectionalQuads(bakedQuads, child);
                if (bakedSIngleModel.overrideList instanceof DynamicBakery.DynamicOverrideList dynamicOverrideList) {
                    for (DynamicBakery.DynamicOverrideList.DynamicBakedOverride overrides : dynamicOverrideList.dynamicOverrides) {
                        BakedSingleModelOverrides.ConditionHolder condition = overrides.conditionHolder;
                        assert condition != null;
                        if (!completeList.containsKey(condition)) {
                            completeList.put(condition, createEmpty());
                        }
                    }
                }
            }
        });
        childModels.forEach(child -> {
            if (child instanceof BakedSingleModel bakedSIngleModel) {
                if (bakedSIngleModel.overrideList instanceof DynamicBakery.DynamicOverrideList dynamicOverrideList) {
                    completeList.forEach(((conditionHolder, directionBakedQuadHashMap) -> {
                        int index = -1;
                        boolean fallback = true;
                        for (int i = 0; i < dynamicOverrideList.dynamicOverrides.length; i++) {
                            BakedSingleModelOverrides.ConditionHolder otherCOndition = dynamicOverrideList.dynamicOverrides[i].conditionHolder;
                            if (dynamicOverrideList.dynamicOverrides[i].conditionHolder.equals(conditionHolder)) {
                                putDirectionalQuads(directionBakedQuadHashMap, dynamicOverrideList.dynamicOverrides[i].model);
                                fallback = false;
                            }
                            if (conditionHolder.isAcceptable(otherCOndition)) {
                                index = i;
                            }
                        }
                        if (index == -1) {
                            putDirectionalQuads(directionBakedQuadHashMap, bakedSIngleModel);
                        } else {
                            if (fallback) {
                                putDirectionalQuads(directionBakedQuadHashMap, dynamicOverrideList.dynamicOverrides[index].model);
                            }
                        }
                    }));
                }
            }
        });
        Map<BakedSingleModelOverrides.ConditionHolder, BakedModel> overrideModels = new LinkedHashMap<>();
        completeList.forEach(((conditionHolder, directionBakedQuadHashMap) -> {
            List<BakedQuad> defaultList = directionBakedQuadHashMap.get(null);
            defaultList = defaultList == null ? new ArrayList<>() : defaultList;
            BakedModel model = new SimpleBakedModel(defaultList, directionBakedQuadHashMap, true, false, true, this.getParticleIcon(), this.modelTransformation, ItemOverrides.EMPTY);
            overrideModels.put(conditionHolder, model);
        }));
        List<BakedQuad> defaultList = bakedQuads.get(null);
        defaultList = defaultList == null ? new ArrayList<>() : defaultList;
        BakedModel model = new SimpleBakedModel(defaultList, bakedQuads, true, false, true, this.getParticleIcon(), this.modelTransformation, this.getOverrides());
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
        model.getQuads(null, null, RandomSource.create()).forEach(bakedQuad -> {
            List<BakedQuad> list = directionalQuads.getOrDefault(bakedQuad.getDirection(), new ArrayList<>());
            list.add(bakedQuad);
        });
        for (Direction dir : Direction.values()) {
            model.getQuads(null, dir, RandomSource.create()).forEach(bakedQuad -> {
                if (bakedQuad.getDirection().equals(dir)) {
                    List<BakedQuad> list = directionalQuads.getOrDefault(bakedQuad.getDirection(), new ArrayList<>());
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
