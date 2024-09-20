package smartin.miapi.client.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.FastColor;
import net.minecraft.util.RandomSource;
import smartin.miapi.client.model.item.BakedSingleModel;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.material.Material;
import smartin.miapi.material.MaterialProperty;

/**
 * This class is a colection of ColorUtilities
 */
@Environment(EnvType.CLIENT)
public class ColorUtil {
    private ColorUtil(){

    }

    /**
     * This mehtod recolors a BakedModel, relies on the CustomColorProvider to work
     *
     * @param originalModel the model to be recolored
     * @param color         the desired Color (Hex representation in the int
     * @return the Recolored Model
     */
    public static BakedModel recolorModel(BakedModel originalModel, int color) {
        ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();

        for (BakedQuad quad : originalModel.getQuads(null, null, RandomSource.create())) {
            builder.add(recolorBakedQuad(quad, color));
        }
        for (Direction dir : Direction.values()) {
            for (BakedQuad quad : originalModel.getQuads(null, dir, RandomSource.create())) {
                builder.add(recolorBakedQuad(quad, color));
            }
        }
        BakedSingleModel model = new BakedSingleModel(builder.build());
        model.overrideList = originalModel.getOverrides();
        return model;
    }

    public static BakedQuad recolorBakedQuad(BakedQuad originalQuad, int newColor) {
        return new BakedQuad(originalQuad.getVertices(), newColor, originalQuad.getDirection(), originalQuad.getSprite(), false);
    }

    public static int getModuleColor(ModuleInstance instance) {
        Material material = MaterialProperty.getMaterial(instance);
        if (material != null) {
            return material.getColor();
        }
        return FastColor.ARGB32.color(255, 255, 255, 255);
    }
}
