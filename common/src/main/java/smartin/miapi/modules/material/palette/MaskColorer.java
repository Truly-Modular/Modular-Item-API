package smartin.miapi.modules.material.palette;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.redpxnda.nucleus.util.Color;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.SpriteContents;
import smartin.miapi.Miapi;
import smartin.miapi.client.renderer.NativeImageGetter;
import smartin.miapi.modules.material.Material;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.HashMap;
import java.util.Map;

public class MaskColorer extends SpriteColorer {
    public Masker masker;
    public SpriteColorer base;
    public SpriteColorer layer;
    public static Map<String, Masker> maskerRegistry = new HashMap<>();

    static {
        maskerRegistry.put("texture", new SpriteMasker(null));
        maskerRegistry.put("texture_strength", new SpriteMaskerStrength(null));
    }

    public MaskColorer(Material material, SpriteColorer base, SpriteColorer layer, Masker masker) {
        super(material);
        this.base = base;
        this.layer = layer;
        this.masker = masker;
    }

    public static MaterialRenderController fromJson(Material material, JsonElement element) {
        try {
            JsonObject object = element.getAsJsonObject();
            MaterialRenderController baseColorer = MaterialRenderControllers.paletteCreator.dispatcher().createPalette(object.get("base"), material);
            MaterialRenderController layerColorer = MaterialRenderControllers.paletteCreator.dispatcher().createPalette(object.get("layer"), material);
            if (baseColorer instanceof SpriteColorer baseSpriteColor && layerColorer instanceof SpriteColorer layerSpriteColor) {
                Masker masker = getMaskerFromJson(object.get("mask"));
                return new MaskColorer(material, baseSpriteColor, layerSpriteColor, masker);
            } else {
                return baseColorer;
            }
        } catch (Exception e) {
            Miapi.LOGGER.error("Could not setup MaskPalette for " + material.getKey(), e);
        }
        return null;
    }

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
    public boolean isAnimated() {
        return base.isAnimated() || layer.isAnimated() || masker.isAnimated();
    }

    public interface Masker {
        NativeImage mask(NativeImage base, NativeImage other);

        Color average(Color base, Color other);

        Masker fromJson(JsonElement element);

        boolean isAnimated();
    }

    public static class SpriteMasker implements Masker {
        SpriteFromJson maskingSprite;

        public SpriteMasker(SpriteFromJson contents) {
            maskingSprite = contents;
        }

        @Override
        public NativeImage mask(NativeImage base, NativeImage other) {
            NativeImageGetter.ImageHolder nativeImage = maskingSprite.getNativeImage();
            NativeImage image = new NativeImage(base.getWidth(), base.getHeight(), false);
            for (int width = 0; width < base.getWidth(); width++) {
                for (int height = 0; height < base.getHeight(); height++) {
                    int baseColor = base.getColor(width, height);
                    int otherColor = other.getColor(width, height);
                    int blendColor = nativeImage.getColor(width % nativeImage.getWidth(), height % nativeImage.getHeight());
                    image.setColor(width, height, blend(baseColor, otherColor, blendColor));
                }
            }
            image.untrack();
            return image;
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
            int blendRed = ((blend) >> 24) & 0xFF;
            int blendGreen = ((blend) >> 16) & 0xFF;
            int blendBlue = ((blend) >> 8) & 0xFF;
            int blendAlpha = (blend) & 0xFF;

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
            JsonObject object = element.getAsJsonObject();
            SpriteFromJson sprite = new SpriteFromJson(object.get("sprite").getAsJsonObject());
            SpriteMasker masker = new SpriteMasker(sprite);
            return masker;
        }

        @Override
        public boolean isAnimated() {
            return maskingSprite.isAnimated();
        }
    }

    public static class SpriteMaskerStrength implements Masker {
        SpriteFromJson maskingSprite;
        float strength = 1.0F;

        public SpriteMaskerStrength(SpriteFromJson contents) {
            maskingSprite = contents;
        }

        @Override
        public NativeImage mask(NativeImage base, NativeImage other) {
            NativeImageGetter.ImageHolder nativeImage = maskingSprite.getNativeImage();
            NativeImage image = new NativeImage(base.getWidth(), base.getHeight(), false);
            for (int width = 0; width < base.getWidth(); width++) {
                for (int height = 0; height < base.getHeight(); height++) {
                    int baseColor = base.getColor(width, height);
                    int otherColor = other.getColor(width, height);
                    int blendColor = nativeImage.getColor(width % nativeImage.getWidth(), height % nativeImage.getHeight());
                    image.setColor(width, height, blend(baseColor, otherColor, blendColor));
                }
            }
            image.untrack();
            return image;
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
            int blendRed = (int) Math.min(255, strength * (blend >> 24 & 0xFF));
            int blendGreen = (int) Math.min(255, strength * (blend >> 16 & 0xFF));
            int blendBlue = (int) Math.min(255, strength * (blend >> 8 & 0xFF));
            int blendAlpha = (int) Math.min(255, strength * ((blend) & 0xFF));

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
            JsonObject object = element.getAsJsonObject();
            SpriteFromJson sprite = new SpriteFromJson(object.get("sprite").getAsJsonObject());
            SpriteMaskerStrength masker = new SpriteMaskerStrength(sprite);
            masker.strength = 1.0f / ModuleProperty.getFloat(object, "strength", 1.0f);
            return masker;
        }

        @Override
        public boolean isAnimated() {
            return maskingSprite.isAnimated();
        }
    }
}
