package smartin.miapi.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.SpriteLoader;
import net.minecraft.client.texture.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import smartin.miapi.modules.properties.material.MaterialProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static smartin.miapi.Miapi.MOD_ID;

@Environment(value = EnvType.CLIENT)
public class MaterialAtlasManager extends SpriteAtlasHolder {
    public static final Identifier MATERIAL_ID = new Identifier(MOD_ID, "materials");

    public MaterialAtlasManager(TextureManager textureManager) {
        super(textureManager, new Identifier(MOD_ID, "textures/atlas/materials.png"), MATERIAL_ID);
    }

    @Override
    public void afterReload(SpriteLoader.StitchResult invalidResult, Profiler profiler) {
        List<SpriteContents> materialSprites = new ArrayList<>();
        Executor executor = newSingleThreadExecutor();
        SpriteLoader spriteLoader = new SpriteLoader(MATERIAL_ID, 256, 256, MaterialProperty.materials.size());
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
