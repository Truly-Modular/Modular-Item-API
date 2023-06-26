package smartin.miapi.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import smartin.miapi.Miapi;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.modules.cache.ModularItemCache;

import java.util.Map;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class SpriteLoader {
    public static void setup() {
        //ClientTextureStitchEvent.PRE.register(SpriteLoader::onTextureStitch);
        //ClientTextureStitchEvent.POST.register((atlas)-> ModularItemCache.discardCache());
        ReloadEvents.START.subscribe(isClient -> ModularItemCache.discardCache());
    }

    protected static void onTextureStitch(SpriteAtlasTexture atlas, Consumer<Identifier> spriteAdder) {
        if (SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE.equals(atlas.getId())) {
            Map<Identifier, Resource> map = MinecraftClient.getInstance().getResourceManager().findResources("textures/item", s->{
                return s.getNamespace().equals(Miapi.MOD_ID);
            });
            map.forEach((identifier, resource) -> {
                String string = identifier.toString().replace("textures/","").replace(".png","");
                spriteAdder.accept(new Identifier(string));
            });
        }
    }
}
