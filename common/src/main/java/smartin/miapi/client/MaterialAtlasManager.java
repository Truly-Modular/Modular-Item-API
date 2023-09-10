package smartin.miapi.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasHolder;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;

import static smartin.miapi.Miapi.MOD_ID;

@Environment(value= EnvType.CLIENT)
public class MaterialAtlasManager extends SpriteAtlasHolder {
    public MaterialAtlasManager(TextureManager textureManager) {
        super(textureManager, new Identifier(MOD_ID, "textures/atlas/materials.png"), new Identifier(MOD_ID, "materials"));
    }

    public Sprite getMaterialSprite(Identifier id) {
        return getSprite(id);
    }
}
