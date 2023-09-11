package smartin.miapi.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.SpriteLoader;
import net.minecraft.client.texture.*;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.material.Material;
import smartin.miapi.modules.properties.material.MaterialProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static smartin.miapi.Miapi.MOD_ID;

@Environment(value = EnvType.CLIENT)
public class MaterialAtlasManager extends SpriteAtlasHolder {
    public static final Identifier MATERIAL_ID = new Identifier(MOD_ID, "miapi_materials");
    private Identifier sourcePath = new Identifier(MOD_ID, "miapi_materials");

    public MaterialAtlasManager(TextureManager textureManager) {
        super(textureManager, new Identifier(MOD_ID, "textures/atlas/materials.png"), MATERIAL_ID);
    }

    public CompletableFuture<Void> reload(ResourceReloader.Synchronizer synchronizer, ResourceManager manager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
        Miapi.DEBUG_LOGGER.error("RELOAD MATERIALATLAS");
        try {
            CompletableFuture var10000 = SpriteLoader.fromAtlas(this.atlas).load(manager, this.sourcePath, 0, prepareExecutor).thenCompose(SpriteLoader.StitchResult::whenComplete);
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

    @Override
    public void afterReload(SpriteLoader.StitchResult invalidResult, Profiler profiler) {
        List<SpriteContents> materialSprites = new ArrayList<>();
        if (invalidResult != null) {
            try{
                atlas.upload(invalidResult);
            }catch (Exception e){
                Miapi.DEBUG_LOGGER.error("EXCEPTION ",e);
            }
        }

        //PaletteCreators.paletteCreator.dispatcher().createPalette(json, key);
        for (String s : MaterialProperty.materials.keySet()) {
            Material material = MaterialProperty.materials.get(s);
            Identifier materialIdentifier = new Identifier(Miapi.MOD_ID, "textures/miapi_materials/" + s);
            if (invalidResult != null && atlas.getSprite(materialIdentifier).equals(invalidResult.missing())) {
                //Sprite sprite = material.
                SpriteContents contents = material.generateSpriteContents();
                if (contents != null) {
                    materialSprites.add(material.generateSpriteContents());
                } else {
                    Miapi.LOGGER.error("Material could not generate a Sprite " + material.getKey());
                }
            } else {
                Sprite sprite = atlas.getSprite(materialIdentifier);
                materialSprites.add(sprite.getContents());
            }
        }
        Executor executor = newSingleThreadExecutor();
        int shortMax = 32766;
        int width = (int) (Math.floor((double) materialSprites.size() / shortMax) * 256);
        int height = materialSprites.size() % shortMax;
        SpriteLoader spriteLoader = new SpriteLoader(MATERIAL_ID, 256, width, height);
        SpriteLoader.StitchResult stitchResult = spriteLoader.stitch(materialSprites, 0, executor);
        Miapi.LOGGER.error("AtlasSize " + stitchResult.height() + " " + stitchResult.width());
        profiler.startTick();
        profiler.push("upload");
        this.atlas.upload(stitchResult);
        profiler.pop();
        profiler.endTick();
    }

    public Sprite getMaterialSprite(Identifier id) {
        return getSprite(id);
    }
}
