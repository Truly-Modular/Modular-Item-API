package smartin.miapi.forge;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;
import org.lwjgl.system.NonnullDefault;
import smartin.miapi.client.model.DynamicBakery;
import smartin.miapi.client.model.item.ItemBakedModelReplacement;

import java.util.function.Function;

@NonnullDefault
public class ForgeModel implements IUnbakedGeometry<ForgeModel> {
    public static MultiBufferSource source;

    @Override
    public BakedModel bake(IGeometryBakingContext iGeometryBakingContext, ModelBaker arg, Function<Material, TextureAtlasSprite> function, ModelState arg2, ItemOverrides arg3) {
        DynamicBakery.dynamicBaker = arg;
        return new ItemBakedModelReplacement();
    }
}
