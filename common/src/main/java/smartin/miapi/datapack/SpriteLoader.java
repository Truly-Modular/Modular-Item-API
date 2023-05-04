package smartin.miapi.datapack;

import dev.architectury.event.events.client.ClientTextureStitchEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.Identifier;
import smartin.miapi.Miapi;
import smartin.miapi.item.modular.cache.ModularItemCache;

import java.util.ArrayList;
import java.util.Set;
import java.util.function.Consumer;

public class SpriteLoader {
    public static final ArrayList<Identifier> spritesToAdd = new ArrayList<>();
    public static void setup() {

        ClientTextureStitchEvent.PRE.register(SpriteLoader::onTextureStitch);
        ClientTextureStitchEvent.POST.register((atlas)->{
            ModularItemCache.discardCache();
        });
        ReloadEvents.START.subscribe(isClient -> {
            if (isClient) {
                ModularItemCache.discardCache();
                spritesToAdd.clear();
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
        Set<Identifier> sprites = Set.copyOf(spritesToAdd);
        sprites.forEach(identifier -> {
            //Miapi.LOGGER.error("Loading Texture TextureStitch "+identifier.toString());
            spriteAdder.accept(identifier);
        });
    }
}
