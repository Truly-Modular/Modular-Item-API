package smartin.miapi.modules.properties.material.palette;

import com.redpxnda.nucleus.util.Color;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.modules.properties.material.Material;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MaterialPaletteFromTexture extends SimpleMaterialPalette {
    NativeImage image;

    public MaterialPaletteFromTexture(Material material, NativeImage img) {
        super(material);
        this.image = img;
    }

    @Override
    public @Nullable SpriteContents generateSpriteContents(Identifier id) {
        List<Color> pixels = Arrays.stream(getPixelArray())
                .mapToObj(colorInt -> new Color(colorInt))
                .filter(color -> color.a() > 0)
                .sorted((a, b) -> (int) (a.toFloatVecNoDiv().length() - b.toFloatVecNoDiv().length())).collect(Collectors.toList());

        PaletteCreators.FillerFunction filler = PaletteCreators.fillers.getOrDefault("interpolation", PaletteCreators.interpolateFiller);

        Map<Integer, Color> colors = new HashMap<>();

        for (int i = 0; i < pixels.size(); i++) {
            colors.put(i / pixels.size() * 250 + 3, pixels.get(i));
        }

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
        return null;
    }

    public int[] getPixelArray() {
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
