package smartin.miapi.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;

public class TrimRenderer {
    public static final TextureAtlas armorTrimsAtlas = Minecraft.getInstance().getModelManager().getAtlas(Sheets.ARMOR_TRIMS_SHEET);
    public static final TextureAtlas atlas = Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS);


    public static void renderTrims(PoseStack matrixStack, BakedQuad bakedQuad, TrimMode trimMode, int light, MultiBufferSource vertexConsumers, Holder<ArmorMaterial> armorMaterial, ItemStack itemStack) {
        ArmorTrim armorTrim = itemStack.getComponents().get(DataComponents.TRIM);
        if (armorTrim != null) {

            TextureAtlasSprite sprite = switch (trimMode) {
                case ITEM -> null;
                case ARMOR_LAYER_ONE -> armorTrimsAtlas.getSprite(armorTrim.innerTexture(armorMaterial));
                case ARMOR_LAYER_TWO -> armorTrimsAtlas.getSprite(armorTrim.innerTexture(armorMaterial));
                default -> null;
            };
            if (sprite != null) {

                VertexConsumer vertexConsumer = sprite.wrap(vertexConsumers.getBuffer(Sheets.armorTrimsSheet(armorTrim.pattern().value().decal())));
                vertexConsumer.putBulkData(matrixStack.last(),bakedQuad,1.0f,1.0f,1.0f,1.0f,light,OverlayTexture.NO_OVERLAY);
            }
        }
    }

    public enum TrimMode {
        NONE,
        ITEM,
        ARMOR_LAYER_ONE,
        ARMOR_LAYER_TWO
    }
}
