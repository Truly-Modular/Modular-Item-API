package smartin.miapi.client.atlas;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.serialization.MapCodec;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.SpriteTicker;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceMetadata;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.mixin.client.SpriteContentsAccessor;
import smartin.miapi.mixin.client.SpriteSourcesAccessor;

import java.util.ArrayList;
import java.util.List;

/**
 * Truly Modulars System to add and modify sprites on the main block/item atlas
 */
public class BufferSpriteAdder implements SpriteSource {
    public static MapCodec<? extends SpriteSource> CODEC = AutoCodec.of(BufferSpriteAdder.class);
    public static SpriteSourceType TYPE = SpriteSourcesAccessor.callMiapiRegister("miapi_runtime", CODEC);

    @Override
    public void run(ResourceManager resourceManager, Output output) {
        MiapiEvents.CLEAR_CACHE.invoker().onReload();
        clearAtlasSlots();
        MaterialSpriteManager.ATLAS_SPRITE_POOL.clear();
        MaterialSpriteManager.FAST_CACHE.clear();
        MiapiConfig.getClientConfig().render.cacheSprites.forEach(cacheSprites -> {
            for (int i = 0; i < cacheSprites.count; i++) {
                List<MaterialSpriteManager.SpriteSlot> slots = MaterialSpriteManager.ATLAS_SPRITE_POOL.computeIfAbsent(MaterialSpriteManager.resToKey(cacheSprites.x, cacheSprites.y), (s) -> new ArrayList<>());
                ResourceLocation id = Miapi.id("runtime_" + cacheSprites.x + "x" + cacheSprites.y + "-" + i);
                NativeImage nativeImage = new NativeImage(cacheSprites.x, cacheSprites.y, false);
                MiapiSpriteContents contents = new MiapiSpriteContents(id,
                        new FrameSize(cacheSprites.x, cacheSprites.y),
                        nativeImage,
                        ResourceMetadata.EMPTY) {
                    SpriteContents thisContents = this;

                    @Nullable
                    public SpriteTicker createTicker() {
                        return new SpriteTicker() {
                            @Override
                            public void tickAndUpload(int x, int y) {
                                if (dirty) {
                                    thisContents.uploadFirstFrame(x, y);
                                    dirty = false;
                                }
                            }

                            @Override
                            public void close() {
                                thisContents.close();
                            }
                        };
                    }
                };
                output.add(id, spriteResourceLoader -> contents);
                slots.add(new MaterialSpriteManager.SpriteSlot(id, cacheSprites.x, cacheSprites.y, contents, (n) -> {
                    SpriteContentsAccessor accessor = (SpriteContentsAccessor) contents;
                    accessor.getImage().copyFrom(n);
                    contents.dirty = true;
                }));
            }
        });
        MiapiEvents.CLEAR_CACHE.invoker().onReload();
    }

    public static void clearAtlasSlots() {
        MaterialSpriteManager.ATLAS_SPRITE_POOL.values().forEach(slots -> {
            slots.forEach(MaterialSpriteManager.SpriteSlot::destroy);
            slots.clear();
        });

        MaterialSpriteManager.FAST_CACHE.values().forEach(MaterialSpriteManager.SpriteSlot::destroy);
        MaterialSpriteManager.FAST_CACHE.clear();

        MaterialSpriteManager.ANIMATED_ATLAS_SPRITES.clear();
    }

    public static class MiapiSpriteContents extends SpriteContents {
        public boolean dirty = false;

        public MiapiSpriteContents(ResourceLocation name, FrameSize frameSize, NativeImage originalImage, ResourceMetadata metadata) {
            super(name, frameSize, originalImage, metadata);
        }
    }

    @Override
    public SpriteSourceType type() {
        return TYPE;
    }
}
