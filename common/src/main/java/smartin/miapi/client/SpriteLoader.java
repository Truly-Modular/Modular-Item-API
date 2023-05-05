package smartin.miapi.client;

import dev.architectury.event.events.client.ClientTextureStitchEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import smartin.miapi.Miapi;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.item.modular.cache.ModularItemCache;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class SpriteLoader {
    public static void setup() {

        ClientTextureStitchEvent.PRE.register(SpriteLoader::onTextureStitch);
        ClientTextureStitchEvent.POST.register((atlas)->{
            ModularItemCache.discardCache();
        });
        ReloadEvents.START.subscribe(isClient -> {
            if (isClient) {
                ModularItemCache.discardCache();
            }
        });

        ReloadEvents.END.subscribe(isClient -> {
            if (isClient) {
                reloadSprites();
            }
        });
    }

    public static void reloadSprites() {
        MinecraftClient.getInstance().reloadResources();
    }

    protected static void onTextureStitch(SpriteAtlasTexture atlas, Consumer<Identifier> spriteAdder) {
        if (SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE.equals(atlas.getId())) {
            Map<Identifier, Resource> map = MinecraftClient.getInstance().getResourceManager().findResources("textures/item", s->{
                return s.getNamespace().equals(Miapi.MOD_ID);
            });
            map.forEach((identifier, resource) -> {
                Miapi.LOGGER.error("reloading");
                Miapi.LOGGER.error(identifier.toString());
                String string = identifier.toString().replace("textures/","").replace(".png","");
                spriteAdder.accept(new Identifier(string));
            });
        }
    }
}
