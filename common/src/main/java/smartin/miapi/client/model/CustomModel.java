package smartin.miapi.client.model;

import com.mojang.datafixers.util.Pair;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.*;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.render.ModelProperty;

import java.util.*;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public class CustomModel implements UnbakedModel, BakedModel {
    private CustomModelOverrides overrides;

    public CustomModel() {
        overrides = new CustomModelOverrides();
    }

    @Override
    public ModelOverrideList getOverrides() {
        return overrides;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, net.minecraft.util.math.random.Random random) {
        return new ArrayList<>();
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
        return ModelTransformation.NONE;
    }


    @Override
    public Collection<Identifier> getModelDependencies() {
        return Collections.EMPTY_LIST;
    }

    @Override
    public void setParents(Function<Identifier, UnbakedModel> modelLoader) {

    }

    @Nullable
    @Override
    public BakedModel bake(Baker baker, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
        ModelProperty.textureGetter = textureGetter;
        DynamicBakery.dynamicBaker = baker;
        return this;
    }
}
