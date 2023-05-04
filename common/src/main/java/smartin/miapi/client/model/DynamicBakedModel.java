package smartin.miapi.client.model;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;

import java.util.ArrayList;
import java.util.List;

/**
 * A BakedModel made to be semi mutable and allow for more dynamic interactions
 */
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
        List<BakedQuad> list = new ArrayList<>();
        childModels.forEach(model -> {
            list.addAll(model.getQuads(state, face, random));
        });
        list.addAll(quads);
        return list;
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
        return null;
    }

    @Override
    public ModelTransformation getTransformation() {
        return modelTransformation;
    }

    @Override
    public ModelOverrideList getOverrides() {
        if(overrideList instanceof DynamicOverrides && childModels.isEmpty()) return ModelOverrideList.EMPTY;
        return overrideList;
    }

    class DynamicOverrides extends ModelOverrideList {

        public DynamicOverrides() {
            super(null, null, null, new ArrayList<>());
        }

        @Override
        public BakedModel apply(BakedModel oldmodel, ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity entity, int seed) {
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
    }
}
