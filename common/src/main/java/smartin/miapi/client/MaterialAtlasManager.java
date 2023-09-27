package smartin.miapi.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.SpriteLoader;
import net.minecraft.client.texture.*;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.material.Material;
import smartin.miapi.modules.properties.material.MaterialProperty;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static smartin.miapi.Miapi.MOD_ID;

@Environment(value = EnvType.CLIENT)
public class MaterialAtlasManager extends SpriteAtlasHolder {
    public static final Identifier MATERIAL_ID = new Identifier("miapi_materials");
    public static final Identifier MATERIAL_ATLAS_ID = new Identifier(MOD_ID, "textures/atlas/materials.png");
    public static final Identifier BASE_MATERIAL_ID = new Identifier(MOD_ID, "miapi_materials/base_palette");

    public MaterialAtlasManager(TextureManager textureManager) {
        super(textureManager, MATERIAL_ATLAS_ID, MATERIAL_ID);
    }

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
            Miapi.LOGGER.error("Error during MaterialAtlasStitching", e);
        }
        for (String s : MaterialProperty.materials.keySet()) {
            Material material = MaterialProperty.materials.get(s);
            Identifier materialIdentifier = new Identifier(Miapi.MOD_ID, "miapi_materials/" + s);
            if (invalidResult != null && atlas.getSprite(materialIdentifier).equals(invalidResult.missing())) {
                try {
                    SpriteContents contents = material.getPalette().generateSpriteContents(materialIdentifier);
                    if (contents != null) {
                        materialSprites.add(contents);
                        material.getPalette().setSpriteId(materialIdentifier);
                    } else {
                        material.getPalette().setSpriteId(BASE_MATERIAL_ID);
                    }
                } catch (Exception e) {
                    Miapi.LOGGER.error("Couldnt generate MaterialPalette for " + s + " ",e);
                    material.getPalette().setSpriteId(BASE_MATERIAL_ID);
                }
            } else {
                Sprite sprite = atlas.getSprite(materialIdentifier);
                Miapi.LOGGER.error(sprite.getContents().getHeight() + " " + sprite.getContents().getWidth());
                try {
                    Identifier identifier = new Identifier(sprite.getContents().getId().toString().replace(":", ":textures/") + ".png");
                    Resource resource = MinecraftClient.getInstance().getResourceManager().getResourceOrThrow(identifier);
                    SpriteContents contents = SpriteLoader.load(sprite.getContents().getId(), resource);
                    if (contents.getWidth() < 255) {
                        Miapi.LOGGER.error("Material manual Image not correctly sized for material " + materialIdentifier);
                    }
                    materialSprites.add(contents);
                    material.getPalette().setSpriteId(materialIdentifier);
                } catch (FileNotFoundException e) {
                    Miapi.LOGGER.error("Error during MaterialAtlasStitching", e);
                }
            }
        }
        Executor executor = newSingleThreadExecutor();
        int shortMax = 32766;
        int width = (int) (Math.floor((double) materialSprites.size() / shortMax) * 256);
        int height = materialSprites.size() % shortMax;
        SpriteLoader spriteLoader = new SpriteLoader(MATERIAL_ID, 256, width, height);
        SpriteLoader.StitchResult stitchResult = spriteLoader.stitch(materialSprites, 0, executor);
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
