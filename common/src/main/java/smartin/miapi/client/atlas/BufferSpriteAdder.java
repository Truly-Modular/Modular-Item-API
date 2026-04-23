package smartin.miapi.client.atlas;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.serialization.MapCodec;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.SpriteTicker;
import net.minecraft.client.renderer.texture.atlas.SpriteResourceLoader;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceMetadata;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.mixin.client.SpriteSourcesAccessor;
import smartin.miapi.mixin.client.SpriteContentsAccessor;

import java.util.ArrayList;
import java.util.List;

/**
 * Truly Modulars System to add and modify sprites on the main block/item atlas
 */
public class BufferSpriteAdder implements SpriteSource {
    public static MapCodec<? extends SpriteSource> CODEC = AutoCodec.of(BufferSpriteAdder.class);
    public static SpriteSourceType TYPE = SpriteSourcesAccessor.callRegister("miapi_runtime", CODEC);

    @Override
    public void run(ResourceManager resourceManager, Output output) {
        MaterialSpriteManager.ATLAS_SPRITE_POOL.clear();
        MaterialSpriteManager.FAST_CACHE.clear();
        MiapiConfig.getClientConfig().other.cacheSprites.forEach(cacheSprites -> {
            for (int i = 0; i < cacheSprites.count; i++) {
                List<MaterialSpriteManager.SpriteSlot> slots = MaterialSpriteManager.ATLAS_SPRITE_POOL.computeIfAbsent(MaterialSpriteManager.resToKey(cacheSprites.x, cacheSprites.y), (s) -> new ArrayList<>());
                ResourceLocation id = Miapi.id("runtime_" + cacheSprites.x + "x" + cacheSprites.y + "-" + i);
                NativeImage nativeImage = new NativeImage(cacheSprites.x, cacheSprites.y, false);

                SpriteContents contents = new SpriteContents(id,
                        new FrameSize(cacheSprites.x, cacheSprites.y),
                        nativeImage,
                        ResourceMetadata.EMPTY) {
                    SpriteContents thisContents = this;

                    @Nullable
                    public SpriteTicker createTicker() {
                        return new SpriteTicker() {
                            @Override
                            public void tickAndUpload(int x, int y) {
                                thisContents.uploadFirstFrame(x, y);
                            }

                            @Override
                            public void close() {
                                thisContents.close();
                            }
                        };
                    }
                };
                output.add(id, new SpriteSupplier() {

                    @Override
                    public SpriteContents apply(SpriteResourceLoader spriteResourceLoader) {
                        return contents;
                    }
                });
                slots.add(new MaterialSpriteManager.SpriteSlot(id, cacheSprites.x, cacheSprites.y, contents, (n) -> {
                    ((SpriteContentsAccessor) contents).getImage().copyFrom(n);
                }));
            }
        });
    }

    @Override
    public SpriteSourceType type() {
        return TYPE;
    }
}
