package smartin.miapi.client.renderer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.item.trim.ArmorTrim;

public class TrimRenderer {
    public static final SpriteAtlasTexture armorTrimsAtlas = MinecraftClient.getInstance().getBakedModelManager().getAtlas(TexturedRenderLayers.ARMOR_TRIMS_ATLAS_TEXTURE);
    public static final SpriteAtlasTexture atlas = MinecraftClient.getInstance().getBakedModelManager().getAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);


    public static void renderTrims(MatrixStack matrixStack, BakedQuad bakedQuad, TrimMode trimMode, int light, VertexConsumerProvider vertexConsumers, ArmorMaterial armorMaterial, ItemStack itemStack) {
        ItemRenderer renderer;
        ArmorTrim.getTrim(MinecraftClient.getInstance().world.getRegistryManager(), itemStack).ifPresent((trim) -> {
            Sprite sprite = switch (trimMode) {
                case ITEM -> //TODO:figure out item Trim mode
                        atlas.getSprite(trim.getGenericModelId(armorMaterial));
                case ARMOR_LAYER_ONE ->
                        armorTrimsAtlas.getSprite(trim.getGenericModelId(armorMaterial));
                case ARMOR_LAYER_TWO ->
                        armorTrimsAtlas.getSprite(trim.getLeggingsModelId(armorMaterial));
                default -> bakedQuad.getSprite();
            };
            VertexConsumer vertexConsumer = sprite.getTextureSpecificVertexConsumer(vertexConsumers.getBuffer(TexturedRenderLayers.getArmorTrims()));
            vertexConsumer.quad(matrixStack.peek(), bakedQuad, 1, 1, 1, light, OverlayTexture.DEFAULT_UV);
        });
    }

    public enum TrimMode {
        NONE,
        ITEM,
        ARMOR_LAYER_ONE,
        ARMOR_LAYER_TWO
    }
}
