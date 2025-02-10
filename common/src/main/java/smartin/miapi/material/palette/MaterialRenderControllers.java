package smartin.miapi.material.palette;

import com.google.gson.JsonElement;
import com.redpxnda.nucleus.util.Color;
import net.minecraft.world.item.ItemDisplayContext;
import smartin.miapi.material.JsonMaterial;
import smartin.miapi.material.base.Material;
import smartin.miapi.modules.ModuleInstance;

import java.util.HashMap;
import java.util.Map;

/**
 * A manager/registry for json-representable {@link MaterialRenderController}s <br>
 * Used namely for {@link JsonMaterial#getRenderController(ModuleInstance, ItemDisplayContext)} <br>
 * Add to {@link MaterialRenderControllers#creators} to allow your {@link MaterialRenderController} to be created in json.
 */
public class MaterialRenderControllers {
    public static final Map<String, RenderControllerCreator> creators = new HashMap<>();
    public static final Map<String, FillerFunction> fillers = new HashMap<>();
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
        creators.put("image_generated", (json, material) -> GrayscalePaletteColorer.createForImageJson(material, json,false));
        creators.put("image_generated_item", (json, material) -> GrayscalePaletteColorer.createForImageJson(material, json,true));
        creators.put("from_material_palette_image", (json, material) -> PaletteAtlasBackedColorer.createColorer(material, json));
        creators.put("layered_mask", (json, material) -> MaskColorer.fromJson(material, json));
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
