package smartin.miapi.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import smartin.miapi.Miapi;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.modules.cache.ModularItemCache;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class SpriteLoader {
    public static List<String> preLoadTexturePaths = new ArrayList<>();

    public static List<Identifier> miapiModels = new ArrayList<>();

    public static void setup() {
        ReloadEvents.START.subscribe(isClient -> ModularItemCache.discardCache());
    }

    public static void clientStart() {
        preLoadTexturePaths.add("skin");
        preLoadTexturePaths.add("gui");

        preLoadTexturePaths.forEach(preLoadTexturePath -> {
            /*
            Map<Identifier, Resource> rawTextures = MinecraftClient.getInstance().getResourceManager().findResources("textures/" + preLoadTexturePath, (identifier ->
            {
                return identifier.getNamespace().equals(Miapi.MOD_ID) && identifier.toString().endsWith(".png");
            }));
            //net.minecraft.client.texture.SpriteLoader.fromAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE).load(null);
            rawTextures.forEach((id, rawTexture) -> {
                Miapi.LOGGER.info("loading texture" + id);
                RenderSystem.setShaderTexture(0, id);
                net.minecraft.client.texture.SpriteLoader.load(id,rawTexture);
            });
             */
        });
        MinecraftClient.getInstance().getResourceManager().findAllResources(
                "models",
                (identifier -> identifier.getNamespace().equals(Miapi.MOD_ID))).forEach((identifier, resources) -> miapiModels.add(identifier));
    }

    protected static void onTextureStitch(SpriteAtlasTexture atlas, Consumer<Identifier> spriteAdder) {
        if (SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE.equals(atlas.getId())) {
            Map<Identifier, Resource> map = MinecraftClient.getInstance().getResourceManager().findResources(
                    "textures/item",
                    s -> s.getNamespace().equals(Miapi.MOD_ID));
            map.forEach((identifier, resource) -> {
                String string = identifier.toString().replace("textures/", "").replace(".png", "");
                spriteAdder.accept(new Identifier(string));
            });
        }
    }
}
