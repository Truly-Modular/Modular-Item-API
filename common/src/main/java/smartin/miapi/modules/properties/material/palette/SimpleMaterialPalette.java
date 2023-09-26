package smartin.miapi.modules.properties.material.palette;

import com.redpxnda.nucleus.util.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.client.MaterialAtlasManager;
import smartin.miapi.client.MaterialVertexConsumer;
import smartin.miapi.client.MiapiClient;
import smartin.miapi.mixin.client.SpriteContentsAccessor;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.material.Material;
import smartin.miapi.registries.RegistryInventory;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public abstract class SimpleMaterialPalette implements MaterialPalette {
    protected final Material material;
    protected Identifier paletteSpriteId = MaterialAtlasManager.BASE_MATERIAL_ID;
    protected MaterialVertexConsumer cachedVertexConsumer;
    protected Color paletteAverageColor;

    public SimpleMaterialPalette(Material material) {
        this.material = material;
    }

    @Nullable
    public abstract SpriteContents generateSpriteContents(Identifier id);

    @Nullable
    public Identifier getSpriteId() {
        return paletteSpriteId;
    }

    public void setSpriteId(Identifier id) {
        paletteSpriteId = id;
    }

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
            paletteAverageColor = new Color(red/colors.size(), green/colors.size(), blue/colors.size(), alpha/colors.size());
        }
        return paletteAverageColor;
    }

    public VertexConsumer getVertexConsumer(VertexConsumerProvider vertexConsumers, ItemStack stack, ItemModule.ModuleInstance moduleInstance, ModelTransformationMode mode) {
        if (cachedVertexConsumer == null) {
            cachedVertexConsumer = new MaterialVertexConsumer(vertexConsumers.getBuffer(RegistryInventory.Client.entityTranslucentMaterialRenderType), material);
        } else {
            cachedVertexConsumer.delegate = vertexConsumers.getBuffer(RegistryInventory.Client.entityTranslucentMaterialRenderType);
        }
        return cachedVertexConsumer;
    }
}
