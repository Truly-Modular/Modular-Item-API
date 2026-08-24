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
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
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
    public static final long CACHE_SIZE = 10000;
    public static final long CACHE_LIFETIME = 10;
    public static final TimeUnit CACHE_LIFETIME_UNIT = TimeUnit.SECONDS;
    protected static Map<ResourceLocation, DynamicTexture> nativeImageBackedTextureMap = new HashMap<>();
    public static Set<TextureAtlasSprite> animated = new HashSet<>();
    /**
     * Image Pool. Managed by {@link BufferSpriteAdder}
     */
    public static final Map<Integer, List<SpriteSlot>> ATLAS_SPRITE_POOL = new HashMap<>();
    /**
     * Fast lookup for currently used SpriteSlots
     */
    public static final Map<Holder, SpriteSlot> FAST_CACHE = new HashMap<>();
    /**
     * Collection of currently used animated slots to be ticked each frame.
     */
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

    /**
     * creates or finds an existing off atlas Dynamic Texture Sprite and returns the id
     */
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
            slot.invalidate();
            ATLAS_SPRITE_POOL.computeIfAbsent(resToKey(slot.x, slot.y), (v) -> new ArrayList<>()).add(slot);
        });
        FAST_CACHE.clear();
        nativeImageBackedTextureMap.forEach((id, texture) -> {
            texture.close();
        });
        nativeImageBackedTextureMap.clear();
        Miapi.LOGGER.info("clear at tick " + MiapiClient.tick.get());
    }

    /**
     * the tick function.
     * responsible for animated textures and clearing unused caches
     * also responsible for managing SpriteSlot activity and decay
     */
    public static void tick() {
        if (ReloadEvents.isInReload()) {
            return;
        }
        try {
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
                s.tickUsage();
                if (s.shouldBeFreed()) {
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
        } catch (RuntimeException e) {
            Miapi.LOGGER.info("prevented crash at tick " + MiapiClient.tick.get());
            MiapiEvents.CLEAR_CACHE.invoker().onReload();
        }
    }

    /**
     * redo the setup for the Provider, grabing (if available) a space on the BlockAtlas
     */
    public static void providerReAquire(VertexConsumerProvider provider) {
        Holder holder = provider.spriteHolder;
        provider.isFast = false;
        if (holder != null && MiapiConfig.getClientConfig().render.enableFastRender) {
            SpriteSlot spriteSlot = FAST_CACHE.get(provider.spriteHolder);
            if (spriteSlot == null) {
                spriteSlot = getFreeAtlasSlot(((SpriteContentsAccessor) holder.sprite().contents()).getMiapiWidth(), ((SpriteContentsAccessor) holder.sprite().contents()).getMiapiHeight());
                if (spriteSlot != null) {
                    //set to 4 to block rendering in first frame to to upload preceding rendering,
                    //so a SpriteSlot is not available during first requested frame
                    spriteSlot.allocated();
                    spriteSlot.holder = provider.spriteHolder;
                    FAST_CACHE.put(provider.spriteHolder, spriteSlot);
                    if (provider.spriteHolder.colorer().doTick()) {
                        ANIMATED_ATLAS_SPRITES.add(spriteSlot);
                        AnimatedTexturesManager.markAnimated(spriteSlot.getSprite());
                    }
                    spriteSlot.updateSprite();
                }
            }
            if (spriteSlot != null) {
                provider.spriteSlot = spriteSlot;
            }
        }
    }


    public static boolean isSlotStilLValid(VertexConsumerProvider provider) {
        return provider.spriteSlot != null &&
               provider.spriteSlot.canBeRendered() &&
               provider.spriteSlot.holder != null && provider.spriteSlot.holder.hashCode() == provider.spriteHolder.hashCode();
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
        if (!originalSprite.atlasLocation().equals(TextureAtlas.LOCATION_BLOCKS)) {
            out.getRenderSaveVC = (b -> getDynamicTextureVertexConsumer(b, originalSprite, out.spriteHolder));
            out.vanillaVCGetter = MaterialSpriteManager::getVanillaItemVC;
            return;
        }
        out.getRenderSaveVC = (b -> {
            if (isSlotStilLValid(out)) {
                return getBlockAtlasVertexConsumer(b, originalSprite, out.spriteHolder, out.spriteSlot);
            } else {
                providerReAquire(out);
                return getDynamicTextureVertexConsumer(b, originalSprite, out.spriteHolder);
            }
        });
        //add sprite verification checks once per tick to each out vcprovider
        out.vanillaVCGetter = MaterialSpriteManager::getVanillaItemVC;
    }

    /**
     * Creates a Vertex Consumer that uses on BlockAtlas Textures to render (if possible)
     */
    private static @NotNull VertexConsumer getBlockAtlasVertexConsumer(MultiBufferSource vertexConsumers, TextureAtlasSprite originalSprite, Holder holder, SpriteSlot spriteSlot) {
        if (spriteSlot.canBeRendered()) {
            spriteSlot.use();
        }
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

    /**
     * Holders serve as unique Identifiers for any kind of texture by how its collored.
     * (Prob) this should not require the material and just the SpriteColorer in the future.
     * is extremly hash and equal friendly.
     */
    public record Holder(TextureAtlasSprite sprite, Material material, SpriteColorer colorer, int hash) {

        public Holder(TextureAtlasSprite sprite, Material material, SpriteColorer colorer) {
            this(sprite, material, colorer, Objects.hash(sprite, material, colorer));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Holder holder = (Holder) o;
            return this.hash() == holder.hash() &&
                   Objects.equals(sprite, holder.sprite) &&
                   Objects.equals(material, holder.material) &&
                   Objects.equals(colorer, holder.colorer);
        }

        @Override
        public int hashCode() {return hash;}
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
            if (slot.shouldBeFreed()) {
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
    public static int resToKey(int width, int height) {return (width << 16) | height;}

    /**
     * a slot on the main block atlas for a sprite, manages its content and stuffs.
     */
    public static class SpriteSlot {
        private int used = 0;
        public ResourceLocation internalID;
        private Consumer<NativeImage> update;
        public int x;
        public int y;
        public final BufferSpriteAdder.MiapiSpriteContents contents;
        public TextureAtlasSprite sprite;
        public Holder holder;
        private static final int KEEP_ALIVE_COUNT = 10;
        private static final int MAX_RENDER_ALLOWED = KEEP_ALIVE_COUNT - 1;

        public SpriteSlot(ResourceLocation id, int x, int y, BufferSpriteAdder.MiapiSpriteContents contents, Consumer<NativeImage> update) {
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

        public boolean canBeRendered() {return used <= MAX_RENDER_ALLOWED && used > 0 && isUploaded();}

        public boolean shouldBeFreed() {return used < 1;}

        public void allocated() {used = KEEP_ALIVE_COUNT;}

        public boolean isUploaded() {return !contents.dirty;}

        public void use() {used = MAX_RENDER_ALLOWED;}

        public void tickUsage() {used--;}

        public void invalidate() {used = 0;}

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
        }

        public void clear() {
            NativeImage image = new NativeImage(x, y, false);
            for (int x = 0; x < this.x; x++) {
                for (int y = 0; y < this.y; y++) {
                    image.setPixelRGBA(x, y, 0);
                }
            }
            update.accept(image);
            used = 0;
        }

        public void destroy() {
            used = 0;
            if (contents != null) {
                contents.close();
            }
            holder = null;
            sprite = null;
            update = null;
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
