package smartin.miapi.material.palette;

import com.google.gson.JsonElement;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.redpxnda.nucleus.util.Color;
import com.redpxnda.nucleus.util.MiscUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.FastColor;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.client.atlas.MaterialAtlasManager;
import smartin.miapi.client.atlas.MaterialSpriteManager;
import smartin.miapi.client.renderer.NativeImageGetter;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class SpriteFromJson {
    public static final Map<String, ResourceLocation> atlasIdShortcuts = MiscUtil.initialize(new HashMap<>(), m -> {
        m.put("block", TextureAtlas.LOCATION_BLOCKS);
        m.put("particle", TextureAtlas.LOCATION_PARTICLES);
        m.put("material", MaterialAtlasManager.MATERIAL_ATLAS_ID);
    });

    public Supplier<NativeImageGetter.ImageHolder> imageSupplier;
    public boolean isAnimated;
    @Nullable
    public TextureAtlasSprite rawSprite = null;

    public static final Codec<SpriteFromJson> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("atlas").forGetter(sprite -> {
                for (Map.Entry<String, ResourceLocation> entry : atlasIdShortcuts.entrySet()) {
                    if (entry.getValue().equals(sprite.rawSprite.atlasLocation())) {
                        return entry.getKey();
                    }
                }
                return sprite.rawSprite.atlasLocation().toString();
            }),
            Codec.STRING.fieldOf("texture").forGetter(sprite -> sprite.rawSprite.contents().name().toString()),
            Codec.BOOL.optionalFieldOf("forceTick", false).forGetter(sprite -> sprite.isAnimated)
    ).apply(instance, SpriteFromJson::new));

    /*
    public SpriteFromJson(JsonElement json) {
        if (!(json instanceof JsonObject obj))
            throw new IllegalArgumentException("json used for json sprite must be a json object!");

        JsonElement atlasRaw = obj.get("atlas");
        if (atlasRaw instanceof JsonPrimitive prim && prim.isString()) {
            String key = prim.getAsString();
            ResourceLocation atlasId;

            if (atlasIdShortcuts.containsKey(key)) atlasId = atlasIdShortcuts.get(key);
            else atlasId = ResourceLocation.parse(key);

            ResourceLocation textureId = ResourceLocation.parse(obj.get("texture").getAsString());
            rawSprite = Minecraft.getInstance().getTextureAtlas(atlasId).apply(textureId);
            SpriteContents contents = rawSprite.contents();
            imageSupplier = () -> NativeImageGetter.get(contents);
            if (obj.has("forceTick"))
                isAnimated = obj.get("forceTick").getAsBoolean();
            else
                isAnimated = SpriteColorer.isAnimatedSpriteStatic(contents);
        } else {
            isAnimated = obj.has("forceTick") && obj.get("forceTick").getAsBoolean();
            ResourceLocation textureId = ResourceLocation.parse(obj.get("texture").getAsString());
            NativeImage rawImage = loadTexture(Minecraft.getInstance().getResourceManager(), textureId);
            NativeImageGetter.ImageHolder holder = new NativeImageGetter.ImageHolder();
            holder.nativeImage = rawImage;
            holder.width = rawImage.getWidth();
            holder.height = rawImage.getHeight();
            imageSupplier = () -> holder;
        }
    }
     */

    public static SpriteFromJson getFromJson(JsonElement element) {
        return CODEC.decode(JsonOps.INSTANCE, element).getOrThrow().getFirst();
    }

    public SpriteFromJson(String atlasKey, String texturePath, boolean forceTick) {
        ResourceLocation atlasId = atlasIdShortcuts.getOrDefault(atlasKey, ResourceLocation.parse(atlasKey));
        ResourceLocation textureId = ResourceLocation.parse(texturePath);
        rawSprite = Minecraft.getInstance().getTextureAtlas(atlasId).apply(textureId);
        SpriteContents contents = rawSprite.contents();
        imageSupplier = () -> NativeImageGetter.get(contents);
        isAnimated = forceTick || SpriteColorer.isAnimatedSpriteStatic(contents);
    }

    public static NativeImage loadTexture(ResourceManager resourceManager, ResourceLocation id) {
        try {
            NativeImage nativeImage;
            Resource resource = resourceManager.getResourceOrThrow(id);
            try (InputStream inputStream = resource.open()) {
                nativeImage = NativeImage.read(inputStream);
            }
            return nativeImage;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Failed to fetch texture '" + id + "' for json sprite data!", ex);
        }
    }

    public void markUse() {
        if (isAnimated() && rawSprite != null) {
            MaterialSpriteManager.markTextureAsAnimatedInUse(rawSprite);
        }
    }

    boolean isAnimated() {
        return isAnimated;
    }

    public NativeImageGetter.ImageHolder getNativeImage() {
        return imageSupplier.get();
    }

    public Color getAverageColor() {
        int red = 0;
        int green = 0;
        int blue = 0;
        int count = 0;

        NativeImageGetter.ImageHolder img = getNativeImage();
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                int color = img.getColor(x, y);
                red += FastColor.ABGR32.red(color);
                green += FastColor.ABGR32.green(color);
                blue += FastColor.ABGR32.blue(color);
                count++;
            }
        }

        return new Color(red / count, green / count, blue / count, 255);
    }
}
