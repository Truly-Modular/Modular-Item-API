package smartin.miapi.client;

import dev.architectury.event.events.client.ClientTickEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resource.metadata.AnimationResourceMetadata;
import net.minecraft.client.texture.SpriteLoader;
import net.minecraft.client.texture.*;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import smartin.miapi.Miapi;
import smartin.miapi.client.modelrework.AltBakedMiapiModel;
import smartin.miapi.mixin.client.SpriteContentsAccessor;
import smartin.miapi.modules.material.Material;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static smartin.miapi.Miapi.MOD_ID;

@Environment(value = EnvType.CLIENT)
public class AltModelAtlasManager extends SpriteAtlasHolder {
    public static AltModelAtlasManager atlasInstance;
    public static final Identifier MATERIAL_ID = new Identifier(MOD_ID, "miapi_material_sprites");
    public static final Identifier MATERIAL_ATLAS_ID = new Identifier(MOD_ID, "textures/atlas/material_sprites.png");
    public static boolean shouldUpdate = false;

    public AltModelAtlasManager(TextureManager textureManager) {
        super(textureManager, MATERIAL_ATLAS_ID, MATERIAL_ID);
        atlasInstance = this;
        ClientTickEvent.CLIENT_PRE.register((client) -> {
            if (AltModelAtlasManager.shouldUpdate) {
                update();
            }
        });
    }

    public CompletableFuture<Void> reload(Synchronizer synchronizer, ResourceManager manager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
        try {
            CompletableFuture var10000 = SpriteLoader.fromAtlas(this.atlas).load(manager, MATERIAL_ID, 0, prepareExecutor).thenCompose(SpriteLoader.StitchResult::whenComplete);
            Objects.requireNonNull(synchronizer);
            return var10000.thenCompose(synchronizer::whenPrepared).thenAcceptAsync((stitchResult) -> {
                this.afterReload((SpriteLoader.StitchResult) stitchResult, applyProfiler);
            }, applyExecutor);
        } catch (Exception e) {
            Miapi.LOGGER.error("Error During TextureStitch", e);
            return CompletableFuture.runAsync(() -> {
                SpriteLoader.StitchResult stitchResult;
                afterReload(null, applyProfiler);
            });
        }
    }

    public static List<WeakReference<AltBakedMiapiModel>> models = new ArrayList<>();

    public void update() {
        shouldUpdate = false;
        Set<SpriteInfoHolder> sprites = new HashSet<>();
        List<SpriteContents> materialSprites = new ArrayList<>();
        models.stream().filter(entry -> entry != null && entry.get() != null).
                forEach(weakReference -> {
                    AltBakedMiapiModel model = weakReference.get();
                    if (model != null) {
                        sprites.addAll(model.getSprites());
                        model.uploaded = true;
                        model.quadLookupMap.clear();
                    }
                });
        sprites.stream().distinct().forEach(spriteInfoHolder -> {
            if (spriteInfoHolder.material != null) {
                materialSprites.add(transform(spriteInfoHolder, spriteInfoHolder.material));
            }
        });
        SpriteLoader spriteLoader = new SpriteLoader(MATERIAL_ID, 1024, 1024, 1024);
        Executor executor = newSingleThreadExecutor();
        SpriteLoader.StitchResult stitchResult = spriteLoader.stitch(materialSprites, 0, executor);
        this.atlas.clear();
        this.atlas.upload(stitchResult);
    }

    public SpriteContents transform(SpriteInfoHolder holder, Material material) {
        NativeImage rawImage = ((SpriteContentsAccessor) holder.oldSprite.getContents()).getImage();
        NativeImage image = new NativeImage(holder.oldSprite.getContents().getWidth(), holder.oldSprite.getContents().getHeight(), true);
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
        Identifier newID = holder.getIdentifier();
        return new SpriteContents(
                newID,
                new SpriteDimensions(
                        ((SpriteContentsAccessor) holder.oldSprite.getContents()).getWidth(),
                        ((SpriteContentsAccessor) holder.oldSprite.getContents()).getHeight()),
                image,
                AnimationResourceMetadata.EMPTY);
    }

    private int getColor(Material material, int color) {
        Sprite sprite = MiapiClient.materialAtlasManager.getMaterialSprite(material.getPalette().getSpriteId());
        if (sprite == null) {
            sprite = MiapiClient.materialAtlasManager.getMaterialSprite(MaterialAtlasManager.BASE_MATERIAL_ID);
        }
        return ((SpriteContentsAccessor) sprite.getContents()).getImage().getColor(Math.max(Math.min(color, 255), 0), 0);
    }


    @Override
    public void afterReload(SpriteLoader.StitchResult invalidResult, Profiler profiler) {
        profiler.startTick();
        profiler.push("upload");
        atlas.upload(invalidResult);
        profiler.pop();
        profiler.endTick();
    }

    public Sprite getSprite(Identifier identifier) {
        try {
            return atlas.getSprite(identifier);
        } catch (Exception e) {
            return null;
        }
    }

    public record SpriteInfoHolder(Sprite oldSprite, Material material) {
        public boolean equals(Record left) {
            if (left instanceof SpriteInfoHolder holder) {
                return oldSprite.getContents().getId().equals(holder.oldSprite.getContents().getId()) &&
                        holder.material.getKey().equals(material.getKey());
            }
            return false;
        }

        public Identifier getIdentifier() {
            if (material != null) {
                return new Identifier(Miapi.MOD_ID, oldSprite.getContents().getId().getPath() + "_transformed_" + material.getKey());
            }
            return new Identifier(Miapi.MOD_ID, oldSprite.getContents().getId().getPath() + "_no_material");
        }
    }
}
