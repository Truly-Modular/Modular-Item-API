package smartin.miapi.modules.material.palette;

import com.google.gson.JsonElement;
import com.redpxnda.nucleus.util.Color;
import com.redpxnda.nucleus.util.InterfaceDispatcher;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import smartin.miapi.modules.material.Material;

import java.util.HashMap;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class MaterialRenderControllers {
    public static final Map<String, RenderControllerCreator> creators = new HashMap<>();
    public static final Map<String, FillerFunction> fillers = new HashMap<>();
    public static final InterfaceDispatcher<RenderControllerCreator> paletteCreator = InterfaceDispatcher.of(creators, "type");
    public static FillerFunction interpolateFiller;

    public static void setup() {
        interpolateFiller = (last, current, next, lX, cX, nX, placer) -> {
            for (int i = lX; i < cX; i++) {
                float delta = (i - lX) / (float) (cX - lX);
                Color col = new Color();
                last.lerp(delta, current, col);
                placer.place(col, i, 0);
            }
        };
        fillers.put("interpolate", interpolateFiller);
        fillers.put("current_to_last", (last, current, next, lX, cX, nX, placer) -> {
            for (int i = lX; i < cX; i++) {
                placer.place(current, i, 0);
            }
        });
        fillers.put("last_to_current", (last, current, next, lX, cX, nX, placer) -> {
            for (int i = lX; i < cX; i++) {
                placer.place(last, i, 0);
            }
        });
        fillers.put("current_last_shared", (last, current, next, lX, cX, nX, placer) -> {
            for (int i = lX; i < cX; i++) {
                float delta = (i - lX) / (float) (cX - lX);
                Color color = delta < 0.5 ? last : current;
                placer.place(color, i, 0);
            }
        });

        creators.put("grayscale_map", (json, material) -> new GrayscalePaletteColorer(material, json));
        creators.put("overlay_texture", (json, material) -> new SpriteOverlayer(material, json));
        creators.put("generated_palette", (json, material) -> GrayscalePaletteColorer.createForImageJson(material, json));
        creators.put("mask_palette", (json, material) -> MaskColorer.fromJson(material, json));
        /*creators.put("end_portal", (json, material) -> new MaterialRenderController() {
            @Override
            public VertexConsumer getVertexConsumer(VertexConsumerProvider vertexConsumers, Sprite originalSprite, ItemStack stack, ItemModule.ModuleInstance moduleInstance, ModelTransformationMode mode) {
                return vertexConsumers.getBuffer(RenderLayers.getBlockLayer(Blocks.END_GATEWAY.getDefaultState()));
            }

            @Override
            public Color getAverageColor() {
                return Color.BLACK;
            }
        });*/
    }

    public interface RenderControllerCreator {
        MaterialRenderController createPalette(JsonElement element, Material material);
    }

    public interface FillerFunction {
        void fill(Color last, Color current, Color next, int lastX, int currentX, int nextX, PixelPlacer placer);
    }

    public interface PixelPlacer {
        void place(Color color, int x, int y);
    }
}
