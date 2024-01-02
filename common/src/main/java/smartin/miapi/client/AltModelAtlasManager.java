package smartin.miapi.client;

import dev.architectury.event.events.common.TickEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.SpriteLoader;
import net.minecraft.client.texture.*;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import smartin.miapi.Miapi;
import smartin.miapi.client.modelrework.AltBakedMiapiModel;
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

    public AltModelAtlasManager(TextureManager textureManager) {
        super(textureManager, MATERIAL_ATLAS_ID, MATERIAL_ID);
        atlasInstance = this;
        TickEvent.PLAYER_PRE.register(player -> {
            if (player.getWorld().isClient) {
                //update();
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
        Set<SpriteInfoHolder> sprites = new HashSet<>();
        List<SpriteContents> materialSprites = new ArrayList<>();
        models.stream().filter(entry -> entry != null && entry.get() != null).
                forEach(altBakedMiapiModelWeakReference ->
                        sprites.addAll(altBakedMiapiModelWeakReference.get().getSprites()));
        sprites.stream().forEach(spriteInfoHolder -> {
            materialSprites.add(spriteInfoHolder.spriteContents());
        });
        SpriteLoader spriteLoader = new SpriteLoader(MATERIAL_ID, 1024, 1024, 1024);
        Executor executor = newSingleThreadExecutor();
        SpriteLoader.StitchResult stitchResult = spriteLoader.stitch(materialSprites, 0, executor);
        this.atlas.upload(stitchResult);
    }

    @Override
    public void afterReload(SpriteLoader.StitchResult invalidResult, Profiler profiler) {
        profiler.startTick();
        profiler.push("upload");
        //update();
        atlas.upload(invalidResult);
        profiler.pop();
        profiler.endTick();
    }

    public Sprite getSprite(Identifier identifier){
        try{
            return atlas.getSprite(identifier);
        }
        catch (Exception e){
            return null;
        }
    }

    public record SpriteInfoHolder(SpriteContents spriteContents, Material material) {
    }
}
