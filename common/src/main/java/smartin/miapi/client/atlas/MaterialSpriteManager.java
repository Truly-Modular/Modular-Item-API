package smartin.miapi.client.atlas;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.architectury.event.EventResult;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.client.AnimatedTexturesManager;
import smartin.miapi.client.MiapiClient;
import smartin.miapi.client.renderer.MovedVertexConsumer;
import smartin.miapi.client.renderer.RescaledVertexConsumer;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.material.base.Material;
import smartin.miapi.material.palette.SpriteColorer;
import smartin.miapi.mixin.client.BufferBuilderAccessor;
import smartin.miapi.mixin.client.SpriteContentsAccessor;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.IntUnaryOperator;

/**
 * This complex mess is responsible for using the result of a Colorer and make sure its renderable.
 * it works via {@link MaterialSpriteManager#getVertexConsumer}. by providing the old sprite and the recolorer this class
 * manages the caching and different rendering methods.
 */
@Environment(EnvType.CLIENT)
public class MaterialSpriteManager {
    static Map<Holder, DynamicTexture> animated_Textures = new HashMap<>();
    static long CURRENT_GENERATION = 0;

    public static final long CACHE_SIZE = 10000;
    public static final long CACHE_LIFETIME = 10;
    public static final TimeUnit CACHE_LIFETIME_UNIT = TimeUnit.SECONDS;
    protected static Map<ResourceLocation, DynamicTexture> nativeImageBackedTextureMap = new HashMap<>();
    public static Set<TextureAtlasSprite> animated = new HashSet<>();
    public static final Map<Integer, List<SpriteSlot>> ATLAS_SPRITE_POOL = new HashMap<>();
    public static final Map<Holder, SpriteSlot> FAST_CACHE = new HashMap<>();
    public static final List<SpriteSlot> ANIMATED_ATLAS_SPRITES = new ArrayList<>();

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
                public @NotNull ResourceLocation load(@NotNull Holder key) {
                    return getMaterialSprite(key);
                }
            });

    static {
        MiapiEvents.CLEAR_CACHE.register(() -> {
            clear();
            return EventResult.pass();
        });
    }

    public static ResourceLocation getMaterialSprite(Holder holder) {
        ResourceLocation identifier = materialSpriteCache.getIfPresent(holder);
        if (identifier == null) {
            SpriteColorer.MaterialRecoloredSpriteHolder colorer = holder.colorer().createSpriteManager(holder.sprite().contents());
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

    /**
     * destroyes all render caches, called on clear caches
     */
    public static void clear() {
        materialSpriteCache.invalidateAll();
        ANIMATED_ATLAS_SPRITES.clear();
        FAST_CACHE.forEach((h, slot) -> {
            slot.used = 0;
            ATLAS_SPRITE_POOL.computeIfAbsent(resToKey(slot.x, slot.y), (v) -> new ArrayList<>()).add(slot);
        });
        FAST_CACHE.clear();
        //TODO:free atlas sprites
    }

    /**
     * the tick function.
     * responsible for animated textures and clearing unused caches
     */
    public static void tick() {
        if (!ReloadEvents.isInReload()) {
            List<Holder> toRemove = new ArrayList<>();
            animated_Textures.forEach(((holder, nativeImageBackedTexture) -> {
                try {
                    holder.colorer.tick((nativeImage) -> {
                        //important!
                        //the MaskColorer is responsible for managing any NativeImage it creates.
                        //BUT the NativeBackedTexture removes its old uploaded NativeImage, so we need to upload a copy
                        Objects.requireNonNull(nativeImageBackedTexture.getPixels()).copyFrom(nativeImage);
                        nativeImageBackedTexture.upload();
                    }, holder.sprite().contents());
                } catch (Exception e) {
                    toRemove.add(holder);
                }
            }));
            ANIMATED_ATLAS_SPRITES.forEach(slot -> {
                AnimatedTexturesManager.markAnimated(slot.getSprite());
                slot.updateSprite();
            });
            toRemove.forEach(materialSpriteCache::invalidate);
            List<Holder> toRemoveFAST = new ArrayList<>();
            FAST_CACHE.forEach((h, s) -> {
                s.used--;
                if (s.used < 1) {
                    toRemoveFAST.add(h);
                    s.clear();
                    ANIMATED_ATLAS_SPRITES.remove(s);
                }
            });
            toRemoveFAST.forEach(h -> {
                SpriteSlot slot = FAST_CACHE.remove(h);
                ATLAS_SPRITE_POOL.computeIfAbsent(resToKey(slot.x, slot.y), (s) -> new ArrayList<>()).add(slot);
            });
            for (TextureAtlasSprite sprite : animated) {
                AnimatedTexturesManager.markAnimated(sprite);
            }

        }

    }

    public static void markTextureAsAnimatedInUse(TextureAtlasSprite sprite) {
        if (MiapiClient.isSodiumLoaded()) {
            animated.add(sprite);
        }
    }

    private static VertexConsumer vanillaItemVc = null;

    public static VertexConsumer getVanillaItemVC(MultiBufferSource b) {
        if (vanillaItemVc != null) {
            if (vanillaItemVc instanceof BufferBuilder buffer) {
                if (((BufferBuilderAccessor) buffer).isMiapiBuilding()) {
                    return vanillaItemVc;
                }
            }
        }
        vanillaItemVc = ItemRenderer.getFoilBuffer(b, ItemBlockRenderTypes.getRenderType(ItemStack.EMPTY, false), true, false);
        return vanillaItemVc;
    }

    /**
     * @param originalSprite        the non-recolored sprite
     * @param material              the material, mainly used for caching and optimised lookups
     * @param materialSpriteColorer The colorer recoloring the sprite
     * @return a vertexconsumer that can be talked to like it was the original sprite, but renders the recolored one.
     */
    public static void getVertexConsumer(TextureAtlasSprite originalSprite, Material material, SpriteColorer materialSpriteColorer, VertexConsumerProvider out) {
        out.spriteHolder = new Holder(originalSprite, material, materialSpriteColorer);
        long thisGeneration = 0;
        if (!originalSprite.atlasLocation().equals(TextureAtlas.LOCATION_BLOCKS)) {
            out.getRenderSaveVC = (b -> getDynamicTextureVertexConsumer(b, originalSprite, out.spriteHolder));
            out.vanillaVCGetter = MaterialSpriteManager::getVanillaItemVC;
            return;
        }
        if (MiapiConfig.getClientConfig().render.enableFastRender) {
            SpriteSlot spriteSlot = FAST_CACHE.get(out.spriteHolder);
            if (spriteSlot == null) {
                spriteSlot = getFreeAtlasSlot(((SpriteContentsAccessor) originalSprite.contents()).getMiapiWidth(), ((SpriteContentsAccessor) originalSprite.contents()).getMiapiHeight());
                if (spriteSlot != null) {
                    spriteSlot.used = 6;
                    spriteSlot.holder = out.spriteHolder;
                    FAST_CACHE.put(out.spriteHolder, spriteSlot);
                    if (out.spriteHolder.colorer().doTick()) {
                        ANIMATED_ATLAS_SPRITES.add(spriteSlot);
                        AnimatedTexturesManager.markAnimated(spriteSlot.getSprite());
                    }
                    spriteSlot.updateSprite();
                    thisGeneration = spriteSlot.generation;
                }
            }
            if (spriteSlot != null) {
                out.u = spriteSlot.getSprite().getU0() - originalSprite.getU0();
                out.v = spriteSlot.getSprite().getV0() - originalSprite.getV0();
                out.spriteSlot = spriteSlot;
            }
        }
        long finalGen = thisGeneration;
        out.isSpriteSlotValid = () -> {
            if (finalGen != out.spriteSlot.generation) {
                getVertexConsumer(originalSprite, material, materialSpriteColorer, out);
                return false;
            }
            return true;
        };
        out.getRenderSaveVC = (b -> {
            if (finalGen != out.spriteSlot.generation) {
                getVertexConsumer(originalSprite, material, materialSpriteColorer, out);
            }
            if (out.spriteSlot != null && out.spriteSlot.used > 0 && out.spriteSlot.used < 4 && MiapiConfig.getClientConfig().render.enableFastRender) {
                return getBlockAtlasVertexConsumer(b, originalSprite, out.spriteHolder, out.spriteSlot);
            }
            return getDynamicTextureVertexConsumer(b, originalSprite, out.spriteHolder);
        });
        //add sprite verification checks once per tick to each out vcprovider
        out.vanillaVCGetter = MaterialSpriteManager::getVanillaItemVC;
    }

    /**
     * Creates a Vertex Consumer that uses on BlockAtlas Textures to render (if possible)
     */
    private static @NotNull VertexConsumer getBlockAtlasVertexConsumer(MultiBufferSource vertexConsumers, TextureAtlasSprite originalSprite, Holder holder, SpriteSlot spriteSlot) {
        spriteSlot.used = 3;

        return new MovedVertexConsumer(
                getVanillaItemVC(vertexConsumers),
                originalSprite,
                spriteSlot.getSprite()
        );
    }

    /**
     * Creates a new Vertex Consumer that uses Dynamic Textures to render
     */
    private static @NotNull RescaledVertexConsumer getDynamicTextureVertexConsumer(MultiBufferSource vertexConsumers, TextureAtlasSprite originalSprite, Holder holder) {
        ResourceLocation replaceId = MaterialSpriteManager.getMaterialSprite(holder);
        RenderType atlasRenderLayer = RenderType.entityTranslucentCull(replaceId);
        VertexConsumer atlasConsumer = ItemRenderer.getFoilBufferDirect(vertexConsumers, atlasRenderLayer, true, false);
        return new RescaledVertexConsumer(atlasConsumer, originalSprite);
    }

    public record Holder(TextureAtlasSprite sprite, Material material, SpriteColorer colorer) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Holder holder = (Holder) o;
            return Objects.equals(sprite, holder.sprite) &&
                   Objects.equals(material, holder.material) &&
                   Objects.equals(colorer, holder.colorer);
        }

        @Override
        public int hashCode() {
            return Objects.hash(sprite, material, colorer);
        }

    }

    /**
     * will try to find a free SpriteSlot on the main BlockAtlas
     *
     * @param width  the desired sprite width
     * @param height the desired sprite height
     * @return a sprite slot if avialible, null if none are available
     */
    @Nullable
    public static SpriteSlot getFreeAtlasSlot(int width, int height) {
        int key = (width << 16) | height;
        List<SpriteSlot> slots = ATLAS_SPRITE_POOL.get(key);
        if (slots == null) return null;

        for (int i = 0; i < slots.size(); i++) {
            SpriteSlot slot = slots.get(i);
            if (slot.used < 1) {
                slots.remove(slot);

                return slot;
            }
        }
        return null;
    }

    /**
     * allows simple integer based keys for res, used for optimised lookups
     *
     * @param width  the desired sprite width
     * @param height the desired sprite height
     */
    public static int resToKey(int width, int height) {
        return (width << 16) | height;
    }

    /**
     * a slot on the main block atlas for a sprite, manages its content and stuffs.
     */
    public static class SpriteSlot {
        public int used = 0;
        public long generation = 0;
        public ResourceLocation internalID;
        public Consumer<NativeImage> update;
        public int x;
        public int y;
        public final SpriteContents contents;
        public TextureAtlasSprite sprite;
        public Holder holder;

        public SpriteSlot(ResourceLocation id, int x, int y, SpriteContents contents, Consumer<NativeImage> update) {
            this.contents = contents;
            this.internalID = id;
            this.x = x;
            this.y = y;
            this.update = update;
        }

        public TextureAtlasSprite getSprite() {
            if (sprite == null) {
                sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(internalID);
            }
            return sprite;
        }

        public int width() {
            return x;
        }

        public int height() {
            return y;
        }

        public void updateSprite() {
            if (holder != null) {
                update.accept(holder.colorer().createSpriteManager(holder.sprite().contents()).recolor());
            }
            this.generation = CURRENT_GENERATION;
        }

        public void clear() {
            NativeImage image = new NativeImage(x, y, false);
            for (int x = 0; x < this.x; x++) {
                for (int y = 0; y < this.y; y++) {
                    image.setPixelRGBA(x, y, 0);
                }
            }
            update.accept(image);
        }


        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (object == null || getClass() != object.getClass()) return false;
            SpriteSlot that = (SpriteSlot) object;
            return Objects.equals(internalID, that.internalID);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(internalID);
        }
    }

}
