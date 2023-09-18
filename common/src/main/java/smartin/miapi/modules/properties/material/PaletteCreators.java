package smartin.miapi.modules.properties.material;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.redpxnda.nucleus.codec.InterfaceDispatcher;
import com.redpxnda.nucleus.codec.MiscCodecs;
import com.redpxnda.nucleus.util.Color;
import net.minecraft.client.resource.metadata.AnimationResourceMetadata;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.client.texture.SpriteDimensions;
import net.minecraft.util.Identifier;
import smartin.miapi.Miapi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PaletteCreators {
    public static final Map<String, PaletteCreator> creators = new HashMap<>();
    public static final Map<String, FillerFunction> fillers = new HashMap<>();
    public static final InterfaceDispatcher<PaletteCreator> paletteCreator = InterfaceDispatcher.of(creators, "type");
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

        /*creators.put("texture", (element, materialKey) -> {
            return null;
        });*/

        Codec<Integer> stringToIntCodec = Codec.STRING.xmap(Integer::parseInt, String::valueOf);
        creators.put("grayscale_map", (json, material) -> {
            if (json instanceof JsonObject object) {
                try {
                    if (!object.has("colors"))
                        throw new JsonParseException("ModularItem API failed to parse grayscale_map sampling palette for material '" + material + "'! Missing member 'colors'.");

                    JsonElement element = object.get("colors");
                    Map<Integer, Color> colors = new HashMap<>(MiscCodecs.quickParse(
                            element, Codec.unboundedMap(stringToIntCodec, MiscCodecs.COLOR),
                            s -> Miapi.LOGGER.error("Failed to create material palette color map from JSON '" + element + "'! -> " + s)
                    ));
                    String key = object.has("filler") ? object.get("filler").getAsString() : "interpolate";
                    FillerFunction filler = fillers.getOrDefault(key, interpolateFiller);

                    Color black = new Color(0, 0, 0, 255);
                    Color white = new Color(255, 255, 255, 255);
                    if (!colors.containsKey(0))
                        colors.put(0, black);
                    if (!colors.containsKey(255))
                        colors.put(255, white);

                    Identifier identifier = new Identifier(Miapi.MOD_ID, "textures/miapi_materials/" + material);
                    NativeImage image = new NativeImage(256, 1, false);
                    PixelPlacer placer = (color, x, y) -> image.setColor(x, y, color.abgr());

                    List<Map.Entry<Integer, Color>> list = colors.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList();
                    for (int i = 0; i < list.size(); i++) {
                        Map.Entry<Integer, Color> last = i == 0 ? Map.entry(0, black) : list.get(i - 1);
                        Map.Entry<Integer, Color> current = list.get(i);
                        Map.Entry<Integer, Color> next = i == list.size() - 1 ? Map.entry(255, white) : list.get(i + 1);

                        filler.fill(
                                last.getValue(),
                                current.getValue(),
                                next.getValue(),
                                last.getKey(),
                                current.getKey(),
                                next.getKey(),
                                placer
                        );
                        image.setColor(current.getKey(), 0, current.getValue().abgr());
                    }
                    image.untrack();

                    return new SpriteContents(identifier, new SpriteDimensions(256, 1), image, AnimationResourceMetadata.EMPTY);
                } catch (Exception e) {
                    RuntimeException runtime = new RuntimeException("Exception parsing Material " + material);
                    runtime.addSuppressed(e);
                    throw runtime;
                }

            }
            throw new JsonParseException("ModularItem API failed to parse grayscale_map sampling palette for material '" + material + "'! Not a JSON object -> " + json);
        });
    }

    public interface PaletteCreator {
        SpriteContents generateSprite(JsonElement element, String materialKey);
    }

    public interface FillerFunction {
        void fill(Color last, Color current, Color next, int lastX, int currentX, int nextX, PixelPlacer placer);
    }

    public interface PixelPlacer {
        void place(Color color, int x, int y);
    }
}
