package smartin.miapi.modules.material.palette;

import com.redpxnda.nucleus.util.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.modules.material.Material;

public class EmptyMaterialPalette extends SimpleMaterialPalette {
    public EmptyMaterialPalette(Material material) {
        super(material);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public @Nullable SpriteContents generateSpriteContents(Identifier id) {
        return null;
    }

    @Override
    public Color getAverageColor() {
        return Color.WHITE;
    }
}
