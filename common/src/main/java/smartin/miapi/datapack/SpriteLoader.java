package smartin.miapi.datapack;

import dev.architectury.event.events.client.ClientTextureStitchEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.Identifier;
import smartin.miapi.Miapi;
import smartin.miapi.item.modular.cache.ModularItemCache;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class SpriteLoader {

    public static void setup(){
        ClientTextureStitchEvent.PRE.register(SpriteLoader::onTextureStitch);
        ClientTextureStitchEvent.POST.register(atlas -> {
            ModularItemCache.discardCache();
            spritesToAdd.clear();
        });
        /*
         */

        ReloadEvent.subscribeEnd(isClient -> {
            if(isClient){
                reloadSprites();
            }
        });
    }

    public static final ArrayList<Identifier> spritesToAdd = new ArrayList<>();

    public static void reloadSprites(){
        MinecraftClient.getInstance().reloadResources();
    }

    protected static void onTextureStitch(SpriteAtlasTexture atlas, Consumer<Identifier> spriteAdder) {
        Set<Identifier> sprites = Set.copyOf(spritesToAdd);
        /*
        Set<String> uniqueSprites = new HashSet<>();
        sprites.forEach(spriteidentifier->{
            uniqueSprites.add(spriteidentifier.toString());
        });
        sprites.clear();
        uniqueSprites.forEach(spriteString->{
            sprites.add(new Identifier(spriteString));
        });
         */
        sprites.forEach(identifier -> {
            Miapi.LOGGER.error("Loading Texture TextureStitch "+identifier.toString());
            spriteAdder.accept(identifier);
        });
    }
}
