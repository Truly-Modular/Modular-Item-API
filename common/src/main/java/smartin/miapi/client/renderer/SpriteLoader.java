package smartin.miapi.client.renderer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
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

    public static List<ResourceLocation> miapiModels = new ArrayList<>();

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
        Minecraft.getInstance().getResourceManager().listResourceStacks(
                "models",
                (identifier -> identifier.getNamespace().equals(Miapi.MOD_ID))).forEach((identifier, resources) -> miapiModels.add(identifier));
    }

    protected static void onTextureStitch(TextureAtlas atlas, Consumer<ResourceLocation> spriteAdder) {
        if (TextureAtlas.LOCATION_BLOCKS.equals(atlas.location())) {
            Map<ResourceLocation, Resource> map = Minecraft.getInstance().getResourceManager().listResources(
                    "textures/item",
                    s -> s.getNamespace().equals(Miapi.MOD_ID));
            map.forEach((identifier, resource) -> {
                String string = identifier.toString().replace("textures/", "").replace(".png", "");
                spriteAdder.accept(ResourceLocation.parse(string));
            });
        }
    }
}
