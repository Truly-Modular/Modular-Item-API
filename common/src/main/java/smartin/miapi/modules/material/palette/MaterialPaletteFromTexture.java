package smartin.miapi.modules.material.palette;

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
import smartin.miapi.modules.material.Material;

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
        List<Color> colors = Arrays.stream(getPixelArray())
                .mapToObj(Color::new)
                .filter(color -> color.a() > 0)
                .distinct()
                .sorted(Comparator.comparingDouble(Color::length))
                .toList();


        NativeImage nativeImage = new NativeImage(256, 1, false);
        PaletteCreators.FillerFunction filler = PaletteCreators.fillers.getOrDefault("interpolation", PaletteCreators.interpolateFiller);
        PaletteCreators.PixelPlacer placer = (color, x, y) -> nativeImage.setColor(x, y, color.abgr());

        int spacing = 255/Math.min(colors.size()-1, 255);
        int index = 0;
        for (int i = 0; i < 256; i+=spacing) {
            boolean isLast = index == colors.size()-1;
            int x = isLast ? 255 : i;

            Map.Entry<Integer, Color> last = index == 0 ? Map.entry(0, Color.BLACK) : Map.entry(i-spacing, colors.get(index - 1));
            Map.Entry<Integer, Color> current = Map.entry(x, colors.get(index));
            Map.Entry<Integer, Color> next = isLast ? Map.entry(255, Color.WHITE) : Map.entry(i+spacing, colors.get(index + 1));

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

            index++;
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
