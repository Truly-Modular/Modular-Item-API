package smartin.miapi.modules.abilities.util.ItemProjectile;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.TridentEntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import smartin.miapi.Miapi;
import smartin.miapi.client.model.ItemRenderUtil;
import smartin.miapi.modules.properties.render.ModelProperty;

public class ItemProjectileRenderer extends EntityRenderer<ItemProjectile> {

    public ItemProjectileRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public Identifier getTexture(ItemProjectile entity) {
        return null;
    }

    //todo correct direction rotatiion
    @Override
    public void render(ItemProjectile entity, float yaw, float tickDelta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumers, int light) {
        ItemStack itemStack = entity.asItemStack();
        //Miapi.LOGGER.error(String.valueOf(itemStack));
        if (itemStack != null && !itemStack.isEmpty()) {
            BakedModel model = ModelProperty.getItemModel(itemStack);
            if (model != null) {
                matrixStack.push();
                matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(MathHelper.lerp(tickDelta, entity.prevYaw, entity.getYaw()) - 90.0F));
                matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(MathHelper.lerp(tickDelta, entity.prevPitch, entity.getPitch()) + 90.0F));
                //VertexConsumer vertexConsumer = ItemRenderer.getDirectItemGlintConsumer(vertexConsumerProvider, this.model.getLayer(this.getTexture(tridentEntity)), false, tridentEntity.isEnchanted());
                ItemRenderUtil.renderModel(matrixStack, itemStack, model, ModelTransformationMode.GUI, vertexConsumers, light, 0);
                matrixStack.pop();
            }
        }
    }
}
