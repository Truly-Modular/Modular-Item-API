package smartin.miapi.modules.properties.material.palette;

import net.minecraft.client.texture.SpriteContents;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.modules.properties.material.Material;

public class EmptyMaterialPalette extends SimpleMaterialPalette {
    public EmptyMaterialPalette(Material material) {
        super(material);
    }

    @Override
    public @Nullable SpriteContents generateSpriteContents(Identifier id) {
        return null;
    }
}
