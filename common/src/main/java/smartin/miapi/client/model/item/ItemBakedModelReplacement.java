package smartin.miapi.client.model.item;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.client.model.DynamicBakery;
import smartin.miapi.modules.properties.render.ModelProperty;
import smartin.miapi.registries.RegistryInventory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import static smartin.miapi.client.renderer.SpriteLoader.miapiModels;

@Environment(EnvType.CLIENT)
public class ItemBakedModelReplacement implements UnbakedModel, BakedModel {
    public static LivingEntity currentEntity = null;

    public static boolean isModularItem(ResourceLocation identifier) {
        if (identifier != null && identifier.toString() != null) {
            return RegistryInventory.modularItems.get(identifier.toString().replace("item/", "")) != null;
        }
        return false;
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemBakedModelOverrides.EMPTY;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, net.minecraft.util.RandomSource random) {
        return new ArrayList<>();
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
        return true;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        ResourceLocation stoneTextureId = ResourceLocation.fromNamespaceAndPath("minecraft", "block/stone");
        return Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(stoneTextureId);
    }

    @Override
    public ItemTransforms getTransforms() {
        return ItemTransforms.NO_TRANSFORMS;
    }


    @Override
    public Collection<ResourceLocation> getDependencies() {
        return miapiModels;
    }

    @Override
    public void resolveParents(Function<ResourceLocation, UnbakedModel> modelLoader) {
        miapiModels.forEach(modelLoader::apply);
    }

    @Nullable
    @Override
    public BakedModel bake(ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState state) {
        ModelProperty.textureGetter = spriteGetter;
        DynamicBakery.dynamicBaker = baker;
        //overrides = new ItemBakedModelOverrides();
        return this;
    }

    @Override
    public String toString() {
        return "CustomModel{" +
               //"overrides=" + overrides +
               '}';
    }
}
