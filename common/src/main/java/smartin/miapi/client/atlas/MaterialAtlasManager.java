package smartin.miapi.client.atlas;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.TextureAtlasHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static smartin.miapi.Miapi.MOD_ID;

@Environment(EnvType.CLIENT)
public class MaterialAtlasManager extends TextureAtlasHolder {
    public static final ResourceLocation MATERIAL_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "miapi_materials");
    public static final ResourceLocation MATERIAL_ATLAS_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/atlas/materials.png");
    public static final ResourceLocation BASE_MATERIAL_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "miapi_materials/base_palette");

    protected final List<AddedSpriteEntry> addedSprites = new ArrayList<>();

    public MaterialAtlasManager(TextureManager textureManager) {
        super(textureManager, MATERIAL_ATLAS_ID, MATERIAL_ID);
    }

    public void addSpriteToLoad(ResourceLocation id) {
        addedSprites.add(new AddedSpriteEntry(id, s -> {}));
    }

    public void addSpriteToLoad(ResourceLocation id, Consumer<SpriteContents> onAdded) {
        addedSprites.add(new AddedSpriteEntry(id, onAdded));
    }


    public void apply(SpriteLoader.Preparations preparations, ProfilerFiller profiler) {
    }

    //TODO:analyze new atlas behaviour. this might already be functional
    /*
    @Override
    public void apply(SpriteLoader.Preparations preparations, ProfilerFiller profiler) {
        if (preparations != null)
            return;
        ReloadEvents.reloadCounter++;
        List<SpriteContents> materialSprites = new ArrayList<>();

        ResourceManager manager = Minecraft.getInstance().getResourceManager();
        for (AddedSpriteEntry entry : addedSprites) {
            ResourceLocation id = entry.id;
            Resource resource = manager.getResource(id).orElseThrow(() -> new RuntimeException(new FileNotFoundException(id.toString())));
            try {
                SpriteContents contents = SpriteLoader.load(id, resource);
                if (contents == null) {
                    Miapi.LOGGER.warn("Sprite creation of '{}' failed for material atlas! See logger error(s) above.", id);
                } else if (contents.width() != 256) {
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
        SpriteLoader.Preparations stitchResult = spriteLoader.stitch(materialSprites, 0, executor);

        profiler.startTick();
        profiler.push("upload");
        textureAtlas.upload(stitchResult);
        Miapi.LOGGER.info("Created material atlas with size {}x{}", width, height);
        profiler.pop();
        profiler.endTick();
        ModularItemCache.discardCache();
        ReloadEvents.reloadCounter--;
    }

     */

    public TextureAtlasSprite getMaterialSprite(ResourceLocation id) {
        TextureAtlasSprite sprite = getSprite(id);
        if (sprite == null) {
            sprite = getSprite(BASE_MATERIAL_ID);
        }
        return sprite;
    }

    public record AddedSpriteEntry(ResourceLocation id, Consumer<SpriteContents> onCreated) {}
}
