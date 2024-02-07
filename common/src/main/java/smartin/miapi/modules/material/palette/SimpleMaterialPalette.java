package smartin.miapi.modules.material.palette;

import com.redpxnda.nucleus.util.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.client.MiapiClient;
import smartin.miapi.client.atlas.MaterialSpriteManager;
import smartin.miapi.client.renderer.RescaledVertexConsumer;
import smartin.miapi.mixin.client.SpriteContentsAccessor;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.material.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public abstract class SimpleMaterialPalette implements MaterialPalette {
    protected final Material material;
    protected Identifier paletteSpriteId = Material.BASE_PALETTE_ID;
    public static Map<Sprite, RescaledVertexConsumer> lookupMap = new WeakHashMap<>();
    protected Color paletteAverageColor;

    public SimpleMaterialPalette(Material material) {
        this.material = material;
    }

    @Environment(EnvType.CLIENT)
    @Nullable
    public abstract SpriteContents generateSpriteContents(Identifier id);

    @Environment(EnvType.CLIENT)
    @Nullable
    public Identifier getSpriteId() {
        return paletteSpriteId;
    }

    @Environment(EnvType.CLIENT)
    public void setSpriteId(Identifier id) {
        paletteSpriteId = id;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public boolean useMaterialAtlas(){
        return true;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public Color getPaletteAverageColor() {
        if (paletteAverageColor == null) {
            NativeImage img = ((SpriteContentsAccessor) MiapiClient.materialAtlasManager.getMaterialSprite(paletteSpriteId).getContents()).getImage();

            List<Color> colors = new ArrayList<>();
            int height = img.getHeight();
            int width = img.getWidth();
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    int color = img.getColor(x, y);
                    colors.add(new Color(
                            ColorHelper.Abgr.getRed(color),
                            ColorHelper.Abgr.getGreen(color),
                            ColorHelper.Abgr.getBlue(color),
                            ColorHelper.Abgr.getAlpha(color)
                    ));
                }
            }

            int red = 0;
            int green = 0;
            int blue = 0;
            int alpha = 0;
            for (Color color : colors) {
                if (color.a() > 0) {
                    red += color.r();
                    green += color.g();
                    blue += color.b();
                    alpha += color.a();
                }
            }
            paletteAverageColor = new Color(red / colors.size(), green / colors.size(), blue / colors.size(), alpha / colors.size());
        }
        return paletteAverageColor;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public VertexConsumer getVertexConsumer(VertexConsumerProvider vertexConsumers, Sprite originalSprite, ItemStack stack, ModuleInstance moduleInstance, ModelTransformationMode mode) {
        Identifier replaceId = MaterialSpriteManager.getMaterialSprite(originalSprite, material);
        RenderLayer atlasRenderLayer = RenderLayer.getEntityTranslucentCull(replaceId);
        VertexConsumer atlasConsumer = ItemRenderer.getDirectItemGlintConsumer(vertexConsumers, atlasRenderLayer, true, false);
        return get(atlasConsumer, originalSprite);
    }

    public RescaledVertexConsumer get(VertexConsumer vertexConsumer, Sprite sprite) {
        RescaledVertexConsumer rescaled = lookupMap.computeIfAbsent(sprite, (s) -> new RescaledVertexConsumer(vertexConsumer, sprite));
        rescaled.delegate = vertexConsumer;
        return rescaled;
    }
}
