package smartin.miapi.modules.material.palette;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.redpxnda.nucleus.util.Color;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import smartin.miapi.client.renderer.NativeImageGetter;
import smartin.miapi.modules.material.Material;

import java.io.InputStream;

public class OverlayMaterialSpriteColorer extends MaterialSpriteColorer {
    public final Color averageColor;
    public final SpriteContents contents;
    public final NativeImage rawImage;
    public final boolean isAnimated;

    public OverlayMaterialSpriteColorer(Material material, JsonElement json) {
        super(material);
        averageColor = Color.BLACK; // todo avg color
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
            isAnimated = isAnimated(contents);
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

    @Override
    public Color getAverageColor() {
        return averageColor;
    }

    @Override
    public NativeImage transform(SpriteContents sprite) {
        NativeImage rawImage = NativeImageGetter.get(sprite);
        NativeImage overlayImage = contents != null ? NativeImageGetter.get(contents) : rawImage;
        NativeImage image = new NativeImage(rawImage.getWidth(), rawImage.getHeight(), true);

        for (int x = 0; x < rawImage.getWidth(); x++) {
            for (int y = 0; y < rawImage.getHeight(); y++) {
                if (rawImage.getOpacity(x, y) < 5 && rawImage.getOpacity(x, y) > -1) {
                    image.setColor(x, y, 0);
                } else {
                    image.setColor(x, y, overlayImage.getColor(x % overlayImage.getWidth(), y % overlayImage.getHeight()));
                }
            }
        }
        image.untrack();
        return image;
    }

    @Override
    public boolean isAnimated() {
        return isAnimated;
    }
}
