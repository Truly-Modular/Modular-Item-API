package smartin.miapi.material.palette;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.serialization.Codec;
import com.redpxnda.nucleus.codec.misc.MiscCodecs;
import com.redpxnda.nucleus.util.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.client.renderer.NativeImageGetter;
import smartin.miapi.material.Material;

import java.util.*;

/**
 * Represents a simple palette for a material, where the index of a color in a set of 256 represents the grayscale pixel to replace with said color.
 * Essentially, (index 5 -> #F8A334) means replace #050505 with #F8A334 in the module texture.
 * The red channel is used, meaning that even non-grayscale pixels will be treated as such.
 * There are several helper methods for things like generated materials.
 */
public class GrayscalePaletteColorer extends SpritePixelReplacer {
    public static final Codec<Integer> stringToIntCodec = Codec.STRING.xmap(Integer::parseInt, String::valueOf);

    protected final int[] colors; // in abgr, blame mc
    protected final Color averageColor;

    /**
     * Create a GrayscalePaletteColorer from a sprite(or rather, the json representing it)
     */
    public static GrayscalePaletteColorer createForImageJson(Material material, JsonElement json, boolean isItem) {
        if (isItem) {
            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(json.getAsJsonObject().get("item").getAsString()));
            return createForGeneratedMaterial(material, item.getDefaultInstance());
        }
        return new GrayscalePaletteColorer(material, createImagePalette(new SpriteFromJson(json).imageSupplier.get()));
    }

    /**
     * Create a GrayscalePaletteColorer for a generated material
     */
    public static GrayscalePaletteColorer createForGeneratedMaterial(Material material, ItemStack mainIngredient) {
        BakedModel itemModel = Minecraft.getInstance().getItemRenderer().getModel(mainIngredient, Minecraft.getInstance().level, null, 0);
        SpriteContents contents = itemModel.getParticleIcon().contents();
        NativeImageGetter.ImageHolder image = NativeImageGetter.getFromContents(contents);
        return new GrayscalePaletteColorer(material, createImagePalette(image));
    }

    public GrayscalePaletteColorer(Material material, Map<Integer, Color> colorsMap) {
        super(material);
        interpolateColorsMap(colorsMap, MaterialRenderControllers.interpolateFiller);
        averageColor = createAverageColor(colorsMap);
        colors = createColorsArray(colorsMap);
    }

    /**
     * Constructs a GrayscalePaletteColorer from the normal json format- an object with: <br>
     * - a field called "colors" acting as map of grayscale value(0-255) to color hex <br>
     * - a field called "filler", determining how to fill in empty space <br>
     * Ex.
     * <pre>
     *     "colors": {
     *       "24": "181818",
     *       "68": "444444",
     *       "107": "6b6b6b",
     *       "150": "969696",
     *       "190": "bebebe",
     *       "216": "d8d8d8",
     *       "255": "ffffff"
     *     },
     *     "filler": "interpolate"
     * </pre>
     */
    public GrayscalePaletteColorer(Material material, JsonElement json) {
        super(material);

        if (json instanceof JsonObject object) {
            if (!object.has("colors"))
                throw new JsonParseException("ModularItem API failed to parse grayscale_map sampling palette for material '" + material.getID() + "'! Missing member 'colors'.");

            JsonElement element = object.get("colors");
            Map<Integer, Color> colorsMap = new HashMap<>(MiscCodecs.quickParse(
                    element, Codec.unboundedMap(stringToIntCodec, MiscCodecs.COLOR),
                    s -> new RuntimeException("Failed to create material palette color map from JSON '" + element + "'! -> " + s)
            ));
            String key = object.has("filler") ? object.get("filler").getAsString() : "interpolate";
            MaterialRenderControllers.FillerFunction filler = MaterialRenderControllers.fillers.getOrDefault(key, MaterialRenderControllers.interpolateFiller);
            interpolateColorsMap(colorsMap, filler);

            averageColor = createAverageColor(colorsMap);
            colors = createColorsArray(colorsMap);
        } else {
            throw new JsonParseException("ModularItem API failed to parse grayscale_map sampling palette for material '" + material.getID() + "'! Not a JSON object -> " + json);
        }
    }

    /**
     * Returns an array representing the color at each position(grayscale value) from 0-255
     */
    public int[] getColors() {
        return colors;
    }

    @Override
    public int getReplacementColor(int pixelX, int pixelY, int previousAbgr) {
        int red = FastColor.ABGR32.red(previousAbgr);
        return colors[red];
    }

    @Override
    public Color getAverageColor() {
        return averageColor;
    }

    @Override
    public boolean doTick() {
        return false;
    }

    /**
     * Uses the filler function to fill in the empty space in the colors map
     */
    public static void interpolateColorsMap(Map<Integer, Color> colors, MaterialRenderControllers.FillerFunction filler) {
        colors.putIfAbsent(0, Color.BLACK);
        colors.putIfAbsent(255, Color.WHITE);

        MaterialRenderControllers.PixelPlacer placer = (color, x, y) -> colors.put(x, color);

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

    /**
     * Creates the color array from the map of colors
     */
    public static int[] createColorsArray(Map<Integer, Color> colors) {
        if (colors.size() != 256) throw new IllegalArgumentException("There must be 256 colors!");
        int[] array = new int[256];
        colors.forEach((pos, color) -> array[pos] = color.abgr());
        return array;
    }

    /**
     * Finds the average color in the map of colors
     */
    public static Color createAverageColor(Map<Integer, Color> colors) {
        Color color = new Color();
        colors.forEach((pos, col) -> {
            color.x += col.x;
            color.y += col.y;
            color.z += col.z;
            color.w += col.w;
        });
        color.x /= colors.size();
        color.y /= colors.size();
        color.z /= colors.size();
        color.w /= colors.size();
        return color;
    }

    /**
     * Turns a native image into an array of pixels
     */
    public static int[] createImagePixelArray(NativeImageGetter.ImageHolder image) {
        if (image.getFormat() != NativeImage.Format.RGBA) {
            throw new UnsupportedOperationException("can only call makePixelArray for RGBA images.");
        } else {
            int[] is = new int[image.getWidth() * image.getHeight()];

            for (int i = 0; i < image.getHeight(); ++i) {
                for (int j = 0; j < image.getWidth(); ++j) {
                    int k = image.getColor(j, i);
                    is[j + i * image.getWidth()] = FastColor.ARGB32.color(FastColor.ABGR32.alpha(k), FastColor.ABGR32.red(k), FastColor.ABGR32.green(k), FastColor.ABGR32.blue(k));
                }
            }

            return is;
        }
    }

    /**
     * Scans a native image to intelligently create a palette from it- colors are extracted from the image and distanced from each other based on their weight
     */
    public static Map<Integer, Color> createImagePalette(NativeImageGetter.ImageHolder image) {
        List<Color> pixels = Arrays.stream(createImagePixelArray(image))
                .mapToObj(Color::new)
                .filter(color -> color.a() > 5)
                .sorted(Comparator.comparingDouble((a) -> a.toFloatVecNoDiv().length()))
                .toList();
        Map<Integer, Color> finalColorMap = new LinkedHashMap<>();
        List<Color> uniqueColors = pixels.stream().distinct().toList();
        for (int i = 0; i < uniqueColors.size() - 1; i++) {
            float scale = 256.0f / (float) pixels.size();
            Color current = uniqueColors.get(i);

            float distanceCurrent = Math.max(1, pixels.lastIndexOf(current) - pixels.indexOf(current));

            int medianScaled = (int) (pixels.indexOf(current) + distanceCurrent / 2);
            finalColorMap.put((int) ((medianScaled) * scale), current);

            Color next = uniqueColors.get(i + 1);
            float distanceNext = Math.max(1, pixels.lastIndexOf(next) - pixels.indexOf(next));
            //float weight = Math.min(1, Math.max(0, Math.min(distanceCurrent / distanceNext, 1 - distanceNext / distanceCurrent)));
            float weight = Math.min(1, Math.max(0, distanceCurrent / (distanceCurrent + distanceNext)));
            Color weightedAverage = new Color(
                    current.redAsFloat() * weight + next.redAsFloat() * (1 - weight),
                    current.greenAsFloat() * weight + next.greenAsFloat() * (1 - weight),
                    current.blueAsFloat() * weight + next.blueAsFloat() * (1 - weight),
                    current.alphaAsFloat() * weight + next.alphaAsFloat() * (1 - weight));
            float weightedPos = pixels.lastIndexOf(current) + (pixels.lastIndexOf(current) - pixels.indexOf(next)) * 0.5f;
            finalColorMap.put((int) (weightedPos * scale), weightedAverage);
        }

        finalColorMap.putIfAbsent(0, Color.BLACK);
        finalColorMap.putIfAbsent(255, uniqueColors.get(uniqueColors.size() - 1));

        return finalColorMap;
    }
}
