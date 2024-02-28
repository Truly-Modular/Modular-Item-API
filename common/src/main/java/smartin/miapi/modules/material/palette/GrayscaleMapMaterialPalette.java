package smartin.miapi.modules.material.palette;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.redpxnda.nucleus.codec.misc.MiscCodecs;
import com.redpxnda.nucleus.util.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.NativeImage;
import smartin.miapi.Miapi;
import smartin.miapi.modules.material.Material;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GrayscaleMapMaterialPalette extends MaterialPalette {
    public static final Codec<Integer> stringToIntCodec = Codec.STRING.xmap(Integer::parseInt, String::valueOf);
    protected final PaletteCreators.FillerFunction filler;
    protected final Map<Integer, Color> colors;

    public GrayscaleMapMaterialPalette(Material material, JsonElement json) {
        super(material);

        if (json instanceof JsonObject object) {
            if (!object.has("colors"))
                throw new JsonParseException("ModularItem API failed to parse grayscale_map sampling palette for material '" + material.getKey() + "'! Missing member 'colors'.");

            JsonElement element = object.get("colors");
            colors = new HashMap<>(MiscCodecs.quickParse(
                    element, Codec.unboundedMap(stringToIntCodec, MiscCodecs.COLOR),
                    s -> Miapi.LOGGER.error("Failed to create material palette color map from JSON '" + element + "'! -> " + s)
            ));
            String key = object.has("filler") ? object.get("filler").getAsString() : "interpolate";
            filler = PaletteCreators.fillers.getOrDefault(key, PaletteCreators.interpolateFiller);
        } else {
            throw new JsonParseException("ModularItem API failed to parse grayscale_map sampling palette for material '" + material.getKey() + "'! Not a JSON object -> " + json);
        }
        this.materialPalette = generateSpriteContents();
    }

    @Environment(EnvType.CLIENT)
    public NativeImage generateSpriteContents() {
        try {
            if (!colors.containsKey(0))
                colors.put(0, Color.BLACK);
            if (!colors.containsKey(255))
                colors.put(255, Color.WHITE);

            NativeImage image = new NativeImage(256, 1, false);
            PaletteCreators.PixelPlacer placer = (color, x, y) -> image.setColor(x, y, color.abgr());

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
                image.setColor(current.getKey(), 0, current.getValue().abgr());
            }
            image.untrack();
            return image;
        } catch (Exception e) {
            RuntimeException runtime = new RuntimeException("Exception whilst generating grayscale map material palette for: " + material.getKey());
            runtime.addSuppressed(e);
            throw runtime;
        }
    }
}
