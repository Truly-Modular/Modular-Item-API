package smartin.miapi.client.model;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import smartin.miapi.item.modular.ItemModule;

public class ColorUtil {
    public static BakedModel recolorModel(BakedModel originalModel, int color) {
        ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();

        for (BakedQuad quad : originalModel.getQuads(null, null, Random.create())) {
            builder.add(recolorBakedQuad(quad,color));
        }
        for (Direction dir : Direction.values()) {
            for (BakedQuad quad : originalModel.getQuads(null, dir, Random.create())) {
                builder.add(recolorBakedQuad(quad,color));
            }
        }
        return new DynamicBakedModel(builder.build());
    }

    private static BakedQuad recolorBakedQuad(BakedQuad originalQuad, int newColor) {
        //hacky way but setting the ColorIndex to the desired Color and then Using a CustomColorProvider to set this color seems to work perfectly
        return new BakedQuad(originalQuad.getVertexData(), newColor, originalQuad.getFace(), originalQuad.getSprite(), false);
    }

    private static int modifyColor(int color, int newColor) {
        int alpha = (color >> 24) & 0xFF;
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;
        int newRed = (newColor >> 16) & 0xFF;
        int newGreen = (newColor >> 8) & 0xFF;
        int newBlue = newColor & 0xFF;

        // Modify the red, green, and blue components of the vertex color
        red = (red * newRed) / 255;
        green = (green * newGreen) / 255;
        blue = (blue * newBlue) / 255;

        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    public static int getModuleColor(ItemModule.ModuleInstance instance){
        ColorHelper.Argb.getArgb(255,0,255,0);
        return ColorHelper.Argb.getArgb(255,0,255,0);
    }

}
