package smartin.miapi.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class ItemProjectileRenderer extends EntityRenderer<ItemProjectileEntity> {

    public ItemProjectileRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public ResourceLocation getTextureLocation(ItemProjectileEntity entity) {
        return null;
    }

    @Override
    public void render(ItemProjectileEntity entity, float yaw, float tickDelta, PoseStack matrixStack, MultiBufferSource vertexConsumers, int light) {
        ItemStack itemStack = entity.getPickupItem();
        if (itemStack != null && !itemStack.isEmpty()) {
            matrixStack.pushPose();
            matrixStack.last().pose().scale(2);
            matrixStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(tickDelta, entity.yRotO, entity.getYRot()) - 90.0F));
            matrixStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(tickDelta, entity.xRotO, entity.getXRot()) + 90.0F));
            matrixStack.last().pose().rotateXYZ(0, 0, (float) (Math.PI / 4) * 5);
            if (Minecraft.getInstance().player != null) {
                //Transform transform;
                //transform.toMatrix();
                //matrixStack.multiplyPositionMatrix(transform.toMatrix());
                Minecraft.getInstance().getItemRenderer().renderStatic(
                        itemStack, ItemDisplayContext.GROUND, light,
                        OverlayTexture.NO_OVERLAY, matrixStack, vertexConsumers,
                        Minecraft.getInstance().player.level(), 1
                );
            }
            matrixStack.popPose();
        }
    }
}
