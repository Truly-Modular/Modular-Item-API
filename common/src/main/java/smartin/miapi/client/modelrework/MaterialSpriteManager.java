package smartin.miapi.client.modelrework;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;
import smartin.miapi.client.MaterialAtlasManager;
import smartin.miapi.client.MiapiClient;
import smartin.miapi.mixin.client.SpriteContentsAccessor;
import smartin.miapi.modules.material.Material;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MaterialSpriteManager {
    static Map<Holder, NativeImageBackedTexture> animated_Textures = new HashMap<>();

    public static final long CACHE_SIZE = 1000;
    public static final long CACHE_LIFETIME = 2;
    public static final TimeUnit CACHE_LIFETIME_UNIT = TimeUnit.MINUTES;
    protected static final Cache<Holder, Identifier> materialSpriteCache = CacheBuilder.newBuilder()
            .maximumSize(CACHE_SIZE)
            .expireAfterAccess(CACHE_LIFETIME, CACHE_LIFETIME_UNIT)
            .removalListener(notification -> {
                if (notification.getValue() instanceof Identifier removeId) {
                    MinecraftClient.getInstance().getTextureManager().destroyTexture(removeId);
                }
            })
            .build(new CacheLoader<>() {
                @Override
                public Identifier load(Holder key) throws Exception {
                    NativeImageBackedTexture nativeImageBackedTexture = new NativeImageBackedTexture(transform(key.sprite(), key.material()));
                    Identifier spriteId = MinecraftClient.getInstance().getTextureManager().registerDynamicTexture("miapi/dynmaterialsprites", nativeImageBackedTexture);
                    Sprite materialSprite = MiapiClient.materialAtlasManager.getMaterialSprite(key.material().getPalette().getSpriteId());
                    if (key.sprite().createAnimation() != null || materialSprite != null && materialSprite.createAnimation() != null) {
                        animated_Textures.put(key, nativeImageBackedTexture);
                    }
                    return spriteId;
                }
            });

    public static Identifier getMaterialSprite(Sprite oldSprite, Material material) {
        Holder holder = new Holder(oldSprite, material);
        Identifier identifier = materialSpriteCache.getIfPresent(holder);
        if (identifier == null) {
            NativeImageBackedTexture nativeImageBackedTexture = new NativeImageBackedTexture(transform(oldSprite, holder.material()));
            Identifier spriteId = MinecraftClient.getInstance().getTextureManager().registerDynamicTexture("miapi/dynmaterialsprites", nativeImageBackedTexture);
            Sprite materialSprite = MiapiClient.materialAtlasManager.getMaterialSprite(holder.material().getPalette().getSpriteId());
            if (oldSprite.createAnimation() != null || materialSprite != null && materialSprite.createAnimation() != null) {
                animated_Textures.put(holder, nativeImageBackedTexture);
            }
            materialSpriteCache.put(holder, spriteId);
            return spriteId;
        }
        return identifier;
    }

    public static void tick() {
        animated_Textures.forEach(((holder, nativeImageBackedTexture) -> {
            nativeImageBackedTexture.setImage(transform(holder.sprite(), holder.material()));
            nativeImageBackedTexture.upload();
        }));
    }

    public static NativeImage transform(Sprite oldSprite, Material material) {
        NativeImage rawImage = ((SpriteContentsAccessor) oldSprite.getContents()).getImage();
        NativeImage image = new NativeImage(oldSprite.getContents().getWidth(), oldSprite.getContents().getHeight(), true);
        for (int x = 0; x < rawImage.getWidth(); x++) {
            for (int y = 0; y < rawImage.getHeight(); y++) {
                rawImage.getColor(x, y);
                if (rawImage.getOpacity(x, y) < 5 && rawImage.getOpacity(x, y) > -1) {
                    image.setColor(x, y, 0);
                } else {
                    if (material != null) {
                        int unsignedInt = rawImage.getRed(x, y) & 0xFF;
                        image.setColor(x, y, getColor(material, unsignedInt));
                    } else {
                        image.setColor(x, y, rawImage.getColor(x, y));
                    }
                    //Miapi.LOGGER.info(String.valueOf(rawImage.getRed(x, y)));
                }
                //image.setColor(x, y, 1);
            }
        }
        image.untrack();
        return image;
    }

    private static int getColor(Material material, int color) {
        Sprite sprite = MiapiClient.materialAtlasManager.getMaterialSprite(material.getPalette().getSpriteId());
        if (sprite == null) {
            sprite = MiapiClient.materialAtlasManager.getMaterialSprite(MaterialAtlasManager.BASE_MATERIAL_ID);
        }
        return ((SpriteContentsAccessor) sprite.getContents()).getImage().getColor(Math.max(Math.min(color, 255), 0), 0);
    }

    public record Holder(Sprite sprite, Material material) {
    }
}
