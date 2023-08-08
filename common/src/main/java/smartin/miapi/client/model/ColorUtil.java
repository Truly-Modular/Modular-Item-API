package smartin.miapi.client.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.material.Material;
import smartin.miapi.modules.properties.material.MaterialProperty;

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

        for (BakedQuad quad : originalModel.getQuads(null, null, Random.create())) {
            builder.add(recolorBakedQuad(quad, color));
        }
        for (Direction dir : Direction.values()) {
            for (BakedQuad quad : originalModel.getQuads(null, dir, Random.create())) {
                builder.add(recolorBakedQuad(quad, color));
            }
        }
        DynamicBakedModel model = new DynamicBakedModel(builder.build());
        model.overrideList = originalModel.getOverrides();
        return model;
    }

    public static BakedQuad recolorBakedQuad(BakedQuad originalQuad, int newColor) {
        return new BakedQuad(originalQuad.getVertexData(), newColor, originalQuad.getFace(), originalQuad.getSprite(), false);
    }

    public static int getModuleColor(ItemModule.ModuleInstance instance) {
        Material material = MaterialProperty.getMaterial(instance);
        if (material != null) {
            return material.getColor();
        }
        return ColorHelper.Argb.getArgb(255, 255, 255, 255);
    }
}
