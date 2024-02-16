package smartin.miapi.client.atlas;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;
import smartin.miapi.Miapi;
import smartin.miapi.modules.material.Material;
import smartin.miapi.modules.material.palette.MaterialSpriteColorer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MaterialSpriteManager {
    static Map<Holder, NativeImageBackedTexture> animated_Textures = new HashMap<>();

    public static final long CACHE_SIZE = 1000;
    public static final long CACHE_LIFETIME = 10;
    public static final TimeUnit CACHE_LIFETIME_UNIT = TimeUnit.SECONDS;
    protected static final Cache<Holder, Identifier> materialSpriteCache = CacheBuilder.newBuilder()
            .maximumSize(CACHE_SIZE)
            .expireAfterAccess(CACHE_LIFETIME, CACHE_LIFETIME_UNIT)
            .removalListener(notification -> {
                if (notification.getValue() instanceof Identifier removeId) {
                    MinecraftClient.getInstance().getTextureManager().destroyTexture(removeId);
                    animated_Textures.remove(notification.getKey());
                }
            })
            .build(new CacheLoader<>() {
                @Override
                public Identifier load(Holder key) {
                    return getMaterialSprite(key);
                }
            });
    public static Identifier getMaterialSprite(Sprite oldSprite, Material material, MaterialSpriteColorer materialSpriteColorer) {
        Holder holder = new Holder(oldSprite, material, materialSpriteColorer);
        return getMaterialSprite(holder);
    }

    public static Identifier getMaterialSprite(Holder holder) {
        Identifier identifier = materialSpriteCache.getIfPresent(holder);
        if (identifier == null) {
            var colorer = holder.colorer().createSpriteManager(holder.sprite().getContents());
            NativeImageBackedTexture nativeImageBackedTexture = new NativeImageBackedTexture(colorer.recolor());
            Identifier spriteId = MinecraftClient.getInstance().getTextureManager().registerDynamicTexture("miapi/dynmaterialsprites", nativeImageBackedTexture);
            if (colorer.requireTick()) {
                animated_Textures.put(holder, nativeImageBackedTexture);
            }
            materialSpriteCache.put(holder, spriteId);
            return spriteId;
        }
        return identifier;
    }

    public static void tick() {
        animated_Textures.forEach(((holder, nativeImageBackedTexture) -> {
            nativeImageBackedTexture.setImage(holder.colorer().transform(holder.sprite().getContents()));
            nativeImageBackedTexture.upload();
        }));
    }

    public record Holder(Sprite sprite, Material material, MaterialSpriteColorer colorer) {
    }
}
