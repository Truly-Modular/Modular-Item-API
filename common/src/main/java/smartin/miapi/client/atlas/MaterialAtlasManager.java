package smartin.miapi.client.atlas;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.*;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import smartin.miapi.Miapi;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.modules.cache.ModularItemCache;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static smartin.miapi.Miapi.MOD_ID;

@Environment(EnvType.CLIENT)
public class MaterialAtlasManager extends SpriteAtlasHolder {
    public static final Identifier MATERIAL_ID = new Identifier(MOD_ID, "miapi_materials");
    public static final Identifier MATERIAL_ATLAS_ID = new Identifier(MOD_ID, "textures/atlas/materials.png");
    public static final Identifier BASE_MATERIAL_ID = new Identifier(MOD_ID, "miapi_materials/base_palette");

    protected final List<AddedSpriteEntry> addedSprites = new ArrayList<>();

    public MaterialAtlasManager(TextureManager textureManager) {
        super(textureManager, MATERIAL_ATLAS_ID, MATERIAL_ID);
    }

    public void addSpriteToLoad(Identifier id) {
        addedSprites.add(new AddedSpriteEntry(id, s -> {}));
    }

    public void addSpriteToLoad(Identifier id, Consumer<SpriteContents> onAdded) {
        addedSprites.add(new AddedSpriteEntry(id, onAdded));
    }

    @Override
    public void afterReload(SpriteLoader.StitchResult invalidResult, Profiler profiler) {
        if (invalidResult != null)
            return;
        ReloadEvents.reloadCounter++;
        List<SpriteContents> materialSprites = new ArrayList<>();

        ResourceManager manager = MinecraftClient.getInstance().getResourceManager();
        for (AddedSpriteEntry entry : addedSprites) {
            Identifier id = entry.id;
            Resource resource = manager.getResource(id).orElseThrow(() -> new RuntimeException(new FileNotFoundException(id.toString())));
            try {
                SpriteContents contents = SpriteLoader.load(id, resource);
                if (contents == null) {
                    Miapi.LOGGER.warn("Sprite creation of '{}' failed for material atlas! See logger error(s) above.", id);
                } else if (contents.getWidth() != 256) {
                    Miapi.LOGGER.warn("Ignoring sprite '{}' for material atlas, as it does not have a width of 256!", id);
                } else {
                    materialSprites.add(contents);
                    entry.onCreated.accept(contents);
                }
            } catch (Exception e) {
                Miapi.LOGGER.error("Failed to add sprite '" + id + "' to material atlas!", e);
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
        atlas.upload(stitchResult);
        Miapi.LOGGER.info("Created material atlas with size {}x{}", width, height);
        profiler.pop();
        profiler.endTick();
        ModularItemCache.discardCache();
        ReloadEvents.reloadCounter--;
    }

    public Sprite getMaterialSprite(Identifier id) {
        Sprite sprite = getSprite(id);
        if (sprite == null) {
            sprite = getSprite(BASE_MATERIAL_ID);
        }
        return sprite;
    }

    public record AddedSpriteEntry(Identifier id, Consumer<SpriteContents> onCreated) {}
}
