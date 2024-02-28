package smartin.miapi.modules.material.palette;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.redpxnda.nucleus.util.Color;
import com.redpxnda.nucleus.util.MiscUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import smartin.miapi.client.atlas.MaterialAtlasManager;
import smartin.miapi.client.renderer.NativeImageGetter;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class SpriteFromJson {
    public static final Map<String, Identifier> atlasIdShortcuts = MiscUtil.initialize(new HashMap<>(), m -> {
        m.put("block", SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
        m.put("particle", SpriteAtlasTexture.PARTICLE_ATLAS_TEXTURE);
        m.put("material", MaterialAtlasManager.MATERIAL_ATLAS_ID);
    });

    public Supplier<NativeImage> imageSupplier;
    public boolean isAnimated;

    public SpriteFromJson(JsonElement json) {
        if (!(json instanceof JsonObject obj))
            throw new IllegalArgumentException("json used for json sprite must be a json object!");

        JsonElement atlasRaw = obj.get("atlas");
        if (atlasRaw instanceof JsonPrimitive prim && prim.isString()) {
            String key = prim.getAsString();
            Identifier atlasId;

            if (atlasIdShortcuts.containsKey(key)) atlasId = atlasIdShortcuts.get(key);
            else atlasId = new Identifier(key);

            Identifier textureId = new Identifier(obj.get("texture").getAsString());
            SpriteContents contents = MinecraftClient.getInstance().getSpriteAtlas(atlasId).apply(textureId).getContents();
            imageSupplier = () -> NativeImageGetter.get(contents);
            isAnimated = MaterialSpriteColorer.isAnimatedSpriteStatic(contents);
        } else {
            isAnimated = false;
            Identifier textureId = new Identifier(obj.get("texture").getAsString());
            NativeImage rawImage = loadTexture(MinecraftClient.getInstance().getResourceManager(), textureId);
            imageSupplier = () -> rawImage;
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
            throw new IllegalArgumentException("Failed to fetch texture '" + id + "' for json sprite data!", ex);
        }
    }

    boolean isAnimated() {
        return isAnimated;
    }

    public NativeImage getNativeImage() {
        return imageSupplier.get();
    }

    public Color getAverageColor() {
        int red = 0;
        int green = 0;
        int blue = 0;
        int count = 0;

        NativeImage img = getNativeImage();
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                int color = img.getColor(x, y);
                red += ColorHelper.Abgr.getRed(color);
                green += ColorHelper.Abgr.getGreen(color);
                blue += ColorHelper.Abgr.getBlue(color);
                count++;
            }
        }

        return new Color(red/count, green/count, blue/count, 255);
    }
}
