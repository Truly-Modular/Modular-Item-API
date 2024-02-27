package smartin.miapi.client.model;

import net.minecraft.block.Blocks;
import net.minecraft.block.entity.ConduitBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.ConduitBlockEntityRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import smartin.miapi.item.modular.Transform;
import smartin.miapi.mixin.ConduitBlockEntityAccessor;

public class ConduitRendererEntity implements MiapiModel {
    Transform transform;
    ConduitBlockEntityRenderer renderer;
    ConduitBlockEntity conduitBlockEntity;

    public ConduitRendererEntity(Transform transform) {
        this.transform = transform;
        conduitBlockEntity = new ConduitBlockEntity(BlockPos.ORIGIN, Blocks.CONDUIT.getDefaultState());
        ((ConduitBlockEntityAccessor) conduitBlockEntity).setActive(true);
        //MinecraftClient.getInstance().getBlockEntityRenderDispatcher().renderEntity()
        //renderer = new ConduitBlockEntityRenderer();
    }

    @Override
    public void render(MatrixStack matrices, ItemStack stack, ModelTransformationMode transformationMode, float tickDelta, VertexConsumerProvider vertexConsumers, LivingEntity entity, int light, int overlay) {
        matrices.push();
        transform.applyPosition(matrices);
        MinecraftClient.getInstance().getBlockEntityRenderDispatcher().renderEntity(conduitBlockEntity, matrices, vertexConsumers, light, overlay);
        //renderer.render(conduitBlockEntity, tickDelta, matrices, vertexConsumers, light, overlay);
        matrices.pop();
    }
}