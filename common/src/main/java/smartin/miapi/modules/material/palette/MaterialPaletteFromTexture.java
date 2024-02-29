package smartin.miapi.modules.material.palette;

import com.google.gson.JsonElement;
import com.redpxnda.nucleus.util.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.resource.metadata.AnimationResourceMetadata;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.client.texture.SpriteDimensions;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.client.renderer.NativeImageGetter;
import smartin.miapi.modules.material.Material;

import java.util.*;
import java.util.function.Supplier;

public class MaterialPaletteFromTexture extends SimpleMaterialPalette {
    protected final Supplier<NativeImageGetter.ImageHolder> imageSupplier;

    @Environment(EnvType.CLIENT)
    public static MaterialPaletteFromTexture forGeneratedMaterial(Material material, ItemStack mainIngredient) {
        return new MaterialPaletteFromTexture(material, () -> {
            BakedModel itemModel = MinecraftClient.getInstance().getItemRenderer().getModel(mainIngredient, MinecraftClient.getInstance().world, null, 0);
            SpriteContents contents = itemModel.getParticleSprite().getContents();
            NativeImageGetter.ImageHolder holder = NativeImageGetter.getFromContents(contents);
            return holder;
        });
    }

    public MaterialPaletteFromTexture(Material material, Supplier<NativeImageGetter.ImageHolder> img) {
        super(material);
        this.imageSupplier = img;
        this.setSpriteId(new Identifier(Miapi.MOD_ID, "miapi_materials/" + material.getKey()));
    }

    public MaterialPaletteFromTexture(Material material, JsonElement json) {
        super(material);
        this.imageSupplier = new SpriteFromJson(json).imageSupplier;
        this.setSpriteId(new Identifier(Miapi.MOD_ID, "miapi_materials/" + material.getKey()));
    }

    public Map<Integer,Color> getColorPalette(){
        List<Color> pixels = Arrays.stream(getPixelArray())
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

    @Environment(EnvType.CLIENT)
    public @Nullable SpriteContents generateSpriteContents(Identifier id) {
        try {
            Map<Integer, Color> finalColorMap = getColorPalette();
            NativeImage image = new NativeImage(256, 1, false);
            PaletteCreators.PixelPlacer placer = (color, x, y) -> image.setColor(x, y, color.abgr());
            PaletteCreators.FillerFunction filler = PaletteCreators.fillers.getOrDefault("interpolation", PaletteCreators.interpolateFiller);

            List<Map.Entry<Integer, Color>> list = finalColorMap.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList();
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
            return new SpriteContents(id, new SpriteDimensions(256, 1), image, AnimationResourceMetadata.EMPTY);
        } catch (Exception e) {
            Miapi.LOGGER.warn("Material Palette generation for " + id + " failed.", e);
            NativeImage nativeImage = new NativeImage(256, 1, false);
            nativeImage.untrack();
            return new SpriteContents(id, new SpriteDimensions(256, 1), nativeImage, AnimationResourceMetadata.EMPTY);
        }
    }

    @Environment(EnvType.CLIENT)
    public int[] getPixelArray() {
        NativeImageGetter.ImageHolder image = imageSupplier.get();
        if (image.getFormat() != NativeImage.Format.RGBA) {
            throw new UnsupportedOperationException("can only call makePixelArray for RGBA images.");
        } else {
            int[] is = new int[image.getWidth() * image.getHeight()];

            for (int i = 0; i < image.getHeight(); ++i) {
                for (int j = 0; j < image.getWidth(); ++j) {
                    int k = image.getColor(j, i);
                    is[j + i * image.getWidth()] = ColorHelper.Argb.getArgb(ColorHelper.Abgr.getAlpha(k), ColorHelper.Abgr.getRed(k), ColorHelper.Abgr.getGreen(k), ColorHelper.Abgr.getBlue(k));
                }
            }

            return is;
        }
    }
}
