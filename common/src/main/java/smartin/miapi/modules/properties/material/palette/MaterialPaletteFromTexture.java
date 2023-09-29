package smartin.miapi.modules.properties.material.palette;

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
import smartin.miapi.mixin.client.SpriteContentsAccessor;
import smartin.miapi.modules.properties.material.Material;

import java.util.*;
import java.util.function.Supplier;

public class MaterialPaletteFromTexture extends SimpleMaterialPalette {
    Supplier<NativeImage> imageSupplier;

    @Environment(EnvType.CLIENT)
    public static MaterialPalette forGeneratedMaterial(Material material, ItemStack mainIngredient) {
        try {
            return new MaterialPaletteFromTexture(material, () -> {
                BakedModel itemModel = MinecraftClient.getInstance().getItemRenderer().getModel(mainIngredient, MinecraftClient.getInstance().world, null, 0);
                SpriteContents contents = itemModel.getParticleSprite().getContents();
                return ((SpriteContentsAccessor) contents).getImage();
            });
        } catch (Exception e) {
            Miapi.LOGGER.warn("Error during palette creation", e);
            return new EmptyMaterialPalette(material);
        }
    }

    public MaterialPaletteFromTexture(Material material, Supplier<NativeImage> img) {
        super(material);
        this.imageSupplier = img;
        this.setSpriteId(new Identifier(Miapi.MOD_ID, "miapi_materials/" + material.getKey()));
    }

    @Environment(EnvType.CLIENT)
    @Override
    public @Nullable SpriteContents generateSpriteContents(Identifier id) {
        List<Color> pixels = Arrays.stream(getPixelArray())
                .mapToObj(Color::new)
                .filter(color -> color.a() > 0)
                .sorted(Comparator.comparingDouble(a -> a.toFloatVecNoDiv().length()))
                .toList();


        PaletteCreators.FillerFunction filler = PaletteCreators.fillers.getOrDefault("interpolation", PaletteCreators.interpolateFiller);

        Map<Integer, Color> colors = new HashMap<>();

        for (int i = 0; i < pixels.size(); i++) {
            colors.put((int)((float)i / (float)pixels.size() * 255.0), pixels.get(i));
        }

        if (!colors.containsKey(0))
            colors.put(0, colors.get(0));
        if (!colors.containsKey(255))
            colors.put(255, colors.get(colors.size()-1));

        NativeImage nativeImage = new NativeImage(256, 1, false);
        PaletteCreators.PixelPlacer placer = (color, x, y) -> nativeImage.setColor(x, y, color.abgr());

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
            nativeImage.setColor(current.getKey(), 0, current.getValue().abgr());
        }
        nativeImage.untrack();
        return new SpriteContents(id, new SpriteDimensions(256, 1), nativeImage, AnimationResourceMetadata.EMPTY);
    }

    @Environment(EnvType.CLIENT)
    public int[] getPixelArray() {
        NativeImage image = imageSupplier.get();
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
