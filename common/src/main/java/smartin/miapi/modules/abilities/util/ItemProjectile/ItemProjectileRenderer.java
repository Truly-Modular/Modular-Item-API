package smartin.miapi.modules.abilities.util.ItemProjectile;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
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

    @Override
    public void render(ItemProjectile entity, float yaw, float tickDelta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumers, int light) {
        ItemStack itemStack = entity.asItemStack();
        if (itemStack != null && !itemStack.isEmpty()) {
            BakedModel model = ModelProperty.getItemModel(itemStack);
            if (model != null) {
                matrixStack.push();
                matrixStack.peek().getPositionMatrix().scale(2);
                matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(MathHelper.lerp(tickDelta, entity.prevYaw, entity.getYaw()) - 90.0F));
                matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(MathHelper.lerp(tickDelta, entity.prevPitch, entity.getPitch()) + 90.0F));
                matrixStack.peek().getPositionMatrix().rotateXYZ((float) 0, (float) 0, (float) (Math.PI / 4) * 5);
                ItemRenderUtil.renderModel(matrixStack, itemStack, model, ModelTransformationMode.GROUND, vertexConsumers, light, 0);
                matrixStack.pop();
            }
        }
    }
}
