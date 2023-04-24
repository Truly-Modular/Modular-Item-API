package smartin.miapi.client.model;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import smartin.miapi.Miapi;
import smartin.miapi.item.modular.ItemModule;
import smartin.miapi.item.modular.properties.MaterialProperty;

/**
 * This class is a colection of ColorUtilities
 */
public class ColorUtil {

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
        return new DynamicBakedModel(builder.build());
    }

    private static BakedQuad recolorBakedQuad(BakedQuad originalQuad, int newColor) {
        //hacky way but setting the ColorIndex to the desired Color and then Using a CustomColorProvider to set this color seems to work perfectly
        return new BakedQuad(originalQuad.getVertexData(), newColor, originalQuad.getFace(), originalQuad.getSprite(), false);
    }

    public static int getModuleColor(ItemModule.ModuleInstance instance) {
        MaterialProperty.Material material = MaterialProperty.getMaterial(instance);
        if (material != null) {
            return material.getColor();
        }
        return ColorHelper.Argb.getArgb(255, 255, 255, 255);
    }
}
