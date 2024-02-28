package smartin.miapi.modules.material.palette;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import smartin.miapi.client.renderer.NativeImageGetter;

import java.io.InputStream;

public class SpriteFromJson {
    public SpriteContents contents;
    public NativeImage rawImage;
    public boolean isAnimated;


    public SpriteFromJson(JsonElement json) {
        if (!(json instanceof JsonObject obj))
            throw new IllegalArgumentException("json used for OverlayMaterialSpriteColorer must be a json object!");

        JsonElement atlasRaw = obj.get("atlas");
        if (atlasRaw instanceof JsonPrimitive prim && prim.isString()) {
            String key = prim.getAsString();
            Identifier atlasId;

            if (key.equals("block")) atlasId = SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
            else atlasId = new Identifier(key);

            Identifier textureId = new Identifier(obj.get("texture").getAsString());
            contents = MinecraftClient.getInstance().getSpriteAtlas(atlasId).apply(textureId).getContents();
            rawImage = null;
            isAnimated = MaterialSpriteColorer.isAnimatedSpriteStatic(contents);
        } else {
            contents = null;
            isAnimated = false;
            Identifier textureId = new Identifier(obj.get("texture").getAsString());
            rawImage = loadTexture(MinecraftClient.getInstance().getResourceManager(), textureId);
        }
    }

    public static NativeImage loadTexture(ResourceManager resourceManager, Identifier id) {
        try {
            NativeImage nativeImage;
            Resource resource = resourceManager.getResourceOrThrow(id);
            try (InputStream inputStream = resource.getInputStream()) {
                nativeImage = NativeImage.read(inputStream);
            }
            return nativeImage;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Failed to fetch texture '" + id + "' for OverlayMaterialSpriteColorer!", ex);
        }
    }

    boolean isAnimated() {
        return isAnimated;
    }

    public NativeImage getNativeImage() {
        if(rawImage==null){
            return NativeImageGetter.get(contents);
        }
        return rawImage;
    }
}
