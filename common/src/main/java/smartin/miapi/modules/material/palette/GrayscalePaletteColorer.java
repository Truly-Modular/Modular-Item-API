package smartin.miapi.modules.material.palette;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.redpxnda.nucleus.codec.misc.MiscCodecs;
import com.redpxnda.nucleus.util.Color;
import net.minecraft.util.math.ColorHelper;
import smartin.miapi.Miapi;
import smartin.miapi.modules.material.Material;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GrayscalePaletteColorer extends SpritePixelReplacer {
    public static final Codec<Integer> stringToIntCodec = Codec.STRING.xmap(Integer::parseInt, String::valueOf);

    protected final int[] colors; // in abgr, blame mc
    protected final Color averageColor;

    public GrayscalePaletteColorer(Material material, JsonElement json) {
        super(material);

        if (json instanceof JsonObject object) {
            if (!object.has("colors"))
                throw new JsonParseException("ModularItem API failed to parse grayscale_map sampling palette for material '" + material.getKey() + "'! Missing member 'colors'.");

            JsonElement element = object.get("colors");
            Map<Integer, Color> colorsMap = new HashMap<>(MiscCodecs.quickParse(
                    element, Codec.unboundedMap(stringToIntCodec, MiscCodecs.COLOR),
                    s -> Miapi.LOGGER.error("Failed to create material palette color map from JSON '" + element + "'! -> " + s)
            ));
            String key = object.has("filler") ? object.get("filler").getAsString() : "interpolate";
            RenderControllers.FillerFunction filler = RenderControllers.fillers.getOrDefault(key, RenderControllers.interpolateFiller);
            interpolateColorsMap(colorsMap, filler);

            averageColor = createAverageColor(colorsMap);
            colors = createColorsArray(colorsMap);
        } else {
            throw new JsonParseException("ModularItem API failed to parse grayscale_map sampling palette for material '" + material.getKey() + "'! Not a JSON object -> " + json);
        }
    }

    public static void interpolateColorsMap(Map<Integer, Color> colors, RenderControllers.FillerFunction filler) {
        if (!colors.containsKey(0))
            colors.put(0, Color.BLACK);
        if (!colors.containsKey(255))
            colors.put(255, Color.WHITE);

        RenderControllers.PixelPlacer placer = (color, x, y) -> colors.put(x, color);

        List<Map.Entry<Integer, Color>> list = colors.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList();
        for (int i = 0; i < list.size(); i++) {
            Map.Entry<Integer, Color> last = i == 0 ? Map.entry(0, Color.BLACK) : list.get(i - 1);
            Map.Entry<Integer, Color> current = list.get(i);
            Map.Entry<Integer, Color> next = i == list.size() - 1 ? Map.entry(255, Color.WHITE) : list.get(i + 1);

            filler.fill(
                    last.getValue(),
                    current.getValue(),
                    next.getValue(),
                    last.getKey(),
                    current.getKey(),
                    next.getKey(),
                    placer
            );
            colors.put(current.getKey(), current.getValue());
        }
    }

    public static int[] createColorsArray(Map<Integer, Color> colors) {
        if (colors.size() != 256) throw new IllegalArgumentException("There must be 256 colors!");
        int[] array = new int[256];
        colors.forEach((pos, color) -> array[0] = color.abgr());
        return array;
    }

    public static Color createAverageColor(Map<Integer, Color> colors) {
        Color color = new Color();
        colors.forEach((pos, col) -> {
            color.x+=col.x;
            color.y+=col.y;
            color.z+=col.z;
            color.w+=col.w;
        });
        color.x/=colors.size();
        color.y/=colors.size();
        color.z/=colors.size();
        color.w/=colors.size();
        return color;
    }

    @Override
    public int getReplacementColor(int previousAbgr) {
        int red = ColorHelper.Abgr.getRed(previousAbgr);
        return colors[red];
    }

    @Override
    public Color getAverageColor() {
        return averageColor;
    }

    @Override
    public boolean isAnimated() {
        return false;
    }
}
