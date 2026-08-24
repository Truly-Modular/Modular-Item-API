package smartin.miapi.client.atlas;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.TextureAtlasHolder;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.mixin.client.TextureAtlasAccessor;
import smartin.miapi.mixin.client.TextureAtlasHolderAccessor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static smartin.miapi.Miapi.MOD_ID;

/**
 * The Material Atlas class, directly tied to Atlas backed Materials.
 * look at {@link smartin.miapi.material.palette.PaletteAtlasBackedColorer} for more context.
 * TL;DR it uses a 1x256 texture to recolor where the x value is the original images brightness.
 * This allows for animated Palettes like prismarine and magma blocks
 */
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
        addedSprites.add(new AddedSpriteEntry(id, s -> {
        }));
    }

    public void addSpriteToLoad(ResourceLocation id, Consumer<SpriteContents> onAdded) {
        addedSprites.add(new AddedSpriteEntry(id, onAdded));
    }

    @Nullable
    public TextureAtlasSprite getMaterialSprite(ResourceLocation id) {
        try {
            TextureAtlasSprite sprite = getSprite(id);
            TextureAtlas atlas = ((TextureAtlasHolderAccessor) this).getMiapiTextureAtlas();
            if (sprite == ((TextureAtlasAccessor) atlas).getMiapiMissingSprite()) {
                return null;
            }
            return getSprite(id);
        } catch (RuntimeException e) {
            return null;
        }
    }

    @Override
    public void close() {
        super.close();
        MiapiEvents.CLEAR_CACHE.invoker().onReload();
    }

    public record AddedSpriteEntry(ResourceLocation id, Consumer<SpriteContents> onCreated) {
    }
}
