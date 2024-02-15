package smartin.miapi.client.atlas;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.*;
import net.minecraft.client.texture.atlas.AtlasLoader;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.modules.material.Material;
import smartin.miapi.modules.material.MaterialProperty;
import smartin.miapi.modules.material.palette.MaterialAtlasPalette;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static smartin.miapi.Miapi.MOD_ID;

@Environment(value = EnvType.CLIENT)
public class MaterialAtlasManager extends SpriteAtlasHolder {
    public static final Identifier MATERIAL_ID = new Identifier(MOD_ID, "miapi_materials");
    public static final Identifier MATERIAL_ATLAS_ID = new Identifier(MOD_ID, "textures/atlas/materials.png");
    public static final Identifier BASE_MATERIAL_ID = new Identifier(MOD_ID, "miapi_materials/base_palette");

    public MaterialAtlasManager(TextureManager textureManager) {
        super(textureManager, MATERIAL_ATLAS_ID, MATERIAL_ID);
        Sprite sprite;
    }

    /*
    public CompletableFuture<Void> reload(ResourceReloader.Synchronizer synchronizer, ResourceManager manager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
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
     */

    @Override
    public void afterReload(SpriteLoader.StitchResult invalidResult, Profiler profiler) {
        List<SpriteContents> materialSprites = new ArrayList<>();
        if (invalidResult != null) {
            try {
                atlas.upload(invalidResult);
            } catch (Exception e) {
            }
        }

        try {
            Sprite sprite = atlas.getSprite(BASE_MATERIAL_ID);
            Identifier identifier = new Identifier(sprite.getContents().getId().toString().replace(":", ":textures/") + ".png");
            Resource resource = MinecraftClient.getInstance().getResourceManager().getResourceOrThrow(identifier);
            SpriteContents contents = SpriteLoader.load(sprite.getContents().getId(), resource);
            materialSprites.add(contents);
        } catch (FileNotFoundException e) {
            Miapi.LOGGER.error("Error during MaterialAtlasStitching - this can be ignored, but should be fixed at some point");
        }
        for (String s : MaterialProperty.materials.keySet()) {
            Material material = MaterialProperty.materials.get(s);
            if (material.getPalette() instanceof MaterialAtlasPalette materialAtlasPalette) {
                Identifier materialIdentifier = new Identifier(Miapi.MOD_ID, "miapi_materials/" + s);
                try {
                    SpriteContents fromTexturePack = null;
                    try {
                        Sprite sprite = atlas.getSprite(materialIdentifier);
                        Identifier identifier = new Identifier(sprite.getContents().getId().toString().replace(":", ":textures/") + ".png");
                        Resource resource = MinecraftClient.getInstance().getResourceManager().getResourceOrThrow(identifier);
                        fromTexturePack = SpriteLoader.load(sprite.getContents().getId(), resource);
                    } catch (FileNotFoundException ignored) {

                    }
                    SpriteContents materialSprite = materialAtlasPalette.generateSpriteContents(materialIdentifier, fromTexturePack);
                    if(materialSprite==null){
                        Miapi.LOGGER.error("Material Image was not found for " + materialIdentifier);
                        Miapi.LOGGER.error("replacing with Uncolored Material " + materialIdentifier);
                    }
                    else if (materialSprite.getWidth() != 256) {
                        Miapi.LOGGER.error("Material Image not correctly sized for material " + materialIdentifier);
                        Miapi.LOGGER.error("replacing with Uncolored Material " + materialIdentifier);
                    } else {
                        materialSprites.add(materialSprite);
                        materialAtlasPalette.setSpriteId(materialIdentifier);
                    }
                } catch (Exception e) {
                    Miapi.LOGGER.error("Could not stitch Material " + materialIdentifier, e);
                }
            }
        }
        Executor executor = newSingleThreadExecutor();
        int shortMax = 32766;
        int width = ((int) (Math.floor((double) materialSprites.size() / shortMax) * 256) + 1) * 256;
        int height = materialSprites.size() % shortMax + 5;
        int maxSize = Math.max(Math.max(512, width + 5), height + 5);
        SpriteLoader spriteLoader = new SpriteLoader(MATERIAL_ID, maxSize, width, height);
        SpriteLoader.StitchResult stitchResult = spriteLoader.stitch(materialSprites, 0, executor);
        profiler.startTick();
        profiler.push("upload");
        this.atlas.upload(stitchResult);
        Miapi.LOGGER.info("Recreated Material atlas with Size " + width + "x" + height);
        profiler.pop();
        profiler.endTick();
    }

    @Nullable
    public Sprite getMaterialSprite(Identifier id) {
        Sprite sprite = getSprite(id);
        if (sprite == null) {
            sprite = getSprite(BASE_MATERIAL_ID);
        }
        if(sprite == null){
            //Miapi.LOGGER.info("Sprite is still not set, something in the reload was broken");
        }
        return sprite;
    }
}
