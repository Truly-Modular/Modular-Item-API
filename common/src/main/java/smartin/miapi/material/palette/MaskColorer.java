package smartin.miapi.material.palette;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.NativeImage;
import com.redpxnda.nucleus.util.Color;
import smartin.miapi.Miapi;
import smartin.miapi.client.renderer.NativeImageGetter;
import smartin.miapi.material.base.Material;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.renderer.texture.SpriteContents;

/**
 * Colors module sprites with a base colorer, a masker, and a layered colorer.
 * First, the base colorer is applied. Then using the masker, the layered colorer will be applied on top, wherever the mask covers.
 * This is comparable to Photoshop masking.
 * <br>
 * The most common masker uses an image to blend the base and layered pixels by each respective pixel in the image.
 */
public class MaskColorer extends SpriteColorer {
    public static Map<String, Masker> maskerRegistry = new HashMap<>();
    static {
        maskerRegistry.put("texture", new SpriteMasker(null));
    }

    public Masker masker;
    public SpriteColorer base;
    public SpriteColorer layer;

    public MaskColorer(Material material, SpriteColorer base, SpriteColorer layer, Masker masker) {
        super(material);
        this.base = base;
        this.layer = layer;
        this.masker = masker;
    }

    /**
     * Attempts to create a MaskColorer from its json format.
     * If either the base colorer or the layer colorer are not SpriteColorers, it will return the base colorer instead. <br>
     * Example json:
     * <pre>
     * "base": { // the base color palette... see {@link MaterialRenderControllers}
     *      "type": "grayscale_map",
     *      "colors": {
     *          "24": "2D0500",
     *          "68": "4A0800",
     *          "107": "720C00",
     *          "150": "720C00",
     *          "190": "BB2008",
     *          "255": "E32008"
     *      }
     *  },
     *  "layer": { // the layer color palette
     *      "type": "grayscale_map",
     *      "colors": {
     *          "24": "002d00",
     *          "68": "005300",
     *          "107": "007b18",
     *          "150": "009529",
     *          "190": "00aa2c",
     *          "216": "17dd62",
     *          "255": "41f384"
     *      }
     *  },
     *  "mask": { // the masker... see {@link MaskColorer#maskerRegistry}
     *      "type": "texture",
     *      "atlas": "block",
     *      "texture": "minecraft:block/water_still"
     *  }
     * </pre>
     */
    public static MaterialRenderController fromJson(Material material, JsonElement element) {
        try {
            JsonObject object = element.getAsJsonObject();
            JsonElement baseElement = object.get("base");
            MaterialRenderController baseColorer = MaterialRenderControllers.creators.get(baseElement.getAsJsonObject().get("type").getAsString()).createPalette(baseElement,material);
            JsonElement layerElement = object.get("layer");
            MaterialRenderController layerColorer = MaterialRenderControllers.creators.get(layerElement.getAsJsonObject().get("type").getAsString()).createPalette(layerElement,material);

            if (baseColorer instanceof SpriteColorer baseSpriteColor && layerColorer instanceof SpriteColorer layerSpriteColor) {
                Masker masker = getMaskerFromJson(object.get("mask"));
                return new MaskColorer(material, baseSpriteColor, layerSpriteColor, masker);
            } else {
                return baseColorer;
            }
        } catch (Exception e) {
            Miapi.LOGGER.error("Could not setup MaskPalette for " + material.getID(), e);
        }
        return null;
    }

    /**
     * Retrieves a Masker from the given json element... see {@link MaskColorer#maskerRegistry}
     */
    public static Masker getMaskerFromJson(JsonElement element) {
        JsonObject object = element.getAsJsonObject();
        String type = object.get("type").getAsString();
        return maskerRegistry.get(type).fromJson(element);
    }

    @Override
    public Color getAverageColor() {
        return masker.average(base.getAverageColor(), layer.getAverageColor());
    }

    @Override
    public NativeImage transform(SpriteContents originalSprite) {
        return masker.mask(base.transform(originalSprite), layer.transform(originalSprite));
    }

    @Override
    public boolean doTick() {
        return base.doTick() || layer.doTick() || masker.isAnimated();
    }

    @Override
    public void close() throws IOException {
        masker.close();
        base.close();
        layer.close();
    }

    public interface Masker extends  Closeable{
        /**
         * @param base the image created from the base colorer
         * @param other the image created from the layer colorer
         * @return the result of this masker
         */
        NativeImage mask(NativeImage base, NativeImage other);

        /**
         * @param base the average color of the base colorer
         * @param other the average color of the layer colorer
         * @return the new average color that this mask colorer should use
         */
        Color average(Color base, Color other);

        /**
         * Parse json to create a functional instance of a masker... see {@link MaskColorer#maskerRegistry}
         */
        Masker fromJson(JsonElement element);

        /**
         * Whether this masker should force an update on tick
         */
        boolean isAnimated();
    }

    public static class SpriteMasker implements Masker, Closeable {
        SpriteFromJson maskingSprite;

        public SpriteMasker(SpriteFromJson contents) {
            maskingSprite = contents;
        }

        public NativeImage lastImage = null;

        @Override
        public NativeImage mask(NativeImage base, NativeImage other) {
            NativeImageGetter.ImageHolder nativeImage = maskingSprite.getNativeImage();
            if (lastImage == null) {
                lastImage = new NativeImage(base.getWidth(), base.getHeight(), true);
                lastImage.untrack();
            }
            if(lastImage!=null && (lastImage.getHeight()!= base.getHeight() || lastImage.getWidth()!= base.getWidth())){
                lastImage.close();
                lastImage = new NativeImage(base.getWidth(), base.getHeight(), true);
                lastImage.untrack();
            }
            for (int width = 0; width < base.getWidth(); width++) {
                for (int height = 0; height < base.getHeight(); height++) {
                    int baseColor = base.getPixelRGBA(width, height);
                    int otherColor = other.getPixelRGBA(width, height);
                    int blendColor = nativeImage.getColor(width % nativeImage.getWidth(), height % nativeImage.getHeight());
                    lastImage.setPixelRGBA(width, height, blend(baseColor, otherColor, blendColor));
                }
            }
            if(maskingSprite!=null){
                maskingSprite.markUse();
            }
            return lastImage;
        }

        public int blend(int base, int other, int blend) {
            // Extracting the individual components from the packed integers
            int baseRed = (base >> 24) & 0xFF;
            int baseGreen = (base >> 16) & 0xFF;
            int baseBlue = (base >> 8) & 0xFF;
            int baseAlpha = base & 0xFF;

            int otherRed = (other >> 24) & 0xFF;
            int otherGreen = (other >> 16) & 0xFF;
            int otherBlue = (other >> 8) & 0xFF;
            int otherAlpha = other & 0xFF;

            // Extracting the individual components from the blend integer
            int blendRed = (blend >> 24) & 0xFF;
            int blendGreen = (blend >> 16) & 0xFF;
            int blendBlue = (blend >> 8) & 0xFF;
            int blendAlpha = blend & 0xFF;

            // Calculate blended components
            int blendedRed = (blendRed * otherRed + (255 - blendRed) * baseRed) / 255;
            int blendedGreen = (blendGreen * otherGreen + (255 - blendGreen) * baseGreen) / 255;
            int blendedBlue = (blendBlue * otherBlue + (255 - blendBlue) * baseBlue) / 255;
            int blendedAlpha = (blendAlpha * otherAlpha + (255 - blendAlpha) * baseAlpha) / 255;

            // Pack the blended components into a single integer
            return (blendedRed << 24) | (blendedGreen << 16) | (blendedBlue << 8) | blendedAlpha;
        }

        @Override
        public Color average(Color base, Color other) {
            return base;
        }

        @Override
        public Masker fromJson(JsonElement element) {
            SpriteFromJson sprite = new SpriteFromJson(element);
            return new SpriteMasker(sprite);
        }

        @Override
        public boolean isAnimated() {
            return maskingSprite.isAnimated();
        }

        @Override
        public void close() throws IOException {
            if(lastImage!=null){
                lastImage.close();
                lastImage = null;
            }
        }
    }
}
