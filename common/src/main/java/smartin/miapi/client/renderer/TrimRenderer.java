package smartin.miapi.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;

public class TrimRenderer {
    public static final TextureAtlas armorTrimsAtlas = Minecraft.getInstance().getModelManager().getAtlas(Sheets.ARMOR_TRIMS_SHEET);
    public static final TextureAtlas atlas = Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS);


    public static void renderTrims(PoseStack matrixStack, BakedQuad bakedQuad, TrimMode trimMode, int light, MultiBufferSource vertexConsumers, ArmorMaterial armorMaterial, ItemStack itemStack) {
        ItemRenderer renderer;
        ArmorTrim.getTrim(Minecraft.getInstance().level.registryAccess(), itemStack).ifPresent((trim) -> {
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
