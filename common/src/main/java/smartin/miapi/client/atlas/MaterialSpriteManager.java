package smartin.miapi.client.atlas;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;
import smartin.miapi.Miapi;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.modules.material.Material;
import smartin.miapi.modules.material.palette.SpriteColorer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.IntUnaryOperator;

public class MaterialSpriteManager {
    static Map<Holder, NativeImageBackedTexture> animated_Textures = new HashMap<>();

    public static final long CACHE_SIZE = 1000;
    public static final long CACHE_LIFETIME = 10;
    public static final TimeUnit CACHE_LIFETIME_UNIT = TimeUnit.SECONDS;
    protected static final Cache<Holder, Identifier> materialSpriteCache = CacheBuilder.newBuilder()
            .maximumSize(CACHE_SIZE)
            .expireAfterAccess(CACHE_LIFETIME, CACHE_LIFETIME_UNIT)
            .removalListener(notification -> {
                if (notification.wasEvicted()) {
                    if (notification.getValue() instanceof Identifier removeId) {
                        MinecraftClient.getInstance().getTextureManager().destroyTexture(removeId);
                    }
                    if (notification.getKey() instanceof Holder holder) {
                        animated_Textures.remove(holder);
                        Miapi.LOGGER.info("actual removal cache " + getCacheSize() + " " + notification.getCause());
                        try {
                            holder.colorer.close();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            })
            .build(new CacheLoader<>() {
                @Override
                public Identifier load(Holder key) {
                    return getMaterialSprite(key);
                }
            });

    public static Identifier getMaterialSprite(Sprite oldSprite, Material material, SpriteColorer materialSpriteColorer) {
        Holder holder = new Holder(oldSprite, material, materialSpriteColorer);
        return getMaterialSprite(holder);
    }

    public static Identifier getMaterialSprite(Holder holder) {
        Identifier identifier = materialSpriteCache.getIfPresent(holder);
        if (identifier == null) {
            var colorer = holder.colorer().createSpriteManager(holder.sprite().getContents());
            //important!
            //the MaskColorer is responsible for managing any NativeImage it creates.
            //BUT the NativeBackedTexture removes its old uploaded NativeImage, so we need to upload a copy
            NativeImageBackedTexture nativeImageBackedTexture = new NativeImageBackedTexture(colorer.recolor().applyToCopy(IntUnaryOperator.identity()));
            Identifier spriteId = MinecraftClient.getInstance().getTextureManager().registerDynamicTexture("miapi/dynmaterialsprites", nativeImageBackedTexture);
            if (colorer.requireTick()) {
                animated_Textures.put(holder, nativeImageBackedTexture);
            }
            materialSpriteCache.put(holder, spriteId);
            return spriteId;
        }
        return identifier;
    }

    public static void clear() {
        materialSpriteCache.invalidateAll();
    }

    public static void tick() {
        if (!ReloadEvents.isInReload()) {
            List<Holder> toRemove = new ArrayList<>();
            animated_Textures.forEach(((holder, nativeImageBackedTexture) -> {
                try {
                    holder.colorer.tick((nativeImage) -> {
                        //important!
                        //the MaskColorer is responsible for managing any NativeImage it creates.
                        //BUT the NativeBackedTexture removes its old uploaded NativeImage, so we need to upload a copy
                        nativeImageBackedTexture.getImage().copyFrom(nativeImage);
                        nativeImageBackedTexture.upload();
                    }, holder.sprite().getContents());
                } catch (Exception e) {
                    toRemove.add(holder);
                }
            }));
            toRemove.forEach(materialSpriteCache::invalidate);
        }
    }

    public static int getCacheSize() {
        return (int) materialSpriteCache.size();
    }

    public record Holder(Sprite sprite, Material material, SpriteColorer colorer) {
    }
}
