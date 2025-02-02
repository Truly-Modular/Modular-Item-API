package smartin.miapi.client.atlas;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.client.MiapiClient;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.material.base.Material;
import smartin.miapi.material.palette.SpriteColorer;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.IntUnaryOperator;

public class MaterialSpriteManager {
    static Map<Holder, DynamicTexture> animated_Textures = new HashMap<>();

    public static final long CACHE_SIZE = 10000;
    public static final long CACHE_LIFETIME = 10;
    public static final TimeUnit CACHE_LIFETIME_UNIT = TimeUnit.SECONDS;
    protected static Map<ResourceLocation, DynamicTexture> nativeImageBackedTextureMap = new HashMap<>();
    public static Set<TextureAtlasSprite> animated = new HashSet<>();
    //WARNING!! only access anything related to colorer ONLY from the RENDER THREAD!
    protected static final Cache<Holder, ResourceLocation> materialSpriteCache = CacheBuilder.newBuilder()
            .maximumSize(CACHE_SIZE)
            .expireAfterAccess(CACHE_LIFETIME, CACHE_LIFETIME_UNIT)
            .removalListener(notification -> {
                if (notification.wasEvicted()) {
                    if (notification.getValue() instanceof ResourceLocation removeId) {
                        DynamicTexture texture = nativeImageBackedTextureMap.get(removeId);
                        if (texture != null) {
                            texture.close();
                        }
                        Minecraft.getInstance().getTextureManager().release(removeId);
                    }
                    if (notification.getKey() instanceof Holder holder) {
                        //the NativeImage should already be closed by the code above, this just kept track of the NativeImageBackedTexture to animate it
                        animated_Textures.remove(holder);
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
                public ResourceLocation load(Holder key) {
                    return getMaterialSprite(key);
                }
            });

    public static ResourceLocation getMaterialSprite(TextureAtlasSprite oldSprite, Material material, SpriteColorer materialSpriteColorer) {
        Holder holder = new Holder(oldSprite, material, materialSpriteColorer);
        return getMaterialSprite(holder);
    }

    public static ResourceLocation getMaterialSprite(Holder holder) {
        ResourceLocation identifier = materialSpriteCache.getIfPresent(holder);
        if (identifier == null) {
            var colorer = holder.colorer().createSpriteManager(holder.sprite().contents());
            //important!
            //the MaskColorer is responsible for managing any NativeImage it creates.
            //BUT the NativeBackedTexture removes its old uploaded NativeImage, so we need to upload a copy
            DynamicTexture nativeImageBackedTexture = new DynamicTexture(colorer.recolor().mappedCopy(IntUnaryOperator.identity()));
            ResourceLocation spriteId = Minecraft.getInstance().getTextureManager().register("miapi/dynmaterialsprites", nativeImageBackedTexture);
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
                        nativeImageBackedTexture.getPixels().copyFrom(nativeImage);
                        nativeImageBackedTexture.upload();
                    }, holder.sprite().contents());
                } catch (Exception e) {
                    toRemove.add(holder);
                }
            }));
            toRemove.forEach(materialSpriteCache::invalidate);
        }
    }

    public static void markTextureAsAnimatedInUse(TextureAtlasSprite sprite) {
        if (MiapiClient.isSodiumLoaded()) {
            animated.add(sprite);
        }
    }

    public static void onHudRender(GuiGraphics drawContext) {
        VertexConsumer consumer =
                ItemRenderer.getFoilBuffer(drawContext.bufferSource(), RenderType.gui(), false, false);
        int[] quadData = new int[32];
        for (TextureAtlasSprite sprite : animated) {
            BakedQuad bakedQuad = new BakedQuad(quadData, 0, Direction.DOWN, sprite, false);
            consumer.putBulkData(drawContext.pose().last(), bakedQuad, 0, 0, 0, 0, 0, 0);
        }
        animated.clear();
    }

    public record Holder(TextureAtlasSprite sprite, Material material, SpriteColorer colorer) {
    }
}
