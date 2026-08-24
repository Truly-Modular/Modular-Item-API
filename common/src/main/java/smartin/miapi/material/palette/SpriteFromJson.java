package smartin.miapi.material.palette;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.redpxnda.nucleus.util.Color;
import com.redpxnda.nucleus.util.MiscUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.client.atlas.MaterialAtlasManager;
import smartin.miapi.client.atlas.MaterialSpriteManager;
import smartin.miapi.client.renderer.NativeImageGetter;
import smartin.miapi.registries.JsonOpsBooleanPatched;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
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

    public static final MapCodec<SpriteFromJson> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
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

    public static SpriteFromJson getFromJson(JsonElement element) {
        return MAP_CODEC.codec().decode(JsonOpsBooleanPatched.INSTANCE, element).getOrThrow().getFirst();
    }


    public SpriteFromJson(String atlasKey, String texturePath, boolean forceTick) {
        AtomicReference<SpriteContents> contents = new AtomicReference<>();
        SpriteContents actualContents = findContextSavely(atlasKey, texturePath);
        contents.set(actualContents);
        imageSupplier = () -> {
            NativeImageGetter.ImageHolder image = NativeImageGetter.get(contents.get());
            if (NativeImageGetter.isStillValid(image.nativeImage)) {
                return NativeImageGetter.get(contents.get());
            }else{
                SpriteContents nextContext = findContextSavely(atlasKey, texturePath);
                contents.set(nextContext);
                return NativeImageGetter.get(nextContext);
            }
        };
        isAnimated = forceTick || SpriteColorer.isAnimatedSpriteStatic(contents.get());
    }

    private @NotNull SpriteContents findContextSavely(String atlasKey, String texturePath) {
        ResourceLocation atlasId = atlasIdShortcuts.getOrDefault(atlasKey, ResourceLocation.parse(atlasKey));
        ResourceLocation textureId = ResourceLocation.parse(texturePath);
        TextureAtlas atlasSprite = Minecraft.getInstance().getModelManager().getAtlas(atlasId);
        if (atlasSprite == null) {
            throw new RuntimeException("could not find atlas" + atlasKey);
        }
        rawSprite = atlasSprite.getSprite(textureId);
        if (rawSprite == null) {
            throw new RuntimeException("could not find atlas image" + textureId + " on atlas " + atlasKey);
        }
        SpriteContents contents = rawSprite.contents();
        return contents;
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
